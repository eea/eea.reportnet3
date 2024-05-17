package org.eea.dataset.service.impl;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.impl.S3ServiceImpl;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.service.SpatialDataHandling;
import org.eea.dataset.service.ParquetConverterService;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.helper.CsvModification;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.dataset.service.model.ImportFileInDremioInfo;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.orchestrator.enums.JobInfoEnum;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.*;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.eea.utils.LiteralConstants.*;

@ImportDataLakeCommons
@Service
public class ParquetConverterServiceImpl implements ParquetConverterService {

    private static final Logger LOG = LoggerFactory.getLogger(ParquetConverterServiceImpl.class);

    private final static String MODIFIED_CSV_SUFFIX = "_%s.csv";
    private final static String CSV_EXTENSION = ".csv";
    private final static String PARQUET_EXTENSION = ".parquet";

    private final static String DEFAULT_PARQUET_NAME = "0_0_0.parquet";

    @Value("${loadDataDelimiter}")
    private char defaultDelimiter;

    @Value("${dremio.parquetConverter.custom}")
    private Boolean convertParquetWithCustomWay;

    @Value("${dremio.parquetConverter.custom.maxCsvLinesPerFile}")
    private Integer maxCsvLinesPerFile;

    @Autowired
    private FileCommonUtils fileCommonUtils;

    @Lazy
    @Autowired
    private FileTreatmentHelper fileTreatmentHelper;

    @Autowired
    JdbcTemplate dremioJdbcTemplate;

    @Autowired
    public DremioHelperService dremioHelperService;

    @Autowired
    private S3ServiceImpl s3Service;

    @Autowired
    private S3Helper s3Helper;

    @Autowired
    DataFlowControllerZuul dataFlowControllerZuul;

    @Autowired
    JobControllerZuul jobControllerZuul;

    @Override
    public void convertCsvFilesToParquetFiles(ImportFileInDremioInfo importFileInDremioInfo, List<File> csvFiles, DataSetSchema dataSetSchema) throws Exception {
        String tableSchemaName;
        Integer numberOfEmptyFiles = 0;
        for(File csvFile: csvFiles){
            if(StringUtils.isNotBlank(importFileInDremioInfo.getTableSchemaId())){
                tableSchemaName = fileCommonUtils.getTableName(importFileInDremioInfo.getTableSchemaId(), dataSetSchema);
            }
            else{
                tableSchemaName = csvFile.getName().replace(CSV_EXTENSION, "");
            }
            convertCsvToParquet(csvFile, dataSetSchema, importFileInDremioInfo, tableSchemaName);
            if(importFileInDremioInfo.getSendEmptyFileWarning()){
                numberOfEmptyFiles++;
            }
        }
        if(numberOfEmptyFiles != 0) {
            if (numberOfEmptyFiles == csvFiles.size()) {
                //all files were empty and an error (instead of a warning) must be thrown
                importFileInDremioInfo.setSendEmptyFileWarning(false);
                importFileInDremioInfo.setErrorMessage(EEAErrorMessage.ERROR_IMPORT_EMPTY_FILES);
                throw new Exception(EEAErrorMessage.ERROR_IMPORT_EMPTY_FILES);
            } else {
                importFileInDremioInfo.setSendEmptyFileWarning(true);
            }
        }
    }

