package org.eea.dataset.service.impl;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.SpatialDataHandling;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.impl.S3ServiceImpl;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.configuration.util.CsvHeaderMapping;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.service.*;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.dataset.service.model.FileWithRecordNum;
import org.eea.dataset.service.model.ImportFileInDremioInfo;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaIdNameVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.eea.utils.LiteralConstants.*;

@ImportDataLakeCommons
@Service
public class ParquetConverterServiceImpl implements ParquetConverterService {

  private static final Logger LOG = LoggerFactory.getLogger(ParquetConverterServiceImpl.class);

  @Value("${importPath}")
  private String importPath;

  private final static String MODIFIED_CSV_SUFFIX = "_%s.csv";
  private final static String CSV_EXTENSION = ".csv";
  private final static String PARQUET_EXTENSION = ".parquet";
  private final static String DEFAULT_PARQUET_NAME = "0_0_0.parquet";

  private final static String DEFAULT_TXT_NAME = "0_0_0.txt";

  public static final String MEASUREMENTS = "MEASUREMENTS";

  @Value("${loadDataDelimiter}")
  private char defaultDelimiter;

  @Value("${dremio.parquetConverter.custom}")
  private Boolean convertParquetWithCustomWay;

  @Value("${dremio.parquetConverter.custom.maxCsvLinesPerFile}")
  private Integer maxCsvLinesPerFile;

  private final FileCommonUtils fileCommonUtils;
  private final FileTreatmentHelper fileTreatmentHelper;
  private final DremioHelperService dremioHelperService;
  private final S3ServiceImpl s3Service;
  private final S3Helper s3Helper;
  private final JobControllerZuul jobControllerZuul;
  private final DatasetMetabaseService datasetMetabaseService;
  private final DatasetSchemaService datasetSchemaService;
  private final SpatialDataHandling spatialDataHandling;

  @Lazy
  @Autowired
  private BigDataDatasetService bigDataDatasetService;

  private JdbcTemplate dremioJdbcTemplate;
  public ParquetConverterServiceImpl(FileCommonUtils fileCommonUtils,
                                     DremioHelperService dremioHelperService,
                                     S3ServiceImpl s3Service,
                                     S3Helper s3Helper,
                                     JobControllerZuul jobControllerZuul,
                                     DatasetMetabaseService datasetMetabaseService,
                                     DatasetSchemaService datasetSchemaService,
                                     SpatialDataHandling spatialDataHandling,
                                     @Lazy FileTreatmentHelper fileTreatmentHelper,
                                     JdbcTemplate dremioJdbcTemplate) {
    this.fileCommonUtils = fileCommonUtils;
    this.dremioHelperService = dremioHelperService;
    this.s3Service = s3Service;
    this.s3Helper = s3Helper;
    this.jobControllerZuul = jobControllerZuul;
    this.datasetMetabaseService = datasetMetabaseService;
    this.datasetSchemaService = datasetSchemaService;
    this.spatialDataHandling = spatialDataHandling;
    this.fileTreatmentHelper = fileTreatmentHelper;
    this.dremioJdbcTemplate = dremioJdbcTemplate;
  }

