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
import org.eea.dataset.service.ParquetConverterService;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.dataset.service.model.ImportFileInDremioInfo;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.eea.utils.LiteralConstants.*;

@ImportDataLakeCommons
@Service
public class ParquetConverterServiceImpl implements ParquetConverterService {

    private static final Logger LOG = LoggerFactory.getLogger(ParquetConverterServiceImpl.class);

    private final static String MOFIDIED_CSV_SUFFIX = "_modified_%s.csv";
    private final static String CSV_EXTENSION = ".csv";
    private final static String PARQUET_EXTENSION = ".parquet";

    @Value("${loadDataDelimiter}")
    private char defaultDelimiter;

    @Autowired
    private FileCommonUtils fileCommonUtils;

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


    @Override
    public Map<String, String> convertCsvFilesToParquetFiles(ImportFileInDremioInfo importFileInDremioInfo, List<File> csvFiles, DataSetSchema dataSetSchema) throws Exception {
        Map<String, String> parquetFileNamesAndPaths = new HashMap<>();
        for(File csvFile: csvFiles){
            String tableSchemaName = csvFile.getName().replace(CSV_EXTENSION, "");
            String parquetFilePath = csvFile.getPath().replace(CSV_EXTENSION, PARQUET_EXTENSION);
            String parquetFileName = csvFile.getName().replace(CSV_EXTENSION, PARQUET_EXTENSION);
            String randomStrForNewFolderSuffix = UUID.randomUUID().toString();
            convertCsvToParquetViaDremio(csvFile, dataSetSchema, importFileInDremioInfo, tableSchemaName, randomStrForNewFolderSuffix);

            parquetFileNamesAndPaths.put(parquetFileName, parquetFilePath);
        }
        return parquetFileNamesAndPaths;
    }

    private void convertCsvToParquetViaDremio(File csvFile, DataSetSchema dataSetSchema, ImportFileInDremioInfo importFileInDremioInfo,
                                              String tableSchemaName, String randomStrForNewFolderSuffix) throws Exception {
        LOG.info("For job {} converting csv file {} to parquet file {}", importFileInDremioInfo, csvFile.getPath());
        //create a new csv file that contains records ids and data provider code as extra information
        File csvFileWithAddedColumns = modifyCsvFile(csvFile, dataSetSchema, importFileInDremioInfo, randomStrForNewFolderSuffix);
        S3PathResolver s3PathResolver = constructS3PathResolver(importFileInDremioInfo, tableSchemaName, tableSchemaName);

        //path in s3 for the stored csv file
        String s3PathForModifiedCsv = getImportPathForCsv(importFileInDremioInfo, csvFileWithAddedColumns.getName(), tableSchemaName);
        //path in s3 for the folder that contains the stored csv files
        String s3PathForCsvFolder = s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH);
        //path in dremio for the stored csv file
        String dremioPathForCsvFile = getImportQueryPathForFolder(importFileInDremioInfo, csvFileWithAddedColumns.getName(), tableSchemaName, LiteralConstants.S3_IMPORT_CSV_FILE_QUERY_PATH);
        //path in dremio for the folder in current that represents the table of the dataset
        String dremioPathForParquetFolder = getImportQueryPathForFolder(importFileInDremioInfo, tableSchemaName, tableSchemaName, LiteralConstants.S3_TABLE_AS_FOLDER_QUERY_PATH);
        //path in dremio for the folder that contains the created parquet file
        String uniqueString = UUID.randomUUID().toString();
        String parquetInnerFolderPath = dremioPathForParquetFolder + ".\"" + uniqueString + "\"";

        if(importFileInDremioInfo.getReplaceData()){
            //remove tables and folders that contain the previous parquet files because data will be replaced
            List<ObjectIdentifier> csvFilesInS3 = s3Helper.listObjectsInBucket(s3PathForCsvFolder);
            for(ObjectIdentifier csvFileInS3 : csvFilesInS3){
                String[] csvFileNameSplit = csvFileInS3.key().split("/");
                String csvFileName = csvFileNameSplit[csvFileNameSplit.length - 1];
                //set up temporary s3PathResolver fileName so that the csv file will be promoted
                s3PathResolver.setFilename(csvFileName);
                dremioHelperService.demoteFolderOrFile(s3PathResolver, csvFileName, true);
                //revert s3PathResolver fileName
                s3PathResolver.setFilename(tableSchemaName);
            }
            if (s3Helper.checkFolderExist(s3PathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH)) {
                s3Helper.deleleFolder(s3PathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH);
            }
        }

        //upload modified csv file to s3
        s3Helper.uploadFileToBucket(s3PathForModifiedCsv, csvFileWithAddedColumns.getPath());