    private void convertCsvToParquet(File csvFile, DataSetSchema dataSetSchema, ImportFileInDremioInfo importFileInDremioInfo,
                                              String tableSchemaName) throws Exception {
        LOG.info("For job {} converting csv file {} to parquet file", importFileInDremioInfo, csvFile.getPath());
        //create a new csv file that contains records ids and data provider code as extra information
        List<File> csvFilesWithAddedColumns = null;
        CsvModification csvModification = null;
        if(convertParquetWithCustomWay){
            csvFilesWithAddedColumns = modifyAndSplitCsvFile(csvFile, dataSetSchema, importFileInDremioInfo);
        }
        else{
            csvModification = modifyCsvFile(csvFile, dataSetSchema, importFileInDremioInfo);
            csvFilesWithAddedColumns = csvModification.getModifiedCsvFiles();
        }

        if(csvFilesWithAddedColumns == null){
           importFileInDremioInfo.setSendEmptyFileWarning(true);
           return;
        }
        else{
            importFileInDremioInfo.setSendEmptyFileWarning(false);
        }

        S3PathResolver s3ImportPathResolver = constructS3PathResolver(importFileInDremioInfo, tableSchemaName, tableSchemaName, S3_IMPORT_FILE_PATH);
        S3PathResolver s3TablePathResolver = constructS3PathResolver(importFileInDremioInfo, tableSchemaName, tableSchemaName, S3_TABLE_NAME_FOLDER_PATH);

        //path in dremio for the folder in current that represents the table of the dataset
        String dremioPathForParquetFolder = getImportQueryPathForFolder(importFileInDremioInfo, tableSchemaName, tableSchemaName, LiteralConstants.S3_TABLE_AS_FOLDER_QUERY_PATH);
        //path in s3 for the folder that contains the stored csv files
        String s3PathForCsvFolder = s3Service.getTableAsFolderQueryPath(s3ImportPathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH);

        if (importFileInDremioInfo.getReplaceData()) {
            //remove tables and folders that contain the previous csv files because data will be replaced
            LOG.info("Removing csv files for job {}", importFileInDremioInfo);
            removeCsvFilesThatWillBeReplaced(s3ImportPathResolver, tableSchemaName, s3PathForCsvFolder);

            //delete attachments if they exist
            if (s3Helper.checkFolderExist(s3TablePathResolver, S3_ATTACHMENTS_TABLE_PATH)) {
                s3Helper.deleteFolder(s3TablePathResolver, S3_ATTACHMENTS_TABLE_PATH);
            }
        }

        Boolean needToDemoteTable = true;

        for(File csvFileWithAddedColumns: csvFilesWithAddedColumns) {

            String csvFileName = csvFileWithAddedColumns.getName().replace(CSV_EXTENSION, "");

            //path in s3 for the stored csv file
            String s3PathForModifiedCsv = getImportPathForCsv(importFileInDremioInfo, csvFileWithAddedColumns.getName(), tableSchemaName);

            //path in dremio for the stored csv file
            String dremioPathForCsvFile = getImportQueryPathForFolder(importFileInDremioInfo, csvFileWithAddedColumns.getName(), tableSchemaName, LiteralConstants.S3_IMPORT_CSV_FILE_QUERY_PATH);
            //path in dremio for the folder that contains the created parquet file
            String parquetInnerFolderPath = dremioPathForParquetFolder + ".\"" + csvFileName + "\"";

            //upload csv file
            uploadCsvFileAndPromoteIt(s3ImportPathResolver, tableSchemaName, s3PathForModifiedCsv, csvFileWithAddedColumns.getPath(), csvFileWithAddedColumns.getName(), dremioPathForCsvFile);

            if(needToDemoteTable) {
                //demote table folder
                dremioHelperService.demoteFolderOrFile(s3TablePathResolver, tableSchemaName);
                if (importFileInDremioInfo.getReplaceData()) {
                    LOG.info("Removing parquet files for job {}", importFileInDremioInfo);
                    //remove folders that contain the previous parquet files because data will be replaced
                    if (s3Helper.checkFolderExist(s3TablePathResolver, S3_TABLE_NAME_FOLDER_PATH)) {
                        s3Helper.deleteFolder(s3TablePathResolver, S3_TABLE_NAME_FOLDER_PATH);
                    }
                }
                needToDemoteTable = false;
            }

            //create parquet file
            if (convertParquetWithCustomWay) {
                LOG.info("For import job {} the conversion of the csv to parquet will use the custom implementation", importFileInDremioInfo);
                String parquetFilePathInReportNet = csvFileWithAddedColumns.getPath().replace(CSV_EXTENSION, PARQUET_EXTENSION);
                convertCsvToParquetCustom(csvFileWithAddedColumns, parquetFilePathInReportNet, importFileInDremioInfo);
                //upload parquet file
                String parquetFileName = csvFileName + "/" + DEFAULT_PARQUET_NAME;
                String importPathForParquet = getImportS3PathForParquet(importFileInDremioInfo, parquetFileName, tableSchemaName);
                s3Helper.uploadFileToBucket(importPathForParquet, parquetFilePathInReportNet);
            } else {
                LOG.info("For import job {} the conversion of the csv to parquet will use a dremio query", importFileInDremioInfo);
                if (csvModification != null) {
                    String createTableQuery = getTableQuery(csvModification, parquetInnerFolderPath, dremioPathForCsvFile);
                    String processId = dremioHelperService.executeSqlStatement(createTableQuery);
                    dremioHelperService.ckeckIfDremioProcessFinishedSuccessfully(createTableQuery, processId);
                }
            }
        }
        //refresh the metadata
        dremioHelperService.refreshTableMetadataAndPromote(importFileInDremioInfo.getJobId(), dremioPathForParquetFolder, s3TablePathResolver, tableSchemaName);

        if (importFileInDremioInfo.getUpdateReferenceFolder()) {
            LOG.info("For job {} the REFERENCE dataset files must be copied to reference folder", importFileInDremioInfo);
            handleReferenceDataset(importFileInDremioInfo, s3TablePathResolver);
        }
        LOG.info("For job {} the import for table {} has been completed", importFileInDremioInfo, tableSchemaName);
    }

