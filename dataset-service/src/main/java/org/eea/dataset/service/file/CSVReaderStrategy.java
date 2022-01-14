package org.eea.dataset.service.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.input.BOMInputStream;
import org.bson.types.ObjectId;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.service.file.interfaces.ReaderStrategy;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.NoArgsConstructor;

/**
 * Instantiates a new CSV reader strategy.
 */
@NoArgsConstructor
public class CSVReaderStrategy implements ReaderStrategy {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(CSVReaderStrategy.class);

  /** The delimiter. */
  private char delimiter;

  /** The dataset id. */
  private Long datasetId;

  /** The file common. */
  private FileCommonUtils fileCommon;

  /** The field max length. */
  private int fieldMaxLength;

  /** The provider code. */
  private String providerCode;

  /** The batch record save. */
  private int batchRecordSave;



  /**
   * Instantiates a new CSV reader strategy.
   *
   * @param delimiter the delimiter
   * @param fileCommon the file common
   * @param datasetId the dataset id
   * @param fieldMaxLength the field max length
   */
  public CSVReaderStrategy(final char delimiter, final FileCommonUtils fileCommon, Long datasetId,
      int fieldMaxLength) {
    this.delimiter = delimiter;
    this.fileCommon = fileCommon;
    this.datasetId = datasetId;
    this.fieldMaxLength = fieldMaxLength;
    this.providerCode = "";
  }

  /**
   * Instantiates a new CSV reader strategy.
   *
   * @param delimiter the delimiter
   * @param fileCommon the file common
   * @param datasetId the dataset id
   * @param fieldMaxLength the field max length
   * @param providerCode the provider code
   */
  public CSVReaderStrategy(char delimiter, FileCommonUtils fileCommon, Long datasetId,
      int fieldMaxLength, String providerCode) {
    this.delimiter = delimiter;
    this.fileCommon = fileCommon;
    this.datasetId = datasetId;
    this.fieldMaxLength = fieldMaxLength;
    this.providerCode = providerCode;
  }

  /**
   * Instantiates a new CSV reader strategy.
   *
   * @param delimiter the delimiter
   * @param fileCommon the file common
   * @param datasetId the dataset id
   * @param fieldMaxLength the field max length
   * @param providerCode the provider code
   * @param batchRecordSave the batch record save
   */
  public CSVReaderStrategy(char delimiter, FileCommonUtils fileCommon, Long datasetId,
      int fieldMaxLength, String providerCode, int batchRecordSave) {
    this.delimiter = delimiter;
    this.fileCommon = fileCommon;
    this.datasetId = datasetId;
    this.fieldMaxLength = fieldMaxLength;
    this.providerCode = providerCode;
    this.batchRecordSave = batchRecordSave;
  }



