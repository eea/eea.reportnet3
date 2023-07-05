package org.eea.dataset.service.impl;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.impl.S3ServiceImpl;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.service.ParquetConverterService;
import org.eea.dataset.service.S3HandlerService;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@ImportDataLakeCommons
@Service
public class ParquetConverterServiceImpl implements ParquetConverterService {

    private static final Logger LOG = LoggerFactory.getLogger(ParquetConverterServiceImpl.class);

    private final static String LINE_SEPARATOR = "\n";

    private final static String MOFIDIED_CSV_SUFFIX = "_modified.csv";

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
    S3HandlerService s3HandlerService;


    @Override
    public Map<String, String> convertCsvFilesToParquetFiles(ImportFileInDremioInfo importFileInDremioInfo, List<File> csvFiles, DataSetSchema dataSetSchema) throws Exception {
        Map<String, String> parquetFileNamesAndPaths = new HashMap<>();
        for(File csvFile: csvFiles){
            String parquetFilePath = csvFile.getPath().replace(".csv",".parquet");
            String parquetFileName = csvFile.getName().replace(".csv",".parquet");
            convertCsvToParquetViaDremio(csvFile, parquetFilePath, dataSetSchema, importFileInDremioInfo);
            parquetFileNamesAndPaths.put(parquetFileName, parquetFilePath);
        }
        return parquetFileNamesAndPaths;
    }

    private void convertCsvToParquetViaDremio(File csvFile, String parquetFilePath, DataSetSchema dataSetSchema, ImportFileInDremioInfo importFileInDremioInfo) throws Exception {

        LOG.info("For job {} converting csv file {} to parquet file {}", importFileInDremioInfo, csvFile.getPath(), parquetFilePath);
        File csvFileWithAddedColumns = modifyCsvFile(csvFile, parquetFilePath, dataSetSchema, importFileInDremioInfo);
        //upload modified csv file to s3 and promote it.
        String importPathForParquet = getImportPathForParquet(importFileInDremioInfo, csvFileWithAddedColumns.getName());
        s3HandlerService.uploadFileToBucket(importPathForParquet, csvFileWithAddedColumns.getPath());
        promoteFolderOrFile(importFileInDremioInfo, csvFileWithAddedColumns.getName(), false);

        //if folder that we want to create is promoted, first demote it
        demoteFolderOrFile(importFileInDremioInfo, csvFile.getName().replace(".csv",""), true);
        //convert csv to parquet
        //String createTableQuery = "CREATE TABLE " + parquetName + " AS SELECT * FROM " + csvName;
        //dremioJdbcTemplate.execute(createTableQuery);

        //promote folder
        promoteFolderOrFile(importFileInDremioInfo, csvFile.getName().replace(".csv",""), true);

        LOG.info("Finished writing to Parquet file: {}. {}", parquetFilePath, importFileInDremioInfo);
    }

    private File modifyCsvFile(File csvFile, String parquetFilePath, DataSetSchema dataSetSchema, ImportFileInDremioInfo importFileInDremioInfo) throws Exception {
        // add a first column which will be the record id and a last column which will be the data provider code
        File csvFileWithAddedColumns = new File(csvFile.getPath().replace(".csv", MOFIDIED_CSV_SUFFIX));
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        char delimiterChar = defaultDelimiter;
        if (!StringUtils.isBlank(importFileInDremioInfo.getDelimiter())){
            delimiterChar = importFileInDremioInfo.getDelimiter().charAt(0);
        }

        List<String> csvHeaders = new ArrayList<>();
        List<FieldSchema> sanitizedHeaders;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile)));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFileWithAddedColumns)));
            String line = null;
            int lineNum = 0;
            String dataProviderCode = (importFileInDremioInfo.getDataProviderCode() != null) ? importFileInDremioInfo.getDataProviderCode() : "";
            while( (line = bufferedReader.readLine()) != null){
                lineNum++;
                String modifiedLine = null;
                if(lineNum == 1){
                    // modify headers
                    modifiedLine = LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER + delimiterChar + line +  delimiterChar + LiteralConstants.PARQUET_PROVIDER_CODE_COLUMN_HEADER;
                }
                else{
                    String recordIdValue = UUID.randomUUID().toString();
                    modifiedLine = recordIdValue + delimiterChar + line + delimiterChar + dataProviderCode;
                }
                bufferedWriter.write(modifiedLine + LINE_SEPARATOR);
            }
        } catch (Exception e) {
            LOG.error("Could not read csv file {}. {}", csvFile.getPath(), importFileInDremioInfo);
            throw new Exception("Could not read csv file " + csvFile.getPath());
        }
        finally {
            if(bufferedReader != null)
                bufferedReader.close();
            if(bufferedWriter != null)
                bufferedWriter.close();
        }
        return csvFileWithAddedColumns;
    }

    private void promoteFolderOrFile(ImportFileInDremioInfo importFileInDremioInfo, String fileName, Boolean folderPromote){
        Long providerId = importFileInDremioInfo.getProviderId() != null ? importFileInDremioInfo.getProviderId() : 0L;
        String tableSchemaName = fileName.replace(".parquet", "");
        S3PathResolver s3PathResolver = new S3PathResolver(importFileInDremioInfo.getDataflowId(), providerId, importFileInDremioInfo.getDatasetId(), tableSchemaName, fileName);
        dremioHelperService.promoteFolderOrFile(s3PathResolver, tableSchemaName, folderPromote);
    }

    private void demoteFolderOrFile(ImportFileInDremioInfo importFileInDremioInfo, String fileName, Boolean folderPromote){
        Long providerId = importFileInDremioInfo.getProviderId() != null ? importFileInDremioInfo.getProviderId() : 0L;
        String tableSchemaName = fileName.replace(".parquet", "");
        S3PathResolver s3PathResolver = new S3PathResolver(importFileInDremioInfo.getDataflowId(), providerId, importFileInDremioInfo.getDatasetId(), tableSchemaName, fileName);
        dremioHelperService.demoteFolderOrFile(s3PathResolver, tableSchemaName, folderPromote);
    }


    private String getImportPathForParquet(ImportFileInDremioInfo importFileInDremioInfo, String fileName) throws Exception {
        Long providerId = importFileInDremioInfo.getProviderId() != null ? importFileInDremioInfo.getProviderId() : 0L;
        String tableSchemaName = fileName.replace(".parquet", "");
        S3PathResolver s3PathResolver = new S3PathResolver(importFileInDremioInfo.getDataflowId(), providerId, importFileInDremioInfo.getDatasetId(), tableSchemaName, fileName);
        String pathToS3ForImport = s3Service.getTableNameProviderPath(s3PathResolver);
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
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                        .withDelimiter(delimiterChar)
                        .withFirstRecordAsHeader()
                        .withIgnoreHeaderCase()
                        .withTrim())) {
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
                }
            }
            header.setHeaderName(csvHeader);
            headers.add(header);
        }

        if (!atLeastOneFieldSchema) {
            LOG.error("Error parsing CSV file. No headers matching FieldSchemas: {}. expectedHeaders={}, actualHeaders={}",
                    importFileInDremioInfo, getFieldNames(tableSchemaId, dataSetSchema), csvHeaders);
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
     * @return the field names
     */
    private List<String> getFieldNames(String tableSchemaId, DataSetSchema dataSetSchema) {
        List<String> fieldNames = new ArrayList<>();

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