    private static String getTableQuery(CsvModification csvModification, String parquetInnerFolderPath, String dremioPathForCsvFile) {
        String createTableQuery;
        SpatialDataHandling spatialDataHandling = new SpatialDataHandlingImpl(csvModification.getCsvHeaders());
        if(spatialDataHandling.geoJsonHeadersIsNotEmpty(true)) {
            String initQuery = "CREATE TABLE " + parquetInnerFolderPath + " AS SELECT %s %s FROM " + dremioPathForCsvFile;
            createTableQuery = String.format(initQuery, spatialDataHandling.getSimpleHeaders(), spatialDataHandling.getHeadersConvertedToBinary());
        } else {
            createTableQuery = "CREATE TABLE " + parquetInnerFolderPath + " AS SELECT * FROM " + dremioPathForCsvFile;
        }
        return createTableQuery;
    }

    private void handleReferenceDataset(ImportFileInDremioInfo importFileInDremioInfo, S3PathResolver s3TablePathResolver) throws Exception {
        String tableSchemaName = s3TablePathResolver.getTableName();
        List<S3Object> tableNameFilenames = s3Helper.getFilenamesFromTableNames(s3TablePathResolver);
        AtomicInteger fileCounter = new AtomicInteger();

        //demote reference table folder
        S3PathResolver s3ReferenceTablePathResolver = constructS3PathResolver(importFileInDremioInfo, tableSchemaName, tableSchemaName, S3_DATAFLOW_REFERENCE_FOLDER_PATH);
        dremioHelperService.demoteFolderOrFile(s3ReferenceTablePathResolver, tableSchemaName);
        if(importFileInDremioInfo.getReplaceData()){
            //remove folders that contain the previous parquet files because data will be replaced
            if (s3Helper.checkFolderExist(s3ReferenceTablePathResolver, S3_DATAFLOW_REFERENCE_FOLDER_PATH)) {
                s3Helper.deleteFolder(s3ReferenceTablePathResolver, S3_DATAFLOW_REFERENCE_FOLDER_PATH);
            }
        }

        tableNameFilenames.stream().forEach(file -> {
            String key = file.key();
            String filename = new File(key).getName();
            S3PathResolver s3ReferenceParquetPathResolver = constructS3PathResolver(importFileInDremioInfo, filename, tableSchemaName, S3_DATAFLOW_REFERENCE_PATH);
            s3ReferenceParquetPathResolver.setFilename(filename);
            s3ReferenceParquetPathResolver.setParquetFolder(key.split("/")[5]);
            try {
                String referenceParquetPath = s3Service.getS3Path(s3ReferenceParquetPathResolver);
                //copy file to reference folder
                s3Helper.copyFileToAnotherDestination(key, referenceParquetPath);

                //path in dremio for the reference folder that represents the table of the dataset
                String dremioPathForReferenceParquetFolder = getImportQueryPathForFolder(importFileInDremioInfo, tableSchemaName, tableSchemaName, LiteralConstants.S3_DATAFLOW_REFERENCE_QUERY_PATH);

                //refresh the metadata
                dremioHelperService.refreshTableMetadataAndPromote(importFileInDremioInfo.getJobId(), dremioPathForReferenceParquetFolder, s3ReferenceTablePathResolver, tableSchemaName);
                fileCounter.incrementAndGet();
            }
            catch (Exception e) {
                LOG.error("Error copying items to reference folder for import job {} ",importFileInDremioInfo, e);
            }
        });

        if(fileCounter.get() != tableNameFilenames.size()){
            throw new Exception("Error copying items to reference folder for import job " + importFileInDremioInfo.getJobId());
        }
        LOG.info("For job {} the REFERENCE dataset files have been successfully copied to the reference folder", importFileInDremioInfo);

    }