  /**
   * Parses the file.
   *
   * @param inputStream the input stream
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param replace the replace
   * @param schema the schema
   * @return the data set VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void parseFile(final InputStream inputStream, final Long dataflowId,
      final Long partitionId, final String idTableSchema, Long datasetId, String fileName,
      boolean replace, DataSetSchema schema, ConnectionDataVO connectionDataVO)
      throws EEAException {
    LOG.info("starting csv file reading");
    readLines(inputStream, partitionId, idTableSchema, datasetId, fileName, replace, schema,
        connectionDataVO);
  }

  /**
   * Read lines.
   *
   * @param inputStream the input stream
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param replace the replace
   * @param dataSetSchema the data set schema
   * @return the data set VO
   * @throws EEAException the EEA exception
   */
  private DatasetValue readLines(final InputStream inputStream, final Long partitionId,
      final String idTableSchema, Long datasetId, String fileName, boolean replace,
      DataSetSchema dataSetSchema, ConnectionDataVO connectionDataVO) throws EEAException {
    LOG.info("Processing entries at method readLines in dataset {}", datasetId);
    // Init variables
    String[] line;
    TableValue table = new TableValue();
    final List<TableValue> tables = new ArrayList<>();
    final DatasetValue dataset = new DatasetValue();
    dataset.setId(datasetId);
    table.setIdTableSchema(idTableSchema);
    table.setRecords(new ArrayList<>());
    table.setDatasetId(dataset);
    tables.add(table);

    try (Reader buf = new BufferedReader(
        new InputStreamReader(new BOMInputStream(inputStream), StandardCharsets.UTF_8))) {

      // Init de library of reader file
      final CSVReader reader = initReader(buf);

      // Get the first Line
      List<String> firstLine = Arrays.asList(reader.readNext());

      // if first line is empty throw an error
      lineEmpty(firstLine);

      // Get the headers
      List<FieldSchema> headers = setHeaders(firstLine, idTableSchema, dataSetSchema);
      String idRecordSchema = fileCommon.findIdRecord(idTableSchema, dataSetSchema);
      List<FieldSchema> fieldSchemas = fileCommon.findFieldSchemas(idTableSchema, dataSetSchema);
      boolean isDesignDataset = fileCommon.isDesignDataset(datasetId);
      TableSchema tableSchema = dataSetSchema.getTableSchemas().stream()
          .filter(tableSc -> tableSc.getIdTableSchema().equals(new ObjectId(idTableSchema)))
          .findFirst().orElse(new TableSchema());
      boolean isFixedNumberOfRecords = tableSchema.getFixedNumber();
      if (!isDesignDataset && tableSchema.getRecordSchema().getFieldSchema().stream()
          .allMatch(FieldSchema::getReadOnly)) {
        throw new IOException("All fields for this table " + tableSchema.getNameTableSchema()
            + " are readOnly, you can't import new fields");
      }
      boolean manageFixedRecords =
          fileCommon.schemaContainsFixedRecords(datasetId, dataSetSchema, idTableSchema);

      // through the file
      int numLines = 0;
      while ((line = reader.readNext()) != null && numLines < 5000) {
        final List<String> values = Arrays.asList(line);
        sanitizeAndCreateDataSet(partitionId, table, tables, values, headers, idTableSchema,
            idRecordSchema, fieldSchemas, isDesignDataset, isFixedNumberOfRecords);
        numLines++;
        if (numLines == batchRecordSave) {
          dataset.setTableValues(tables);
          // Set the dataSetSchemaId of MongoDB
          dataset.setIdDatasetSchema(dataSetSchema.getIdDataSetSchema().toString());
          fileCommon.persistImportedDataset(idTableSchema, datasetId, fileName, replace,
              dataSetSchema, dataset, manageFixedRecords, connectionDataVO);
          numLines = 0;
          tables.remove(table);
          table.setRecords(new ArrayList<>());
          tables.add(table);
        }
      }
      dataset.setTableValues(tables);
      // Set the dataSetSchemaId of MongoDB
      dataset.setIdDatasetSchema(dataSetSchema.getIdDataSetSchema().toString());
      fileCommon.persistImportedDataset(idTableSchema, datasetId, fileName, replace, dataSetSchema,
          dataset, manageFixedRecords, connectionDataVO);

    } catch (final IOException | SQLException e) {
      LOG_ERROR.error(e.getMessage());
      throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE, e);
    }
    LOG.info("Reading Csv File Completed in dataset {}", datasetId);
    return dataset;
  }

  /**
   * Line empty.
   *
   * @param firstLine the first line
   *
   * @throws InvalidFileException the invalid file exception
   */
  private void lineEmpty(List<String> firstLine) throws InvalidFileException {
    // if the array is size one and their content is empty means that the line is empty
    if (null == firstLine || firstLine.isEmpty()
        || (firstLine.size() == 1 && "".equals(firstLine.get(0)))) {
      // throw an error if firstLine is empty, we need a header.
      throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE);
    }
  }

  /**
   * Sanitize and create data set.
   *
   * @param partitionId the partition id
   * @param table the table
   * @param tables the tables
   * @param values the values
   * @param headers the headers
   * @param idTableSchema the id table schema
   * @param idRecordSchema the id record schema
   * @param fieldSchemas the field schemas
   * @param isDesignDataset the is design dataset
   * @param isFixedNumberOfRecords the is fixed number of records
   */
  private void sanitizeAndCreateDataSet(final Long partitionId, TableValue table,
      final List<TableValue> tables, final List<String> values, List<FieldSchema> headers,
      final String idTableSchema, final String idRecordSchema, final List<FieldSchema> fieldSchemas,
      boolean isDesignDataset, boolean isFixedNumberOfRecords) {
    // if the line is white then skip it
    if (null != values && !values.isEmpty() && !(values.size() == 1 && "".equals(values.get(0)))) {
      addRecordToTable(table, tables, values, partitionId, headers, idTableSchema, idRecordSchema,
          fieldSchemas, isDesignDataset, isFixedNumberOfRecords);
    }
  }

  /**
   * Inits the reader.
   *
   * @param buf the buf
   *
   * @return the CSV reader
   */
  private CSVReader initReader(final Reader buf) {
    // Init CSV Library and select | as a delimiter
    final CSVParser csvParser = new CSVParserBuilder().withSeparator(delimiter).build();
    return new CSVReaderBuilder(buf).withCSVParser(csvParser).build();
  }

  /**
   * Sets the headers.
   *
   * @param values the values
   * @param idTableSchema the id table schema
   * @param dataSetSchema the data set schema
   * @return the list
   * @throws EEAException the EEA exception
   */
  private List<FieldSchema> setHeaders(final List<String> values, final String idTableSchema,
      DataSetSchema dataSetSchema) throws EEAException {

    boolean atLeastOneFieldSchema = false;
    List<FieldSchema> headers = new ArrayList<>();

    for (String value : values) {
      final FieldSchema header = new FieldSchema();
      if (idTableSchema != null) {
        final FieldSchema fieldSchema =
            fileCommon.findIdFieldSchema(value, idTableSchema, dataSetSchema);
        if (null != fieldSchema) {
          atLeastOneFieldSchema = true;
          header.setIdFieldSchema(fieldSchema.getIdFieldSchema());
          header.setType(fieldSchema.getType());
          header.setReadOnly(
              fieldSchema.getReadOnly() == null ? Boolean.FALSE : fieldSchema.getReadOnly());
        }
      }
      header.setHeaderName(value);
      headers.add(header);
    }

    if (!atLeastOneFieldSchema) {
      LOG_ERROR.error(
          "Error parsing CSV file. No headers matching FieldSchemas: datasetId={}, tableSchemaId={}, expectedHeaders={}, actualHeaders={}",
          datasetId, idTableSchema, getFieldNames(idTableSchema, dataSetSchema), values);
      throw new EEAException("No headers matching FieldSchemas");
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

  /**
   * Adds the record to table.
   *
   * @param table the table
   * @param tables the tables
   * @param values the values
   * @param partitionId the partition id
   * @param headers the headers
   * @param idTableSchema the id table schema
   * @param idRecordSchema the id record schema
   * @param fieldSchemas the field schemas
   * @param isDesignDataset the is design dataset
   * @param isFixedNumberOfRecords the is fixed number of records
   */
  private void addRecordToTable(TableValue table, final List<TableValue> tables,
      final List<String> values, final Long partitionId, List<FieldSchema> headers,
      final String idTableSchema, final String idRecordSchema, List<FieldSchema> fieldSchemas,
      boolean isDesignDataset, boolean isFixedNumberOfRecords) {
    // Create object Table and set the attributes
    if (null == table.getIdTableSchema()) {
      table.setIdTableSchema(idTableSchema);

      table.setRecords(createRecords(values, partitionId, table.getIdTableSchema(), headers,
          idRecordSchema, fieldSchemas, isDesignDataset, isFixedNumberOfRecords, table));
      tables.add(table);

    } else {
      table.getRecords().addAll(createRecords(values, partitionId, table.getIdTableSchema(),
          headers, idRecordSchema, fieldSchemas, isDesignDataset, isFixedNumberOfRecords, table));
    }
  }

  /**
   * Creates the records.
   *
   * @param values the values
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @param headers the headers
   * @param idRecordSchema the id record schema
   * @param fieldSchemas the field schemas
   * @param isDesignDataset the is design dataset
   * @param isFixedNumberOfRecords the is fixed number of records
   * @param tableValue the table value
   * @return the list
   */
  private List<RecordValue> createRecords(final List<String> values, final Long partitionId,
      final String idTableSchema, List<FieldSchema> headers, final String idRecordSchema,
      List<FieldSchema> fieldSchemas, boolean isDesignDataset, boolean isFixedNumberOfRecords,
      TableValue tableValue) {
    final List<RecordValue> records = new ArrayList<>();
    final RecordValue record = new RecordValue();
    if (null != idTableSchema) {
      record.setIdRecordSchema(idRecordSchema);
    }
    record.setFields(createFields(values, headers, fieldSchemas, isDesignDataset,
        isFixedNumberOfRecords, record));
    record.setDatasetPartitionId(partitionId);
    record.setDataProviderCode(this.providerCode);
    record.setTableValue(tableValue);
    records.add(record);
    return records;
  }

  /**
   * Creates the fields VO.
   *
   * @param values the values
   * @param headers the headers
   * @param headersSchema the headers schema
   * @param isDesignDataset the is design dataset
   * @param isFixedNumberOfRecords the is fixed number of records
   * @param record the record
   * @return the list
   */
  private List<FieldValue> createFields(final List<String> values, List<FieldSchema> headers,
      List<FieldSchema> headersSchema, boolean isDesignDataset, boolean isFixedNumberOfRecords,
      RecordValue record) {
    final List<FieldValue> fields = new ArrayList<>();
    List<String> idSchema = new ArrayList<>();
    int contAux = 0;

    for (String value : values) {
      final FieldValue field = new FieldValue();
      if (contAux < headers.size()) {
        FieldSchema fieldSchema = headers.get(contAux);
        if (null != fieldSchema && null != fieldSchema.getIdFieldSchema()) {
          field.setIdFieldSchema(fieldSchema.getIdFieldSchema().toString());
          field.setType(fieldSchema.getType());
          field.setValue(value);
          field.setRecord(record);
          if (null == field.getType()) {
            if (null != value && value.length() >= fieldMaxLength) {
              field.setValue(value.substring(0, fieldMaxLength));
            }
          } else {
            switch (field.getType()) {
              case ATTACHMENT:
                field.setValue("");
                break;
              case POINT:
                break;
              case LINESTRING:
                break;
              case POLYGON:
                break;
              case MULTIPOINT:
                break;
              case MULTILINESTRING:
                break;
              case MULTIPOLYGON:
                break;
              case GEOMETRYCOLLECTION:
                break;
              default:
                if (value.length() >= fieldMaxLength) {
                  field.setValue(value.substring(0, fieldMaxLength));
                }
            }
          }

          if (field.getIdFieldSchema() != null && ((!fieldSchema.getReadOnly() && !isDesignDataset)
              || isDesignDataset || isFixedNumberOfRecords)) {
            fields.add(field);
            idSchema.add(field.getIdFieldSchema());
          }
        }
      }
      contAux++;
    }
    setMissingField(headersSchema, fields, idSchema);

    return fields;
  }

  /**
   * Sets the missing field.
   *
   * @param headersSchema the headers schema
   * @param fields the fields
   * @param idSchema the id schema
   */
  private void setMissingField(List<FieldSchema> headersSchema, final List<FieldValue> fields,
      List<String> idSchema) {
    headersSchema.stream().forEach(header -> {
      if (!idSchema.contains(header.getIdFieldSchema().toString())) {
        final FieldValue field = new FieldValue();
        field.setIdFieldSchema(header.getIdFieldSchema().toString());
        field.setType(header.getType());
        field.setValue("");
        fields.add(field);
      }
    });
  }
}
