package org.eea.dataset.service.impl;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.service.ParquetConverterService;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.model.ImportFileInDremioInfo;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParquetConverterServiceImpl implements ParquetConverterService {

    private static final Logger LOG = LoggerFactory.getLogger(ParquetConverterServiceImpl.class);

    /**
     * The delimiter.
     */
    @Value("${loadDataDelimiter}")
    private char defaultDelimiter;

    @Autowired
    private FileCommonUtils fileCommonUtils;

    @Override
    public Map<String, String> convertCsvFilesToParquetFiles(ImportFileInDremioInfo importFileInDremioInfo, List<File> csvFiles, DataSetSchema dataSetSchema) throws Exception {
        Map<String, String> parquetFileNamesAndPaths = new HashMap<>();
        for(File csvFile: csvFiles){
            String parquetFilePath = csvFile.getPath().replace(".csv",".parquet");
            String parquetFileName = csvFile.getName().replace(".csv",".parquet");
            convertCsvToParquet(csvFile.getPath(), parquetFilePath, importFileInDremioInfo.getDelimiter(), dataSetSchema, importFileInDremioInfo.getTableSchemaId(), importFileInDremioInfo.getDatasetId());
            parquetFileNamesAndPaths.put(parquetFileName, parquetFilePath);
        }
        return parquetFileNamesAndPaths;
    }

    private void convertCsvToParquet(String csvFilePath, String parquetFilePath, String delimiter, DataSetSchema dataSetSchema, String tableSchemaId, Long datasetId) throws Exception {

        //TODO change recordId to actual record id
        char delimiterChar = defaultDelimiter;
        if (!StringUtils.isBlank(delimiter)){
            delimiterChar = delimiter.charAt(0);
        }


        // Check that the parquet file exists, if so delete it
        if (Files.exists(Paths.get(parquetFilePath))) {
            try {
                Files.delete(Paths.get(parquetFilePath));
            } catch (IOException e) {
                throw new Exception("Could not delete folder for file " + parquetFilePath);
            }
        }

        List<String> csvHeaders = new ArrayList<>();
        List<List<String>> data = new ArrayList<>();
        List<FieldSchema> sanitizedHeaders;
        //Reading csv file
        try (
                Reader reader = Files.newBufferedReader(Paths.get(csvFilePath));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                        .withDelimiter(delimiterChar)
                        .withFirstRecordAsHeader()
                        .withIgnoreHeaderCase()
                        .withTrim())) {
            csvHeaders.add("recordId");
            csvHeaders.addAll(csvParser.getHeaderMap().keySet());

            sanitizedHeaders = checkIfCSVHeadersAreCorrect(csvHeaders, dataSetSchema, tableSchemaId, datasetId);

            Integer recordCounter = 0;
            for (CSVRecord csvRecord : csvParser) {
                if(csvRecord.values().length == 0){
                    LOG.error("Empty first line in csv file {}", csvFilePath);
                    throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE);
                }
                recordCounter++;
                List<String> row = new ArrayList<>();
                for (FieldSchema sanitizedHeader : sanitizedHeaders) {
                    if(sanitizedHeader.getHeaderName().equals("recordId")){
                        row.add(recordCounter.toString());
                    }
                    else {
                        row.add(csvRecord.get(sanitizedHeader.getHeaderName()));
                    }
                }
                data.add(row);
            }
        } catch (IOException e) {
            throw new Exception("Could not read csv file " + csvFilePath);
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
            throw new Exception("Could not write in parquet file " + parquetFilePath);
        }
        LOG.info("Finished writing to Parquet file: {}", parquetFilePath);
    }

    private List<FieldSchema> checkIfCSVHeadersAreCorrect(List<String> csvHeaders, DataSetSchema dataSetSchema, String tableSchemaId, Long datasetId) throws EEAException {
        boolean atLeastOneFieldSchema = false;
        List<FieldSchema> headers = new ArrayList<>();
        FieldSchema recordIdHeader = new FieldSchema();
        recordIdHeader.setHeaderName("recordId");
        headers.add(recordIdHeader);

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
            LOG.error("Error parsing CSV file. No headers matching FieldSchemas: datasetId={}, tableSchemaId={}, expectedHeaders={}, actualHeaders={}",
                    datasetId, tableSchemaId, getFieldNames(tableSchemaId, dataSetSchema), csvHeaders);
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