  @Override
  public void convertCsvFilesToParquetFiles(ImportFileInDremioInfo importFileInDremioInfo, List<File> csvFiles, DataSetSchema dataSetSchema) throws Exception {
    String tableSchemaName;
    int numberOfEmptyFiles = 0;
    int numberOfFailedImportsForFixedNumberOfRecordsWithoutReplace = 0;
    int numberOfFailedImportsForWrongNumberOfRecords = 0;
    int numberOfFailedImportsForOnlyReadOnlyFields = 0;
    for (File csvFile : csvFiles) {
      if (StringUtils.isNotBlank(importFileInDremioInfo.getTableSchemaId())) {
        tableSchemaName = fileCommonUtils.getTableName(importFileInDremioInfo.getTableSchemaId(), dataSetSchema);
      } else {
        tableSchemaName = csvFile.getName().replace(CSV_EXTENSION, "");
      }
      convertCsvToParquet(csvFile, dataSetSchema, importFileInDremioInfo, tableSchemaName);
      if (StringUtils.isNotBlank(importFileInDremioInfo.getWarningMessage())) {
        if (importFileInDremioInfo.getWarningMessage().equals(JobInfoEnum.WARNING_SOME_FILES_ARE_EMPTY.getValue(null))) {
          numberOfEmptyFiles++;
        }
        else if (importFileInDremioInfo.getWarningMessage().equals(JobInfoEnum.WARNING_SOME_IMPORT_FAILED_FIXED_NUM_WITHOUT_REPLACE_DATA.getValue(null))){
          numberOfFailedImportsForFixedNumberOfRecordsWithoutReplace++;
        }
        else if (importFileInDremioInfo.getWarningMessage().equals(JobInfoEnum.WARNING_SOME_IMPORT_FAILED_WRONG_NUM_OF_RECORDS.getValue(null))){
          numberOfFailedImportsForWrongNumberOfRecords++;
        }
        else if (importFileInDremioInfo.getWarningMessage().equals(JobInfoEnum.WARNING_SOME_IMPORT_FAILED_ONLY_READ_ONLY_FIELDS.getValue(null))){
          numberOfFailedImportsForOnlyReadOnlyFields++;
        }
      }
    }

    //check if all imports failed and an error (instead of a warning) must be thrown
    if (numberOfEmptyFiles != 0) {
      if (numberOfEmptyFiles == csvFiles.size()) {
        importFileInDremioInfo.setWarningMessage(null);
        importFileInDremioInfo.setErrorMessage(EEAErrorMessage.ERROR_IMPORT_EMPTY_FILES);
        throw new Exception(EEAErrorMessage.ERROR_IMPORT_EMPTY_FILES);
      } else {
        importFileInDremioInfo.setWarningMessage(JobInfoEnum.WARNING_SOME_FILES_ARE_EMPTY.getValue(null));
      }
    }
    if (numberOfFailedImportsForFixedNumberOfRecordsWithoutReplace != 0) {
      if (numberOfFailedImportsForFixedNumberOfRecordsWithoutReplace == csvFiles.size()) {
        //all imports failed and an error (instead of a warning) must be thrown
        importFileInDremioInfo.setWarningMessage(null);
        importFileInDremioInfo.setErrorMessage(EEAErrorMessage.ERROR_IMPORT_FAILED_FIXED_NUM_WITHOUT_REPLACE_DATA);
        throw new Exception(EEAErrorMessage.ERROR_IMPORT_FAILED_FIXED_NUM_WITHOUT_REPLACE_DATA);
      } else {
        importFileInDremioInfo.setWarningMessage(JobInfoEnum.WARNING_SOME_IMPORT_FAILED_FIXED_NUM_WITHOUT_REPLACE_DATA.getValue(null));
      }
    }

    if (numberOfFailedImportsForWrongNumberOfRecords != 0) {
      if (numberOfFailedImportsForWrongNumberOfRecords == csvFiles.size()) {
        //all imports failed and an error (instead of a warning) must be thrown
        importFileInDremioInfo.setWarningMessage(null);
        importFileInDremioInfo.setErrorMessage(EEAErrorMessage.ERROR_IMPORT_FAILED_WRONG_NUM_OF_RECORDS);
        throw new Exception(EEAErrorMessage.ERROR_IMPORT_FAILED_WRONG_NUM_OF_RECORDS);
      } else {
        importFileInDremioInfo.setWarningMessage(JobInfoEnum.WARNING_SOME_IMPORT_FAILED_WRONG_NUM_OF_RECORDS.getValue(null));
      }
    }

    if (numberOfFailedImportsForOnlyReadOnlyFields != 0) {
      if (numberOfFailedImportsForOnlyReadOnlyFields == csvFiles.size()) {
        importFileInDremioInfo.setWarningMessage(null);
        importFileInDremioInfo.setErrorMessage(EEAErrorMessage.ERROR_IMPORT_FAILED_ONLY_READ_ONLY_FIELDS);
        throw new Exception(EEAErrorMessage.ERROR_IMPORT_FAILED_ONLY_READ_ONLY_FIELDS);
      } else {
        importFileInDremioInfo.setWarningMessage(JobInfoEnum.WARNING_SOME_IMPORT_FAILED_ONLY_READ_ONLY_FIELDS.getValue(null));
      }
    }
  }