    private String getImportS3PathForParquet(ImportFileInDremioInfo importFileInDremioInfo, String fileName, String tableSchemaName) throws Exception {
        S3PathResolver s3PathResolver = constructS3PathResolver(importFileInDremioInfo, fileName, tableSchemaName, S3_TABLE_NAME_PATH);
        String pathToS3ForImport = s3Service.getS3Path(s3PathResolver);
        if(StringUtils.isBlank(pathToS3ForImport)){
            LOG.error("Could not resolve path to s3 for import for job {}", importFileInDremioInfo);
            throw new Exception("Could not resolve path to s3 for import");
        }
        return pathToS3ForImport;
    }

    private void uploadCsvFileAndPromoteIt(S3PathResolver s3PathResolver, String tableSchemaName, String s3PathForModifiedCsv, String csvFilePath, String csvFileName, String dremioPathForCsvFile){
        //upload modified csv file to s3
        s3Helper.uploadFileToBucket(s3PathForModifiedCsv, csvFilePath);
        //set up temporary s3PathResolver fileName so that the csv file will be promoted
        s3PathResolver.setFilename(csvFileName);
        dremioHelperService.promoteFolderOrFile(s3PathResolver, csvFileName);
        //revert s3PathResolver fileName
        s3PathResolver.setFilename(tableSchemaName);
        //refresh the metadata for the csv table
        String refreshImportTableQuery = "ALTER TABLE " + dremioPathForCsvFile + " REFRESH METADATA";
        dremioHelperService.executeSqlStatement(refreshImportTableQuery);
    }

    @Override
    public void removeCsvFilesThatWillBeReplaced(S3PathResolver s3PathResolver, String tableSchemaName, String s3PathForCsvFolder){
        List<ObjectIdentifier> csvFilesInS3 = s3Helper.listObjectsInBucket(s3PathForCsvFolder);
        for(ObjectIdentifier csvFileInS3 : csvFilesInS3){
            String[] csvFileNameSplit = csvFileInS3.key().split("/");
            String csvFileName = csvFileNameSplit[csvFileNameSplit.length - 1];
            //set up temporary s3PathResolver fileName so that the csv file will be demoted
            s3PathResolver.setFilename(csvFileName);
            dremioHelperService.demoteFolderOrFile(s3PathResolver, csvFileName);
            //revert s3PathResolver fileName
            s3PathResolver.setFilename(tableSchemaName);
        }
        if (s3Helper.checkFolderExist(s3PathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH)) {
            s3Helper.deleteFolder(s3PathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH);
        }
    }