        //set up temporary s3PathResolver fileName so that the csv file will be promoted
        s3PathResolver.setFilename(csvFileWithAddedColumns.getName());
        dremioHelperService.promoteFolderOrFile(s3PathResolver, csvFileWithAddedColumns.getName(), true);
        //revert s3PathResolver fileName
        s3PathResolver.setFilename(tableSchemaName);

        //refresh the metadata for the csv table
        String refreshImportTableQuery = "ALTER TABLE " + dremioPathForCsvFile + " REFRESH METADATA";
        dremioJdbcTemplate.execute(refreshImportTableQuery);

        //demote table folder
        dremioHelperService.demoteFolderOrFile(s3PathResolver, tableSchemaName, false);
        if(importFileInDremioInfo.getReplaceData()) {
            //remove folders that contain the previous parquet files because data will be replaced
            if (s3Helper.checkFolderExist(s3PathResolver, S3_TABLE_NAME_FOLDER_PATH)) {
                s3Helper.deleleFolder(s3PathResolver, S3_TABLE_NAME_FOLDER_PATH);
            }
        }

        String createTableQuery = constructCreateTableStatementForParquet(parquetInnerFolderPath, dremioPathForCsvFile);
        dremioJdbcTemplate.execute(createTableQuery);

        //promote folder
        dremioHelperService.promoteFolderOrFile(s3PathResolver, tableSchemaName, false);