  private void convertCsvToParquet(File csvFile, DataSetSchema dataSetSchema, ImportFileInDremioInfo importFileInDremioInfo,
                                   String tableSchemaName) throws Exception {
    LOG.info("For job {} converting csv file {} to parquet file", importFileInDremioInfo, csvFile.getPath());
    DatasetTypeEnum datasetType = datasetMetabaseService.getDatasetType(importFileInDremioInfo.getDatasetId());
    //create a new csv file that contains records ids and data provider code as extra information
    List<FileWithRecordNum> csvFilesWithAddedColumns;
    TableSchemaVO tableSchemaVO = getTableSchemaVO(csvFile.getName(), dataSetSchema, importFileInDremioInfo);

    S3PathResolver s3ImportPathResolver = constructS3PathResolver(importFileInDremioInfo, tableSchemaName, tableSchemaName, S3_IMPORT_FILE_PATH);
    S3PathResolver s3TablePathResolver = constructS3PathResolver(importFileInDremioInfo, tableSchemaName, tableSchemaName, S3_TABLE_NAME_FOLDER_PATH);

    //path in dremio for the folder in current that represents the table of the dataset
    String dremioPathForParquetFolder = getImportQueryPathForFolder(importFileInDremioInfo, tableSchemaName, tableSchemaName, LiteralConstants.S3_TABLE_AS_FOLDER_QUERY_PATH);
    //path in s3 for the folder that contains the stored csv files
    String s3PathForCsvFolder = s3Service.getTableAsFolderQueryPath(s3ImportPathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH);
    datasetType = DatasetTypeEnum.REPORTING;
    if (!DatasetTypeEnum.DESIGN.equals(datasetType) && tableSchemaVO.getRecordSchema().getFieldSchema().stream().allMatch(FieldSchemaVO::getReadOnly)) {
      importFileInDremioInfo.setWarningMessage(JobInfoEnum.WARNING_SOME_IMPORT_FAILED_ONLY_READ_ONLY_FIELDS.getValue(null));
      return;
    }

    Boolean readOnlyFieldsExist = tableSchemaVO.getRecordSchema().getFieldSchema().stream().anyMatch(FieldSchemaVO::getReadOnly);
    if(!DatasetTypeEnum.DESIGN.equals(datasetType) && readOnlyFieldsExist && importFileInDremioInfo.getReplaceData()){
      //convert old table to iceberg
      Long providerId = (importFileInDremioInfo.getProviderId() != null) ? importFileInDremioInfo.getProviderId() : 0L;
      bigDataDatasetService.convertParquetToIcebergTable(importFileInDremioInfo.getDatasetId(), importFileInDremioInfo.getDataflowId(), providerId, tableSchemaVO, dataSetSchema.getIdDataSetSchema().toString());
      S3PathResolver s3IcebergTablePathResolver = new S3PathResolver(importFileInDremioInfo.getDataflowId(), providerId, importFileInDremioInfo.getDatasetId(), tableSchemaVO.getNameTableSchema(), tableSchemaVO.getNameTableSchema(), S3_TABLE_AS_FOLDER_QUERY_PATH);
      s3IcebergTablePathResolver.setIsIcebergTable(true);
      try {
        updatePrefilledDataBasedOnReadOnlyData(importFileInDremioInfo, csvFile, s3IcebergTablePathResolver, dataSetSchema, datasetType);
      }
      finally {
        //after all updates convert iceberg to parquet
        // todo using a function to convert hex data to binary
        bigDataDatasetService.convertIcebergToParquetTable(importFileInDremioInfo.getDatasetId(), importFileInDremioInfo.getDataflowId(), providerId, tableSchemaVO, dataSetSchema.getIdDataSetSchema().toString());
      }
      return;
    }


    if (convertParquetWithCustomWay) {
      csvFilesWithAddedColumns = modifyAndSplitCsvFile(csvFile, dataSetSchema, importFileInDremioInfo, maxCsvLinesPerFile, datasetType);
    } else {
      if (spatialDataHandling.geoJsonHeadersAreNotEmpty(tableSchemaVO)) {
        csvFilesWithAddedColumns = modifyAndSplitCsvFile(csvFile, dataSetSchema, importFileInDremioInfo, 5000, datasetType);
      } else {
        csvFilesWithAddedColumns = modifyCsvFile(csvFile, dataSetSchema, importFileInDremioInfo, datasetType);
      }
    }

    if (csvFilesWithAddedColumns == null) {
      importFileInDremioInfo.setWarningMessage(JobInfoEnum.WARNING_SOME_FILES_ARE_EMPTY.getValue(null));
      return;
    } else {
      importFileInDremioInfo.setWarningMessage(null);
    }

    Long numberOfRecordsToBeInserted = csvFilesWithAddedColumns.stream().mapToLong(FileWithRecordNum::getNumberOfRecords).sum();

    if (!DatasetTypeEnum.DESIGN.equals(datasetType) && Boolean.TRUE.equals(tableSchemaVO.getFixedNumber())) {
      if(!canImportForFixedNumberOfRecords(importFileInDremioInfo, numberOfRecordsToBeInserted, dremioPathForParquetFolder, s3TablePathResolver)){
        return;
      }
    }

    if (importFileInDremioInfo.getReplaceData()) {
      //remove tables and folders that contain the previous csv files because data will be replaced
      LOG.info("Removing csv files for job {}", importFileInDremioInfo);
      removeCsvFilesThatWillBeReplaced(s3ImportPathResolver, tableSchemaName, s3PathForCsvFolder);

      //delete attachments if they exist
      if (s3Helper.checkFolderExist(s3TablePathResolver, S3_ATTACHMENTS_TABLE_PATH)) {
        s3Helper.deleteFolder(s3TablePathResolver, S3_ATTACHMENTS_TABLE_PATH);
      }
    }

    boolean needToDemoteTable = true;

    for (FileWithRecordNum entry : csvFilesWithAddedColumns) {
      File csvFileWithAddedColumns = entry.getFile();
      String csvFileName = csvFileWithAddedColumns.getName().replace(CSV_EXTENSION, "");

      //path in s3 for the stored csv file
      String s3PathForModifiedCsv = getImportPathForCsv(importFileInDremioInfo, csvFileWithAddedColumns.getName(), tableSchemaName);

      //path in dremio for the stored csv file
      String dremioPathForCsvFile = getImportQueryPathForFolder(importFileInDremioInfo, csvFileWithAddedColumns.getName(), tableSchemaName, LiteralConstants.S3_IMPORT_CSV_FILE_QUERY_PATH);
      //path in dremio for the folder that contains the created parquet file
      String parquetInnerFolderPath = dremioPathForParquetFolder + ".\"" + csvFileName + "\"";

      //upload csv file
      uploadCsvFileAndPromoteIt(s3ImportPathResolver, tableSchemaName, s3PathForModifiedCsv, csvFileWithAddedColumns.getPath(), csvFileWithAddedColumns.getName(), dremioPathForCsvFile);

      if (needToDemoteTable) {
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
        LOG.info("For import job {} " +
            "the conversion of the csv to parquet will use a dremio query", importFileInDremioInfo);
        String createTableQuery = getTableQuery(importFileInDremioInfo, parquetInnerFolderPath, dremioPathForCsvFile, csvFile.getName(), dataSetSchema);
        String processId = dremioHelperService.executeSqlStatement(createTableQuery);
        dremioHelperService.checkIfDremioProcessFinishedSuccessfully(createTableQuery, processId, null);
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

  private TableSchemaVO getTableSchemaVO(String csvFileName, DataSetSchema dataSetSchema, ImportFileInDremioInfo importFileInDremioInfo) throws EEAException {
    String tableSchemaId = importFileInDremioInfo.getTableSchemaId();
    if (StringUtils.isBlank(tableSchemaId)) {
      tableSchemaId = fileTreatmentHelper.getTableSchemaIdFromFileName(dataSetSchema, csvFileName, false);
    }
    DataSetMetabaseVO dataset = datasetMetabaseService.findDatasetMetabase(importFileInDremioInfo.getDatasetId());
    String datasetSchemaId = dataset.getDatasetSchema();
    return datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);
  }

  private String getTableQuery(ImportFileInDremioInfo importFileInDremioInfo, String parquetInnerFolderPath, String dremioPathForCsvFile, String csvFileName, DataSetSchema dataSetSchema) throws EEAException {
    TableSchemaVO tableSchemaVO = getTableSchemaVO(csvFileName, dataSetSchema, importFileInDremioInfo);
    String createTableQuery;
    if (spatialDataHandling.geoJsonHeadersAreNotEmpty(tableSchemaVO)) {
      String initQuery = "CREATE TABLE " + parquetInnerFolderPath + " AS SELECT %s %s FROM " + dremioPathForCsvFile;
      createTableQuery = String.format(initQuery, spatialDataHandling.getSimpleHeaders(tableSchemaVO), spatialDataHandling.getHeadersConvertedToBinary(tableSchemaVO));
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
    if (s3Helper.checkFolderExist(s3ReferenceTablePathResolver, S3_DATAFLOW_REFERENCE_FOLDER_PATH)) {
      dremioHelperService.demoteFolderOrFile(s3ReferenceTablePathResolver, tableSchemaName);
      if (importFileInDremioInfo.getReplaceData()) {
        //remove folders that contain the previous parquet files because data will be replaced
        s3Helper.deleteFolder(s3ReferenceTablePathResolver, S3_DATAFLOW_REFERENCE_FOLDER_PATH);
      }
    }

    tableNameFilenames.forEach(file -> {
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
      } catch (Exception e) {
        LOG.error("Error copying items to reference folder for import job {} ", importFileInDremioInfo, e);
      }
    });

    if (fileCounter.get() != tableNameFilenames.size()) {
      throw new Exception("Error copying items to reference folder for import job " + importFileInDremioInfo.getJobId());
    }
    LOG.info("For job {} the REFERENCE dataset files have been successfully copied to the reference folder", importFileInDremioInfo);

  }

  private String getImportS3PathForParquet(ImportFileInDremioInfo importFileInDremioInfo, String fileName, String tableSchemaName) throws Exception {
    S3PathResolver s3PathResolver = constructS3PathResolver(importFileInDremioInfo, fileName, tableSchemaName, S3_TABLE_NAME_PATH);
    String pathToS3ForImport = s3Service.getS3Path(s3PathResolver);
    if (StringUtils.isBlank(pathToS3ForImport)) {
      LOG.error("Could not resolve path to s3 for import for job {}", importFileInDremioInfo);
      throw new Exception("Could not resolve path to s3 for import");
    }
    return pathToS3ForImport;
  }

  private void uploadCsvFileAndPromoteIt(S3PathResolver s3PathResolver, String tableSchemaName, String s3PathForModifiedCsv, String csvFilePath, String csvFileName, String dremioPathForCsvFile) {
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
  public void removeCsvFilesThatWillBeReplaced(S3PathResolver s3PathResolver, String tableSchemaName, String s3PathForCsvFolder) {
    List<ObjectIdentifier> csvFilesInS3 = s3Helper.listObjectsInBucket(s3PathForCsvFolder);
    for (ObjectIdentifier csvFileInS3 : csvFilesInS3) {
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

  private List<FileWithRecordNum> modifyCsvFile(File csvFile, DataSetSchema dataSetSchema,
                                                ImportFileInDremioInfo importFileInDremioInfo, DatasetTypeEnum datasetType) throws Exception {
    LOG.info(MEASUREMENTS + " with job {} modifyCsvFile started", importFileInDremioInfo);
    char delimiterChar = !StringUtils.isBlank(importFileInDremioInfo.getDelimiter()) ?
        importFileInDremioInfo.getDelimiter().charAt(0) : defaultDelimiter;

    File csvFileWithAddedColumns = createNewFilePath(csvFile);
    List<FileWithRecordNum> modifiedCsvFiles = new ArrayList<>();
    long recordCounter = 0;

    try (Reader reader = Files.newBufferedReader(Paths.get(csvFile.getPath()));
         CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
             .setHeader()
             .setSkipHeaderRecord(false)
             .setDelimiter(delimiterChar)
             .setIgnoreHeaderCase(true)
             .setIgnoreEmptyLines(false)
             .setTrim(true).build());
         BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvFileWithAddedColumns.getPath()));
         CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setDelimiter(LiteralConstants.COMMA).build())) {

      CsvHeaderMapping typeMapping = getHeaderTypeMapping(csvFile, dataSetSchema, importFileInDremioInfo, csvParser);

      for (CSVRecord csvRecord : csvParser) {
        checkForEmptyValues(csvRecord, "Empty first line in CSV file {}. {}", csvFile, importFileInDremioInfo);
        recordCounter++;
        if (recordCounter == 1) {
          csvPrinter.printRecord(typeMapping.getExpectedHeaders().stream().map(FieldSchema::getHeaderName).collect(Collectors.toList()));
        }

        List<String> row = generateRow(csvRecord, typeMapping.getExpectedHeaders(), typeMapping.getFieldNameAndTypeMap(), importFileInDremioInfo, datasetType);
        csvPrinter.printRecord(row);
      }

      csvPrinter.flush();
      modifiedCsvFiles.add(new FileWithRecordNum(csvFileWithAddedColumns, recordCounter));
    } catch (IOException | UncheckedIOException e) {
      handleCsvProcessingError(e, csvFile, importFileInDremioInfo);
    }

    if (recordCounter == 0) {
      LOG.info("For job {} file {} contains only headers", importFileInDremioInfo, csvFile.getName());
      return null;
    }
    LOG.info(MEASUREMENTS + " with job {} modifyCsvFile finished", importFileInDremioInfo);
    return modifiedCsvFiles;
  }

  private List<FileWithRecordNum> modifyAndSplitCsvFile(File csvFile, DataSetSchema dataSetSchema,
                                                        ImportFileInDremioInfo importFileInDremioInfo, Integer batchSize, DatasetTypeEnum datasetType) throws Exception {
    LOG.info(MEASUREMENTS + " with job {} modifyAndSplitCsvFile started", importFileInDremioInfo);
    char delimiterChar = !StringUtils.isBlank(importFileInDremioInfo.getDelimiter()) ?
        importFileInDremioInfo.getDelimiter().charAt(0) : defaultDelimiter;

    List<FileWithRecordNum> modifiedCsvFiles = new ArrayList<>();
    long recordCounter = 0;
    boolean fileIsEmpty = true;

    BufferedWriter writer = null;
    CSVPrinter csvPrinter = null;
    File csvFileWithAddedColumns = null;

    try (Reader reader = Files.newBufferedReader(Paths.get(csvFile.getPath()));
         CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
             .setHeader()
             .setSkipHeaderRecord(false)
             .setDelimiter(delimiterChar)
             .setIgnoreHeaderCase(true)
             .setIgnoreEmptyLines(false)
             .setTrim(true).build())) {

      CsvHeaderMapping typeMapping = getHeaderTypeMapping(csvFile, dataSetSchema, importFileInDremioInfo, csvParser);

      for (CSVRecord csvRecord : csvParser) {
        fileIsEmpty = false;
        checkForEmptyValues(csvRecord, "Empty first line in csv file {}. {}", csvFile, importFileInDremioInfo);

        if (recordCounter == 0) {
          csvFileWithAddedColumns = createNewFilePath(csvFile);
          writer = Files.newBufferedWriter(Paths.get(csvFileWithAddedColumns.getPath()));
          csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setDelimiter(LiteralConstants.COMMA).build());
          csvPrinter.printRecord(typeMapping.getExpectedHeaders().stream().map(FieldSchema::getHeaderName).collect(Collectors.toList()));
        }

        List<String> row = generateRow(csvRecord, typeMapping.getExpectedHeaders(), typeMapping.getFieldNameAndTypeMap(), importFileInDremioInfo, datasetType);
        csvPrinter.printRecord(row);
        recordCounter++;

        if (recordCounter == batchSize) {
          closeResources(writer, csvPrinter);
          modifiedCsvFiles.add(new FileWithRecordNum(csvFileWithAddedColumns, recordCounter));
          recordCounter = 0;
        }
      }

      if (fileIsEmpty) {
        LOG.info("For job {} file {} contains only headers", importFileInDremioInfo, csvFile.getName());
        return null;
      }
    } catch (IOException | UncheckedIOException e) {
      handleCsvProcessingError(e, csvFile, importFileInDremioInfo);
    } finally {
      if (recordCounter != 0) {
        closeResources(writer, csvPrinter);
        modifiedCsvFiles.add(new FileWithRecordNum(csvFileWithAddedColumns, recordCounter));

      }
    }