    private CsvModification modifyCsvFile(File csvFile, DataSetSchema dataSetSchema, ImportFileInDremioInfo importFileInDremioInfo) throws Exception {
        char delimiterChar = defaultDelimiter;
        if (!StringUtils.isBlank(importFileInDremioInfo.getDelimiter())){
            delimiterChar = importFileInDremioInfo.getDelimiter().charAt(0);
        }
        String randomStrForNewFolderSuffix = UUID.randomUUID().toString();
        List<File> modifiedCsvFiles = new ArrayList<>();
        String modifiedFilePath = csvFile.getPath().replace(".csv", "") + String.format(MODIFIED_CSV_SUFFIX, randomStrForNewFolderSuffix);
        File csvFileWithAddedColumns = new File(modifiedFilePath);
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvFileWithAddedColumns.getPath()));
        List<String> csvHeaders = new ArrayList<>();
        int recordCounter = 0;
        //Reading csv file
        try (
                Reader reader = Files.newBufferedReader(Paths.get(csvFile.getPath()));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(false)
                        .setDelimiter(delimiterChar)
                        .setIgnoreHeaderCase(true)
                        .setIgnoreEmptyLines(false)
                        .setTrim(true).build())) {
            csvHeaders.add(LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER);
            csvHeaders.addAll(csvParser.getHeaderMap().keySet());
            csvHeaders.add(LiteralConstants.PARQUET_PROVIDER_CODE_COLUMN_HEADER);


            String tableSchemaId = importFileInDremioInfo.getTableSchemaId();
            if (StringUtils.isBlank(tableSchemaId)) {
                tableSchemaId = fileTreatmentHelper.getTableSchemaIdFromFileName(dataSetSchema, csvFile.getName(), false);
            }
            List<String> expectedHeaders = checkIfCSVHeadersAreCorrect(csvHeaders, dataSetSchema, importFileInDremioInfo, csvFile.getName(), tableSchemaId);
            Map<String, DataType> fieldNameAndTypeMap = getFieldNameAndTypeMap(tableSchemaId, dataSetSchema);

            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setDelimiter(LiteralConstants.COMMA).build());
            for (CSVRecord csvRecord : csvParser) {
                if(csvRecord.values().length == 0){
                    LOG.error("Empty first line in csv file {}. {}", csvFile.getPath(), importFileInDremioInfo);
                    throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE);
                }
                recordCounter++;
                if(recordCounter == 1){
                    csvPrinter.printRecord(expectedHeaders);
                }
                List<String> row = new ArrayList<>();
                String recordIdValue = UUID.randomUUID().toString();
                for (String expectedHeader : expectedHeaders) {
                    DataType fieldType = fieldNameAndTypeMap.get(expectedHeader);
                    if(fieldType == DataType.ATTACHMENT){
                        //do not add any value to attachment fields
                        row.add("");
                        continue;
                    }
                    if (expectedHeader.equals(LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER)) {
                        row.add(recordIdValue);
                    } else if (expectedHeader.equals(LiteralConstants.PARQUET_PROVIDER_CODE_COLUMN_HEADER)) {
                        row.add((importFileInDremioInfo.getDataProviderCode() != null) ? importFileInDremioInfo.getDataProviderCode() : "");
                    } else {
                        if(csvRecord.isMapped(expectedHeader)){
                            row.add(csvRecord.get(expectedHeader));
                        }
                        else{
                            //check if header contains bom
                            String headerWithBom = "\uFEFF" + expectedHeader;
                            if(csvRecord.isMapped(headerWithBom)){
                                row.add(csvRecord.get(headerWithBom));
                            }
                            else {
                                row.add("");
                            }
                        }

                    }
                }
                csvPrinter.printRecord(row);
            }
            csvPrinter.flush();
        } catch (IOException | UncheckedIOException e) {
            if(e.getClass().equals(UncheckedIOException.class) && ((UncheckedIOException) e).getMessage().contains("invalid char between encapsulated token and delimiter")){
                Integer lineNumber = extractCsvErrorLineNumber(((UncheckedIOException) e).getMessage());
                if(lineNumber != -1){
                    jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.ERROR_CSV_MULTIPLE_QUOTES_WITH_LINE_NUM, lineNumber);
                }
                else{
                    jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.ERROR_CSV_MULTIPLE_QUOTES, null);
                }
            }
            else if (e.getClass().equals(MalformedInputException.class)){
                jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.ERROR_CSV_ILLEGAL_CHARACTERS, null);
            }
            LOG.error("Could not read csv file {}. {}", csvFile.getPath(), importFileInDremioInfo);
            throw new Exception("Could not read csv file " + csvFile.getPath());
        }
        finally {
            writer.close();
        }
        if(recordCounter == 0){
            LOG.info("For job {} file {} contains only headers", importFileInDremioInfo, csvFile.getName());
            return new CsvModification(null, csvHeaders);
        }
        modifiedCsvFiles.add(csvFileWithAddedColumns);
        return new CsvModification(modifiedCsvFiles, csvHeaders);
    }


    private List<File> modifyAndSplitCsvFile(File csvFile, DataSetSchema dataSetSchema, ImportFileInDremioInfo importFileInDremioInfo) throws Exception {
        char delimiterChar = defaultDelimiter;
        if (!StringUtils.isBlank(importFileInDremioInfo.getDelimiter())){
            delimiterChar = importFileInDremioInfo.getDelimiter().charAt(0);
        }

        List<File> modifiedCsvFiles = new ArrayList<>();
        String modifiedFilePath;
        File csvFileWithAddedColumns = null;
        BufferedWriter writer = null;
        CSVPrinter csvPrinter = null;

        List<String> csvHeaders = new ArrayList<>();
        List<String> expectedHeaders;
        //Reading csv file
        try (
                Reader reader = Files.newBufferedReader(Paths.get(csvFile.getPath()));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(false)
                        .setDelimiter(delimiterChar)
                        .setIgnoreHeaderCase(true)
                        .setIgnoreEmptyLines(false)
                        .setTrim(true).build())) {
            csvHeaders.add(LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER);
            csvHeaders.addAll(csvParser.getHeaderMap().keySet());
            csvHeaders.add(LiteralConstants.PARQUET_PROVIDER_CODE_COLUMN_HEADER);

            String tableSchemaId = importFileInDremioInfo.getTableSchemaId();
            if (StringUtils.isBlank(tableSchemaId)) {
                tableSchemaId = fileTreatmentHelper.getTableSchemaIdFromFileName(dataSetSchema, csvFile.getName(), false);
            }
            expectedHeaders = checkIfCSVHeadersAreCorrect(csvHeaders, dataSetSchema, importFileInDremioInfo, csvFile.getName(), tableSchemaId);
            Map<String, DataType> fieldNameAndTypeMap = getFieldNameAndTypeMap(tableSchemaId, dataSetSchema);

            int recordCounter = 0;
            Boolean emptyFile = true;

            for (CSVRecord csvRecord : csvParser) {
                emptyFile = false;
                if(csvRecord.values().length == 0){
                    LOG.error("Empty first line in csv file {}. {}", csvFile.getPath(), importFileInDremioInfo);
                    throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE);
                }

                if(recordCounter == 0){
                    //create new file
                    String randomStrForNewFolderSuffix = UUID.randomUUID().toString();
                    modifiedFilePath = csvFile.getPath().replace(".csv", "") + String.format(MODIFIED_CSV_SUFFIX, randomStrForNewFolderSuffix);
                    csvFileWithAddedColumns = new File(modifiedFilePath);
                    writer = Files.newBufferedWriter(Paths.get(csvFileWithAddedColumns.getPath()));
                    csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setDelimiter(LiteralConstants.COMMA).build());

                    //add headers
                    csvPrinter.printRecord(expectedHeaders);
                }
                List<String> row = new ArrayList<>();
                String recordIdValue = UUID.randomUUID().toString();
                for (String expectedHeader : expectedHeaders) {
                    DataType fieldType = fieldNameAndTypeMap.get(expectedHeader);
                    if(fieldType == DataType.ATTACHMENT){
                        //do not add any value to attachment fields
                        row.add("");
                        continue;
                    }
                    if (expectedHeader.equals(LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER)) {
                        row.add(recordIdValue);
                    } else if (expectedHeader.equals(LiteralConstants.PARQUET_PROVIDER_CODE_COLUMN_HEADER)) {
                        row.add((importFileInDremioInfo.getDataProviderCode() != null) ? importFileInDremioInfo.getDataProviderCode() : "");
                    } else {
                        if(csvRecord.isMapped(expectedHeader)){
                            row.add(csvRecord.get(expectedHeader));
                        }
                        else{
                            //check if header contains bom
                            String headerWithBom = "\uFEFF" + expectedHeader;
                            if(csvRecord.isMapped(headerWithBom)){
                                row.add(csvRecord.get(headerWithBom));
                            }
                            else {
                                row.add("");
                            }
                        }

                    }
                }
                csvPrinter.printRecord(row);
                recordCounter++;
                if(recordCounter == maxCsvLinesPerFile){
                    csvPrinter.flush();
                    writer.close();
                    recordCounter = 0;
                    //add file to list
                    modifiedCsvFiles.add(csvFileWithAddedColumns);
                }
            }
            if(recordCounter != 0){
                csvPrinter.flush();
                writer.close();
                //add file to list
                modifiedCsvFiles.add(csvFileWithAddedColumns);
            }
            if(emptyFile){
                LOG.info("For job {} file {} contains only headers", importFileInDremioInfo, csvFile.getName());
                return null;
            }
        } catch (IOException | UncheckedIOException e) {
            if(e.getClass().equals(UncheckedIOException.class) && ((UncheckedIOException) e).getMessage().contains("invalid char between encapsulated token and delimiter")){
                Integer lineNumber = extractCsvErrorLineNumber(((UncheckedIOException) e).getMessage());
                if(lineNumber != -1){
                    jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.ERROR_CSV_MULTIPLE_QUOTES_WITH_LINE_NUM, lineNumber);
                }
                else{
                    jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.ERROR_CSV_MULTIPLE_QUOTES, null);
                }
            }
            else if (e.getClass().equals(MalformedInputException.class)){
                jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.ERROR_CSV_ILLEGAL_CHARACTERS, null);
            }
            LOG.error("Could not read csv file {}. {}", csvFile.getPath(), importFileInDremioInfo);
            throw new Exception("Could not read csv file " + csvFile.getPath());
        }

        return modifiedCsvFiles;
    }

    private S3PathResolver constructS3PathResolver(ImportFileInDremioInfo importFileInDremioInfo, String fileName, String tableSchemaName, String path){
        Long providerId = importFileInDremioInfo.getProviderId() != null ? importFileInDremioInfo.getProviderId() : 0L;
        S3PathResolver s3PathResolver = new S3PathResolver(importFileInDremioInfo.getDataflowId(), providerId, importFileInDremioInfo.getDatasetId(), tableSchemaName, fileName, path);
        return s3PathResolver;
    }

    private String getImportPathForCsv(ImportFileInDremioInfo importFileInDremioInfo, String fileName, String tableSchemaName) throws Exception {
        S3PathResolver s3PathResolver = constructS3PathResolver(importFileInDremioInfo, fileName, tableSchemaName, S3_IMPORT_FILE_PATH);
        String pathToS3ForImport = null;
        pathToS3ForImport = s3Service.getS3Path(s3PathResolver);
        if(StringUtils.isBlank(pathToS3ForImport)){
            LOG.error("Could not resolve path to s3 for import {}", importFileInDremioInfo);
            throw new Exception("Could not resolve path to s3 for import");
        }
        return pathToS3ForImport;
    }

    private String getImportQueryPathForFolder(ImportFileInDremioInfo importFileInDremioInfo, String fileName, String tableSchemaName, String pathConstant) throws Exception {
        S3PathResolver s3PathResolver = constructS3PathResolver(importFileInDremioInfo, fileName, tableSchemaName, pathConstant);
        String pathToS3ForImport = null;
        if(pathConstant.equals(LiteralConstants.S3_TABLE_NAME_QUERY_PATH) || pathConstant.equals(LiteralConstants.S3_IMPORT_CSV_FILE_QUERY_PATH)){
            pathToS3ForImport = s3Service.getS3Path(s3PathResolver);
        }
        else{
            pathToS3ForImport = s3Service.getTableAsFolderQueryPath(s3PathResolver, pathConstant);
        }
        if(StringUtils.isBlank(pathToS3ForImport)){
            LOG.error("Could not resolve path to s3 for import for job {}", importFileInDremioInfo);
            throw new Exception("Could not resolve path to s3 for import");
        }
        return pathToS3ForImport;
    }

    private void convertCsvToParquetCustom(File csvFile, String parquetFilePath, ImportFileInDremioInfo importFileInDremioInfo) throws Exception {

        LOG.info("For job {} converting csv file {} to parquet file {}", importFileInDremioInfo, csvFile.getPath(), parquetFilePath);
        char delimiterChar = LiteralConstants.COMMA.charAt(0);

        // Check that the parquet file exists, if so delete it
        if (Files.exists(Paths.get(parquetFilePath))) {
            try {
                Files.delete(Paths.get(parquetFilePath));
            } catch (IOException e) {
                LOG.error("Could not delete folder for file {}. {}", parquetFilePath, importFileInDremioInfo);
                throw new Exception("Could not delete folder for file " + parquetFilePath);
            }
        }

        List<String> csvHeaders = new ArrayList<>();
        List<List<String>> data = new ArrayList<>();
        //Reading csv file
        try (
                Reader reader = Files.newBufferedReader(Paths.get(csvFile.getPath()));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(false)
                        .setDelimiter(delimiterChar)
                        .setIgnoreHeaderCase(true)
                        .setTrim(true).build())) {
            csvHeaders.addAll(csvParser.getHeaderMap().keySet());

            for (CSVRecord csvRecord : csvParser) {
                if(csvRecord.values().length == 0){
                    LOG.error("Empty first line in csv file {}. {}", csvFile.getPath(), importFileInDremioInfo);
                    throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE);
                }
                List<String> row = new ArrayList<>();
                for (String csvHeader : csvHeaders) {
                    row.add(csvRecord.get(csvHeader));
                }
                data.add(row);
            }
        } catch (IOException e) {
            LOG.error("Could not read csv file {}. {}", csvFile.getPath(), importFileInDremioInfo);
            throw new Exception("Could not read csv file " + csvFile.getPath());
        }
        //Defining schema
        List<Schema.Field> fields = new ArrayList<>();
        for (String csvHeader : csvHeaders) {
            fields.add(new Schema.Field(csvHeader, Schema.create(Schema.Type.STRING), null, null));
        }
        Schema schema = Schema.createRecord("Data", null, null, false, fields);

        // Write all data to a single Parquet file
        try (ParquetWriter<GenericRecord> writer = AvroParquetWriter
                .<GenericRecord>builder(new Path(parquetFilePath))
                .withSchema(schema)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withPageSize(4 * 1024)
                .withRowGroupSize(16 * 1024)
                .build()) {
            for (List<String> row : data) {
                GenericRecord record = new GenericData.Record(schema);
                for (int i = 0; i < csvHeaders.size(); i++) {
                    record.put(csvHeaders.get(i), row.get(i));
                }
                writer.write(record);
            }
        } catch (IOException e) {
            LOG.error("Could not write in parquet file {}. {}", parquetFilePath, importFileInDremioInfo);
            throw new Exception("Could not write in parquet file " + parquetFilePath);
        }
        LOG.info("Finished writing to Parquet file: {}. {}", parquetFilePath, importFileInDremioInfo);
    }

    private List<String> checkIfCSVHeadersAreCorrect(List<String> csvHeaders, DataSetSchema dataSetSchema, ImportFileInDremioInfo importFileInDremioInfo, String csvFileName, String tableSchemaId) throws EEAException {
        boolean atLeastOneFieldSchema = false;

        for (String csvHeader : csvHeaders) {
            if (csvHeader.startsWith("\uFEFF")){
                //remove BOM
                csvHeader = csvHeader.replace("\uFEFF", "");
            }
            if (tableSchemaId != null) {
                final FieldSchema fieldSchema = fileCommonUtils.findIdFieldSchema(csvHeader, tableSchemaId, dataSetSchema);
                if (fieldSchema != null) {
                    atLeastOneFieldSchema = true;
                    break;
                }
            }
        }

        if (!atLeastOneFieldSchema) {
            LOG.error("Error parsing CSV file. No headers matching FieldSchemas: {}. expectedHeaders={}, actualHeaders={}",
                    importFileInDremioInfo, getFieldNames(tableSchemaId, dataSetSchema, csvFileName), csvHeaders);
            importFileInDremioInfo.setErrorMessage(EEAErrorMessage.ERROR_FILE_NO_HEADERS_MATCHING);
            throw new EEAException(EEAErrorMessage.ERROR_FILE_NO_HEADERS_MATCHING);
        }

        return getFieldNames(tableSchemaId, dataSetSchema, csvFileName);
    }

    /**
     * Gets the field names.
     *
     * @param tableSchemaId the table schema id
     * @param dataSetSchema the data set schema
     * @param fileName the fileName
     * @return the field names
     */
    private List<String> getFieldNames(String tableSchemaId, DataSetSchema dataSetSchema, String fileName) throws EEAException {
        if (StringUtils.isBlank(tableSchemaId)) {
            tableSchemaId = fileTreatmentHelper.getTableSchemaIdFromFileName(dataSetSchema, fileName, false);
        }
        List<String> fieldNames = new ArrayList<>();
        fieldNames.add(PARQUET_RECORD_ID_COLUMN_HEADER);
        fieldNames.add(PARQUET_PROVIDER_CODE_COLUMN_HEADER);

        if (null != tableSchemaId) {
            for (TableSchema tableSchema : dataSetSchema.getTableSchemas()) {
                if (tableSchemaId.equals(tableSchema.getIdTableSchema().toString())) {
                    for (FieldSchema fieldSchema : tableSchema.getRecordSchema().getFieldSchema()) {
                        fieldNames.add(fieldSchema.getHeaderName());
                    }
                    break;
                }
            }
        }

        return fieldNames;
    }

    private Map<String, DataType> getFieldNameAndTypeMap(String tableSchemaId, DataSetSchema dataSetSchema){
        Map<String, DataType> fieldNameAndType = new HashMap<>();
        fieldNameAndType.put(PARQUET_RECORD_ID_COLUMN_HEADER, DataType.TEXT);
        fieldNameAndType.put(PARQUET_PROVIDER_CODE_COLUMN_HEADER, DataType.TEXT);
        for (TableSchema tableSchema : dataSetSchema.getTableSchemas()) {
            if (tableSchemaId.equals(tableSchema.getIdTableSchema().toString())) {
                for (FieldSchema fieldSchema : tableSchema.getRecordSchema().getFieldSchema()) {
                    fieldNameAndType.put(fieldSchema.getHeaderName(), fieldSchema.getType());
                }
                break;
            }
        }
        return fieldNameAndType;
    }

    @Override
    public FileTreatmentHelper getFileTreatmentHelper(){
        return this.fileTreatmentHelper;
    }

    private static Integer extractCsvErrorLineNumber(String errorMessage) {
        Pattern pattern = Pattern.compile("\\(line (\\d+)\\)");
        Matcher matcher = pattern.matcher(errorMessage);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            return -1;
        }
    }
}