        //refresh the metadata
        String refreshTableQuery = "ALTER TABLE " + dremioPathForParquetFolder + " REFRESH METADATA ";
        dremioJdbcTemplate.execute(refreshTableQuery);
        LOG.info("For job {} the import for table {} has been completed", importFileInDremioInfo, tableSchemaName);
    }

    private String constructCreateTableStatementForParquet(String parquetInnerFolderPath, String dremioPathForCsvFile){
        //String createTableQuery = "CREATE TABLE " + parquetInnerFolderPath + " AS SELECT A as " + PARQUET_RECORD_ID_COLUMN_HEADER + ", B as f1, C as f2, D as " + PARQUET_PROVIDER_CODE_COLUMN_HEADER+ " FROM " + dremioPathForCsvFile;
        String createTableQuery = "CREATE TABLE " + parquetInnerFolderPath + " AS SELECT * FROM " + dremioPathForCsvFile;
        return createTableQuery;
    }

    private File modifyCsvFile(File csvFile, DataSetSchema dataSetSchema, ImportFileInDremioInfo importFileInDremioInfo, String randomStrForNewFolderSuffix) throws Exception {
        char delimiterChar = defaultDelimiter;
        if (!StringUtils.isBlank(importFileInDremioInfo.getDelimiter())){
            delimiterChar = importFileInDremioInfo.getDelimiter().charAt(0);
        }

        String modifiedFilePath = csvFile.getPath().replace(".csv", "") + String.format(MOFIDIED_CSV_SUFFIX, randomStrForNewFolderSuffix);
        File csvFileWithAddedColumns = new File(modifiedFilePath);
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvFileWithAddedColumns.getPath()));

        List<String> csvHeaders = new ArrayList<>();
        List<FieldSchema> sanitizedHeaders;
        List<String> expectedHeaders;
        //Reading csv file
        try (
                Reader reader = Files.newBufferedReader(Paths.get(csvFile.getPath()));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                        .withFirstRecordAsHeader().builder()
                        .setDelimiter(delimiterChar)
                        .setIgnoreHeaderCase(true)
                        .setTrim(true).build())) {
            csvHeaders.add(LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER);
            csvHeaders.addAll(csvParser.getHeaderMap().keySet());
            csvHeaders.add(LiteralConstants.PARQUET_PROVIDER_CODE_COLUMN_HEADER);

            checkIfCSVHeadersAreCorrect(csvHeaders, dataSetSchema, importFileInDremioInfo, csvFile.getName());
            expectedHeaders = getFieldNames(importFileInDremioInfo.getTableSchemaId(), dataSetSchema, csvFile.getName());
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setDelimiter(delimiterChar).build());
            int recordCounter = 0;
            for (CSVRecord csvRecord : csvParser) {
                if(csvRecord.values().length == 0){
                    LOG.error("Empty first line in csv file {}. {}", csvFile.getPath(), importFileInDremioInfo);
                    throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE);
                }
                recordCounter++;
                if(recordCounter == 1){
                    //for the first loop, add the headers of the file
                    /*List<String> row = new ArrayList<>();
                    expectedHeaders.stream().forEach(header -> row.add(header));
                    sanitizedHeaders.stream().forEach(header -> row.add(header.getHeaderName()));*/
                    csvPrinter.printRecord(expectedHeaders);
                }
                List<String> row = new ArrayList<>();
                String recordIdValue = UUID.randomUUID().toString();
                for (String expectedHeader : expectedHeaders) {
                    if (expectedHeader.equals(LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER)) {
                        row.add(recordIdValue);
                    } else if (expectedHeader.equals(LiteralConstants.PARQUET_PROVIDER_CODE_COLUMN_HEADER)) {
                        row.add((importFileInDremioInfo.getDataProviderCode() != null) ? importFileInDremioInfo.getDataProviderCode() : "");
                    } else {
                        if(csvRecord.isMapped(expectedHeader)){
                            row.add(csvRecord.get(expectedHeader));
                        }
                        else{
                            row.add("");
                        }

                    }
                }
                csvPrinter.printRecord(row);
            }
            csvPrinter.flush();
        } catch (IOException e) {
            LOG.error("Could not read csv file {}. {}", csvFile.getPath(), importFileInDremioInfo);
            throw new Exception("Could not read csv file " + csvFile.getPath());
        }
        finally {
            writer.close();
        }
        return csvFileWithAddedColumns;
    }

    private S3PathResolver constructS3PathResolver(ImportFileInDremioInfo importFileInDremioInfo, String fileName, String tableSchemaName){
        Long providerId = importFileInDremioInfo.getProviderId() != null ? importFileInDremioInfo.getProviderId() : 0L;
        S3PathResolver s3PathResolver = new S3PathResolver(importFileInDremioInfo.getDataflowId(), providerId, importFileInDremioInfo.getDatasetId(), tableSchemaName, fileName);
        return s3PathResolver;
    }

    private String getImportPathForCsv(ImportFileInDremioInfo importFileInDremioInfo, String fileName, String tableSchemaName) throws Exception {
        S3PathResolver s3PathResolver = constructS3PathResolver(importFileInDremioInfo, fileName, tableSchemaName);
        String pathToS3ForImport = null;
        pathToS3ForImport = s3Service.getImportProviderPath(s3PathResolver);
        if(StringUtils.isBlank(pathToS3ForImport)){
            LOG.error("Could not resolve path to s3 for import {}", importFileInDremioInfo);
            throw new Exception("Could not resolve path to s3 for import");
        }
        return pathToS3ForImport;
    }

    private String getImportQueryPathForFolder(ImportFileInDremioInfo importFileInDremioInfo, String fileName, String tableSchemaName, String pathConstant) throws Exception {
        Long providerId = importFileInDremioInfo.getProviderId() != null ? importFileInDremioInfo.getProviderId() : 0L;
        S3PathResolver s3PathResolver = new S3PathResolver(importFileInDremioInfo.getDataflowId(), providerId, importFileInDremioInfo.getDatasetId(), tableSchemaName, fileName);
        String pathToS3ForImport = null;
        if(pathConstant.equals(LiteralConstants.S3_TABLE_NAME_QUERY_PATH) || pathConstant.equals(LiteralConstants.S3_IMPORT_CSV_FILE_QUERY_PATH)){
            pathToS3ForImport = s3Service.getTableNameProviderQueryPath(s3PathResolver, pathConstant);
        }
        else{
            pathToS3ForImport = s3Service.getTableAsFolderQueryPath(s3PathResolver, pathConstant);
        }
        if(StringUtils.isBlank(pathToS3ForImport)){
            LOG.error("Could not resolve path to s3 for import for providerId {} {}", providerId, importFileInDremioInfo);
            throw new Exception("Could not resolve path to s3 for import");
        }
        return pathToS3ForImport;
    }

    private void convertCsvToParquetCustom(File csvFile, String parquetFilePath, DataSetSchema dataSetSchema, ImportFileInDremioInfo importFileInDremioInfo) throws Exception {

        LOG.info("For job {} converting csv file {} to parquet file {}", importFileInDremioInfo, csvFile.getPath(), parquetFilePath);
        char delimiterChar = defaultDelimiter;
        if (!StringUtils.isBlank(importFileInDremioInfo.getDelimiter())){
            delimiterChar = importFileInDremioInfo.getDelimiter().charAt(0);
        }


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
        List<FieldSchema> sanitizedHeaders;
        //Reading csv file
        try (
                Reader reader = Files.newBufferedReader(Paths.get(csvFile.getPath()));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                        .setDelimiter(delimiterChar)
                        .setSkipHeaderRecord(true)
                        .setIgnoreHeaderCase(true)
                        .setTrim(true).build())) {
            csvHeaders.add(LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER);
            csvHeaders.addAll(csvParser.getHeaderMap().keySet());
            csvHeaders.add(LiteralConstants.PARQUET_PROVIDER_CODE_COLUMN_HEADER);

            sanitizedHeaders = checkIfCSVHeadersAreCorrect(csvHeaders, dataSetSchema, importFileInDremioInfo, csvFile.getName());

            for (CSVRecord csvRecord : csvParser) {
                if(csvRecord.values().length == 0){
                    LOG.error("Empty first line in csv file {}. {}", csvFile.getPath(), importFileInDremioInfo);
                    throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE);
                }

                String recordIdValue = UUID.randomUUID().toString();
                List<String> row = new ArrayList<>();
                for (FieldSchema sanitizedHeader : sanitizedHeaders) {
                    if(sanitizedHeader.getHeaderName().equals(LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER)){
                        row.add(recordIdValue);
                    }
                    else if(sanitizedHeader.getHeaderName().equals(LiteralConstants.PARQUET_PROVIDER_CODE_COLUMN_HEADER)){
                        row.add((importFileInDremioInfo.getDataProviderCode() != null) ? importFileInDremioInfo.getDataProviderCode() : "");
                    }
                    else {
                        row.add(csvRecord.get(sanitizedHeader.getHeaderName()));
                    }
                }
                data.add(row);
            }
        } catch (IOException e) {
            LOG.error("Could not read csv file {}. {}", csvFile.getPath(), importFileInDremioInfo);
            throw new Exception("Could not read csv file " + csvFile.getPath());
        }
        //Defining schema
        List<Schema.Field> fields = new ArrayList<>();
        for (FieldSchema sanitizedHeader : sanitizedHeaders) {
            fields.add(new Schema.Field(sanitizedHeader.getHeaderName(), Schema.create(Schema.Type.STRING), null, null));
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
                for (int i = 0; i < sanitizedHeaders.size(); i++) {
                    record.put(sanitizedHeaders.get(i).getHeaderName(), row.get(i));
                }
                writer.write(record);
            }
        } catch (IOException e) {
            LOG.error("Could not write in parquet file {}. {}", parquetFilePath, importFileInDremioInfo);
            throw new Exception("Could not write in parquet file " + parquetFilePath);
        }
        LOG.info("Finished writing to Parquet file: {}. {}", parquetFilePath, importFileInDremioInfo);
    }

    private List<FieldSchema> checkIfCSVHeadersAreCorrect(List<String> csvHeaders, DataSetSchema dataSetSchema, ImportFileInDremioInfo importFileInDremioInfo, String csvFileName) throws EEAException {
        boolean atLeastOneFieldSchema = false;
        List<FieldSchema> headers = new ArrayList<>();

        String tableSchemaId = importFileInDremioInfo.getTableSchemaId();
        if (StringUtils.isBlank(tableSchemaId)) {
            tableSchemaId = fileTreatmentHelper.getTableSchemaIdFromFileName(dataSetSchema, csvFileName);
        }

        for (String csvHeader : csvHeaders) {
            FieldSchema header = new FieldSchema();
            if (tableSchemaId != null) {
                final FieldSchema fieldSchema = fileCommonUtils.findIdFieldSchema(csvHeader, tableSchemaId, dataSetSchema);
                if (fieldSchema != null) {
                    atLeastOneFieldSchema = true;
                    header.setIdFieldSchema(fieldSchema.getIdFieldSchema());
                    header.setType(fieldSchema.getType());
                    header.setReadOnly(
                            fieldSchema.getReadOnly() == null ? Boolean.FALSE : fieldSchema.getReadOnly());
                    header.setHeaderName(csvHeader);
                    headers.add(header);
                }
                else if(csvHeader.equals(LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER) || csvHeader.equals(PARQUET_PROVIDER_CODE_COLUMN_HEADER)){
                    header.setReadOnly(Boolean.TRUE);
                    header.setHeaderName(csvHeader);
                    headers.add(header);
                }
            }
        }

        if (!atLeastOneFieldSchema) {
            LOG.error("Error parsing CSV file. No headers matching FieldSchemas: {}. expectedHeaders={}, actualHeaders={}",
                    importFileInDremioInfo, getFieldNames(tableSchemaId, dataSetSchema, csvFileName), csvHeaders);
            importFileInDremioInfo.setErrorMessage(EEAErrorMessage.ERROR_FILE_NO_HEADERS_MATCHING);
            throw new EEAException(EEAErrorMessage.ERROR_FILE_NO_HEADERS_MATCHING);
        }
        return headers;
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
            tableSchemaId = fileTreatmentHelper.getTableSchemaIdFromFileName(dataSetSchema, fileName);
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
}