    LOG.info(MEASUREMENTS + " with job {} modifyCsvFile finished", importFileInDremioInfo);
    return modifiedCsvFiles;
  }

  private void checkForEmptyValues(CSVRecord csvRecord, String s, File csvFile, ImportFileInDremioInfo importFileInDremioInfo) throws InvalidFileException {
    if (csvRecord.values().length == 0) {
      LOG.error(s, csvFile.getPath(), importFileInDremioInfo);
      throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE);
    }
  }

  private CsvHeaderMapping getHeaderTypeMapping(File csvFile, DataSetSchema dataSetSchema, ImportFileInDremioInfo importFileInDremioInfo, CSVParser csvParser) throws EEAException {
    List<String> csvHeaders = getCsvHeaders(csvParser);
    String tableSchemaId = importFileInDremioInfo.getTableSchemaId();
    if (StringUtils.isBlank(tableSchemaId)) {
      tableSchemaId = fileTreatmentHelper.getTableSchemaIdFromFileName(dataSetSchema, csvFile.getName(), false);
    }
    List<FieldSchema> expectedHeaders = checkIfCSVHeadersAreCorrect(csvHeaders, dataSetSchema, importFileInDremioInfo, csvFile.getName(), tableSchemaId);
    Map<String, DataType> fieldNameAndTypeMap = getFieldNameAndTypeMap(tableSchemaId, dataSetSchema);
    return new CsvHeaderMapping(expectedHeaders, fieldNameAndTypeMap);
  }

  private List<String> getCsvHeaders(CSVParser csvParser) {
    List<String> csvHeaders = new ArrayList<>();
    csvHeaders.add(LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER);
    csvHeaders.addAll(csvParser.getHeaderMap().keySet());
    csvHeaders.add(LiteralConstants.PARQUET_PROVIDER_CODE_COLUMN_HEADER);
    return csvHeaders;
  }

  private void handleCsvProcessingError(Exception e, File csvFile, ImportFileInDremioInfo importFileInDremioInfo) throws Exception {
    if (e.getClass().equals(UncheckedIOException.class) && e.getMessage().contains("invalid char between encapsulated token and delimiter")) {
      Integer lineNumber = extractCsvErrorLineNumber(e.getMessage());
      if (lineNumber != -1) {
        jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.ERROR_CSV_MULTIPLE_QUOTES_WITH_LINE_NUM, lineNumber);
      } else {
        jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.ERROR_CSV_MULTIPLE_QUOTES, null);
      }
    } else if (e.getClass().equals(MalformedInputException.class)) {
      jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.ERROR_CSV_ILLEGAL_CHARACTERS, null);
    }
    LOG.error("Could not read CSV file {}. {}", csvFile.getPath(), importFileInDremioInfo);
    throw new Exception("Could not read CSV file " + csvFile.getPath(), e);
  }

  private void closeResources(BufferedWriter writer, CSVPrinter csvPrinter) throws IOException {
    if (csvPrinter != null) {
      csvPrinter.flush();
      csvPrinter.close();
    }
    if (writer != null) {
      writer.close();
    }
  }

  private File createNewFilePath(File csvFile) {
    String randomStrForNewFolderSuffix = UUID.randomUUID().toString();
    String modifiedFilePath = csvFile.getPath().replace(".csv", "") + String.format(MODIFIED_CSV_SUFFIX, randomStrForNewFolderSuffix);
    return new File(modifiedFilePath);
  }

  private List<String> generateRow(CSVRecord csvRecord, List<FieldSchema> expectedHeaders, Map<String, DataType> fieldNameAndTypeMap,
                                   ImportFileInDremioInfo importFileInDremioInfo, DatasetTypeEnum datasetType) {
    List<String> row = new ArrayList<>();
    String recordIdValue = UUID.randomUUID().toString();

    for (FieldSchema expectedHeader : expectedHeaders) {
      String expectedHeaderName = expectedHeader.getHeaderName();
      DataType fieldType = fieldNameAndTypeMap.get(expectedHeaderName);
      if (fieldType == DataType.ATTACHMENT ||
              (!DatasetTypeEnum.DESIGN.equals(datasetType) && BooleanUtils.isTrue(expectedHeader.getReadOnly()) && !BooleanUtils.isTrue(importFileInDremioInfo.getReplaceData()))) {
        //if the field is attachment or replace data is not selected and the field is read only, no value should be inserted
        row.add("");
      } else if (expectedHeaderName.equals(LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER)) {
        row.add(recordIdValue);
      } else if (expectedHeaderName.equals(LiteralConstants.PARQUET_PROVIDER_CODE_COLUMN_HEADER)) {
        row.add(importFileInDremioInfo.getDataProviderCode() != null ? importFileInDremioInfo.getDataProviderCode() : "");
      } else if (csvRecord.isMapped(expectedHeaderName)) {
        row.add(spatialDataHandling.getGeoJsonEnums().contains(fieldType) ?
            spatialDataHandling.convertToHEX(csvRecord.get(expectedHeaderName)) : csvRecord.get(expectedHeaderName));
      } else {
        String headerWithBom = "\uFEFF" + expectedHeaderName;
        row.add(csvRecord.isMapped(headerWithBom) ? csvRecord.get(headerWithBom) : "");
      }
    }

    return row;
  }


  private S3PathResolver constructS3PathResolver(ImportFileInDremioInfo importFileInDremioInfo, String fileName, String tableSchemaName, String path) {
    long providerId = importFileInDremioInfo.getProviderId() != null ? importFileInDremioInfo.getProviderId() : 0L;
    return new S3PathResolver(importFileInDremioInfo.getDataflowId(), providerId, importFileInDremioInfo.getDatasetId(), tableSchemaName, fileName, path);
  }

  private String getImportPathForCsv(ImportFileInDremioInfo importFileInDremioInfo, String fileName, String tableSchemaName) throws Exception {
    S3PathResolver s3PathResolver = constructS3PathResolver(importFileInDremioInfo, fileName, tableSchemaName, S3_IMPORT_FILE_PATH);
    String pathToS3ForImport;
    pathToS3ForImport = s3Service.getS3Path(s3PathResolver);
    if (StringUtils.isBlank(pathToS3ForImport)) {
      LOG.error("Could not resolve path to s3 for import {}", importFileInDremioInfo);
      throw new Exception("Could not resolve path to s3 for import");
    }
    return pathToS3ForImport;
  }

  private String getImportQueryPathForFolder(ImportFileInDremioInfo importFileInDremioInfo, String fileName, String tableSchemaName, String pathConstant) throws Exception {
    S3PathResolver s3PathResolver = constructS3PathResolver(importFileInDremioInfo, fileName, tableSchemaName, pathConstant);
    String pathToS3ForImport;
    if (pathConstant.equals(LiteralConstants.S3_TABLE_NAME_QUERY_PATH) || pathConstant.equals(LiteralConstants.S3_IMPORT_CSV_FILE_QUERY_PATH)) {
      pathToS3ForImport = s3Service.getS3Path(s3PathResolver);
    } else {
      pathToS3ForImport = s3Service.getTableAsFolderQueryPath(s3PathResolver, pathConstant);
    }
    if (StringUtils.isBlank(pathToS3ForImport)) {
      LOG.error("Could not resolve path to s3 for import for job {}", importFileInDremioInfo);
      throw new Exception("Could not resolve path to s3 for import");
    }
    return pathToS3ForImport;
  }

  private void convertCsvToParquetCustom(File csvFile, String parquetFilePath, ImportFileInDremioInfo importFileInDremioInfo) throws Exception {

    LOG.info("For job {} converting csv file {} to parquet file {}", importFileInDremioInfo, csvFile.getPath(), parquetFilePath);
    char delimiterChar = LiteralConstants.COMMA.charAt(0);

    // Check that the parquet file exists, if so delete it
    java.nio.file.Path path = Paths.get(parquetFilePath);
    if (Files.exists(path)) {
      try {
        Files.delete(path);
      } catch (IOException e) {
        LOG.error("Could not delete folder for file {}. {}", parquetFilePath, importFileInDremioInfo);
        throw new Exception("Could not delete folder for file " + parquetFilePath);
      }
    }

    List<String> csvHeaders;
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
      csvHeaders = new ArrayList<>(csvParser.getHeaderMap().keySet());

      for (CSVRecord csvRecord : csvParser) {
        checkForEmptyValues(csvRecord, "Empty first line in csv file {}. {}", csvFile, importFileInDremioInfo);
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
        .withRowGroupSize(16 * 1024L)
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

  private List<FieldSchema> checkIfCSVHeadersAreCorrect(List<String> csvHeaders, DataSetSchema dataSetSchema, ImportFileInDremioInfo importFileInDremioInfo, String csvFileName, String tableSchemaId) throws EEAException {
    boolean atLeastOneFieldSchema = false;

    for (String csvHeader : csvHeaders) {
      if (csvHeader.startsWith("\uFEFF")) {
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

    List<FieldSchema> expectedFields = getExpectedFields(tableSchemaId, dataSetSchema, csvFileName);

    if (!atLeastOneFieldSchema) {
      List<String> expectedHeaders = expectedFields.stream().map(FieldSchema::getHeaderName).collect(Collectors.toList());
      LOG.error("Error parsing CSV file. No headers matching FieldSchemas: {}. expectedHeaders={}, actualHeaders={}",
          importFileInDremioInfo, expectedHeaders, csvHeaders);
      importFileInDremioInfo.setErrorMessage(EEAErrorMessage.ERROR_FILE_NO_HEADERS_MATCHING);
      throw new EEAException(EEAErrorMessage.ERROR_FILE_NO_HEADERS_MATCHING);
    }

    return expectedFields;
  }

  /**
   * Gets the fields.
   *
   * @param tableSchemaId the table schema id
   * @param dataSetSchema the data set schema
   * @param fileName      the fileName
   * @return the fields
   */
  private List<FieldSchema> getExpectedFields(String tableSchemaId, DataSetSchema dataSetSchema, String fileName) throws EEAException {
    if (StringUtils.isBlank(tableSchemaId)) {
      tableSchemaId = fileTreatmentHelper.getTableSchemaIdFromFileName(dataSetSchema, fileName, false);
    }
    List<FieldSchema> fields = new ArrayList<>();
    FieldSchema recordIdField = new FieldSchema();
    recordIdField.setHeaderName(PARQUET_RECORD_ID_COLUMN_HEADER);
    recordIdField.setReadOnly(false);
    FieldSchema providerCodeField = new FieldSchema();
    providerCodeField.setHeaderName(PARQUET_PROVIDER_CODE_COLUMN_HEADER);
    providerCodeField.setReadOnly(false);
    fields.add(recordIdField);
    fields.add(providerCodeField);

    if (null != tableSchemaId) {
      for (TableSchema tableSchema : dataSetSchema.getTableSchemas()) {
        if (tableSchemaId.equals(tableSchema.getIdTableSchema().toString())) {
          for (FieldSchema fieldSchema : tableSchema.getRecordSchema().getFieldSchema()) {
            fields.add(fieldSchema);
          }
          break;
        }
      }
    }

    return fields;
  }

  private Map<String, DataType> getFieldNameAndTypeMap(String tableSchemaId, DataSetSchema dataSetSchema) {
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
  public FileTreatmentHelper getFileTreatmentHelper() {
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

  private Boolean canImportForFixedNumberOfRecords(ImportFileInDremioInfo importFileInDremioInfo, Long numberOfRecordsToBeInserted, String tableQueryPath, S3PathResolver s3PathResolver){
    Long numberOfExistingRecords = 0L;
    if (s3Helper.checkFolderExist(s3PathResolver, S3_TABLE_NAME_FOLDER_PATH) && dremioHelperService.checkFolderPromoted(s3PathResolver, s3PathResolver.getTableName())) {
      String recordsCountQuery = "SELECT COUNT (record_id) FROM " + tableQueryPath;
      numberOfExistingRecords = dremioJdbcTemplate.queryForObject(recordsCountQuery, Long.class);
    }

    if(importFileInDremioInfo.getReplaceData() == false){
      importFileInDremioInfo.setWarningMessage(JobInfoEnum.WARNING_SOME_IMPORT_FAILED_FIXED_NUM_WITHOUT_REPLACE_DATA.getValue(null));
      return false;
    }
    if(numberOfExistingRecords != numberOfRecordsToBeInserted){
      importFileInDremioInfo.setWarningMessage(JobInfoEnum.WARNING_SOME_IMPORT_FAILED_WRONG_NUM_OF_RECORDS.getValue(null));
      LOG.info("For job {} for fixed number of records table, existing records are {} and records to be inserted are {}", importFileInDremioInfo, numberOfExistingRecords, numberOfRecordsToBeInserted);
      return false;
    }
    return true;
  }

  @Override
  public File exportParquetToCsvFile(String existingTableQueryPath, String exportTableQueryPath, String exportTableS3Path, String exportedFileName, String exportedFilePath, S3PathResolver s3ExportPathResolver) throws Exception {
    try {
      String createCsvQuery = "CREATE TABLE " + exportTableQueryPath + " STORE AS (type => 'text', fieldDelimiter => ',', lineDelimiter => '\r\n') " +
              " WITH SINGLE WRITER AS SELECT * FROM " + existingTableQueryPath;
      String processId = dremioHelperService.executeSqlStatement(createCsvQuery);
      dremioHelperService.checkIfDremioProcessFinishedSuccessfully(createCsvQuery, processId, null);

      File exportedFile = s3Helper.getFileFromS3(exportTableS3Path, exportedFileName, exportedFilePath, CSV_TYPE);
      return exportedFile;
    }
    finally {
      //remove newly created file from s3
      if(s3Helper.checkFolderExist(s3ExportPathResolver)){
        s3Helper.deleteFileFromS3(exportTableS3Path);
      }
    }
  }

  private void updatePrefilledDataBasedOnReadOnlyData(ImportFileInDremioInfo importFileInDremioInfo, File inputFile,
                                                      S3PathResolver s3IcebergTablePathResolver, DataSetSchema dataSetSchema, DatasetTypeEnum datasetType) throws Exception {

    String icebergTablePath = s3Service.getTableAsFolderQueryPath(s3IcebergTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
    //parse csv and for each record of the input csv
    //do an update query with read only fields in where statement
    //if columns are geometry execute the to_hex command
    char delimiterChar = !StringUtils.isBlank(importFileInDremioInfo.getDelimiter()) ?
            importFileInDremioInfo.getDelimiter().charAt(0) : defaultDelimiter;

    long recordCounter = 0;

    try (Reader reader = Files.newBufferedReader(Paths.get(inputFile.getPath()));
         CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                 .setHeader()
                 .setSkipHeaderRecord(false)
                 .setDelimiter(delimiterChar)
                 .setIgnoreHeaderCase(true)
                 .setIgnoreEmptyLines(false)
                 .setTrim(true).build())) {

      CsvHeaderMapping typeMapping = getHeaderTypeMapping(inputFile, dataSetSchema, importFileInDremioInfo, csvParser);
      for (CSVRecord csvRecord : csvParser) {
        checkForEmptyValues(csvRecord, "Empty first line in CSV file {}. {}", inputFile, importFileInDremioInfo);
        if(recordCounter == 1){
          continue;
        }
        StringBuilder updateQueryBuilder = new StringBuilder().append("UPDATE ").append(icebergTablePath).append(" SET ");
        StringBuilder whereStatementBuilder = new StringBuilder().append(" WHERE ");

        for (FieldSchema field : typeMapping.getExpectedHeaders()) {
          String expectedHeaderName = field.getHeaderName();
          DataType fieldType = typeMapping.getFieldNameAndTypeMap().get(expectedHeaderName);
          if(BooleanUtils.isTrue(field.getReadOnly())){
            //construct where statement
            String value = csvRecord.get(expectedHeaderName);
            whereStatementBuilder.append(" " + expectedHeaderName + " = '" + value + "' AND");
          }
          else if (expectedHeaderName.equals(LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER) || expectedHeaderName.equals(LiteralConstants.PARQUET_PROVIDER_CODE_COLUMN_HEADER) ||
                    fieldType == DataType.ATTACHMENT){
            //we will not update these values
              continue;
          } else if (csvRecord.isMapped(expectedHeaderName)) {
            String value = spatialDataHandling.getGeoJsonEnums().contains(fieldType) ?
                    spatialDataHandling.convertToHEX(csvRecord.get(expectedHeaderName)) : csvRecord.get(expectedHeaderName);
            updateQueryBuilder.append(" " + expectedHeaderName + " = '" + value + "' ,");


          } else {
            String headerWithBom = "\uFEFF" + expectedHeaderName;
            String value = csvRecord.isMapped(headerWithBom) ? csvRecord.get(headerWithBom) : "";
            updateQueryBuilder.append(" " + expectedHeaderName + " = '" + value + "' ,");
          }
        }

        //remove last character if it is comma
        if(updateQueryBuilder.toString().endsWith(" ,")){
          updateQueryBuilder.deleteCharAt(updateQueryBuilder.length() - 1);
        }
        if(whereStatementBuilder.toString().endsWith("AND")) {
          // Find the last occurrence of "AND" and remove it
          int lastIndex = whereStatementBuilder.lastIndexOf("AND");
          whereStatementBuilder.delete(lastIndex, lastIndex + 3); // 3 is the length of "AND"
        }

        updateQueryBuilder.append(whereStatementBuilder);
        String processId = dremioHelperService.executeSqlStatement(updateQueryBuilder.toString());
        dremioHelperService.checkIfDremioProcessFinishedSuccessfully(updateQueryBuilder.toString(), processId, 2000L);

      }
    } catch (IOException | UncheckedIOException e) {
      handleCsvProcessingError(e, inputFile, importFileInDremioInfo);
    }

    if (recordCounter == 0) {
      LOG.info("For job {} file {} contains only headers", importFileInDremioInfo, inputFile.getName());
    }
  }
}
