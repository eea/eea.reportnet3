package org.eea.dataset.service.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.service.file.interfaces.ReaderStrategy;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
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
   * Parses the file.
   *
   * @param inputStream the input stream
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @return the data set VO
   * @throws EEAException the EEA exception
   */
  @Override
  public DataSetVO parseFile(final InputStream inputStream, final Long dataflowId,
      final Long partitionId, final String idTableSchema) throws EEAException {
    LOG.info("starting csv file reading");
    return readLines(inputStream, dataflowId, partitionId, idTableSchema);
  }

  /**
   * Read lines.
   *
   * @param inputStream the input stream
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @return the data set VO
   * @throws EEAException the EEA exception
   */
  private DataSetVO readLines(final InputStream inputStream, final Long dataflowId,
      final Long partitionId, final String idTableSchema) throws EEAException {
    LOG.info("Processing entries at method readLines");
    // Init variables
    String[] line;
    TableVO tableVO = new TableVO();
    final List<TableVO> tables = new ArrayList<>();
    final DataSetVO dataset = new DataSetVO();
    tableVO.setIdTableSchema(idTableSchema);
    tableVO.setRecords(new ArrayList<RecordVO>());
    tables.add(tableVO);

    // Get DataSetSchema
    DataSetSchemaVO dataSetSchema = fileCommon.getDataSetSchema(dataflowId, datasetId);

    try (Reader buf =
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

      // Init de library of reader file
      final CSVReader reader = initReader(buf);

      // Get the first Line
      List<String> firstLine = Arrays.asList(reader.readNext());

      // if first line is empty throw an error
      lineEmpty(firstLine);

      // Get the headers
      List<FieldSchemaVO> headers = setHeaders(firstLine, idTableSchema, dataSetSchema);
      String idRecordSchema = fileCommon.findIdRecord(idTableSchema, dataSetSchema);
      List<FieldSchemaVO> fieldSchemaVOS =
          fileCommon.findFieldSchemas(idTableSchema, dataSetSchema);
      boolean isDesignDataset = fileCommon.isDesignDataset(datasetId);
      TableSchemaVO tableSchemaVO = dataSetSchema.getTableSchemas().stream()
          .filter(tableSchema -> tableSchema.getIdTableSchema().equals(idTableSchema)).findFirst()
          .orElse(new TableSchemaVO());
      boolean isFixedNumberOfRecords = tableSchemaVO.getFixedNumber();
      if (!isDesignDataset && tableSchemaVO.getRecordSchema().getFieldSchema().stream()
          .allMatch(field -> field.getReadOnly())) {
        throw new IOException("All fields for this table " + tableSchemaVO.getNameTableSchema()
            + " are readOnly, you can't import new fields");
      }
      // through the file
      while ((line = reader.readNext()) != null) {
        final List<String> values = Arrays.asList(line);
        sanitizeAndCreateDataSet(partitionId, tableVO, tables, values, headers, idTableSchema,
            idRecordSchema, fieldSchemaVOS, isDesignDataset, isFixedNumberOfRecords);
      }
      dataset.setTableVO(tables);
      // Set the dataSetSchemaId of MongoDB
      dataset.setIdDatasetSchema(null != dataSetSchema ? dataSetSchema.getIdDataSetSchema() : null);
    } catch (final IOException e) {
      LOG_ERROR.error(e.getMessage());
      throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE, e);
    }
    LOG.info("Reading Csv File Completed");
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
   * @param tableVO the table VO
   * @param tables the tables
   * @param values the values
   * @param headers the headers
   * @param idTableSchema the id table schema
   * @param idRecordSchema the id record schema
   * @param fieldSchemaVOS the field schema VOS
   * @param isDesignDataset the is design dataset
   * @param isFixedNumberOfRecords the is fixed number of records
   */
  private void sanitizeAndCreateDataSet(final Long partitionId, TableVO tableVO,
      final List<TableVO> tables, final List<String> values, List<FieldSchemaVO> headers,
      final String idTableSchema, final String idRecordSchema,
      final List<FieldSchemaVO> fieldSchemaVOS, boolean isDesignDataset,
      boolean isFixedNumberOfRecords) {
    // if the line is white then skip it
    if (null != values && !values.isEmpty() && !(values.size() == 1 && "".equals(values.get(0)))) {
      addRecordToTable(tableVO, tables, values, partitionId, headers, idTableSchema, idRecordSchema,
          fieldSchemaVOS, isDesignDataset, isFixedNumberOfRecords);
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
  private List<FieldSchemaVO> setHeaders(final List<String> values, final String idTableSchema,
      DataSetSchemaVO dataSetSchema) throws EEAException {

    boolean atLeastOneFieldSchema = false;
    List<FieldSchemaVO> headers = new ArrayList<>();

    for (String value : values) {
      final FieldSchemaVO header = new FieldSchemaVO();
      if (idTableSchema != null) {
        final FieldSchemaVO fieldSchema =
            fileCommon.findIdFieldSchema(value, idTableSchema, dataSetSchema);
        if (null != fieldSchema) {
          atLeastOneFieldSchema = true;
          header.setId(fieldSchema.getId());
          header.setType(fieldSchema.getType());
          header.setReadOnly(
              fieldSchema.getReadOnly() == null ? Boolean.FALSE : fieldSchema.getReadOnly());
        }
      }
      header.setName(value);
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
   * @param dataSetSchemaVO the data set schema VO
   * @return the field names
   */
  private List<String> getFieldNames(String tableSchemaId, DataSetSchemaVO dataSetSchemaVO) {
    List<String> fieldNames = new ArrayList<>();

    if (null != tableSchemaId) {
      for (TableSchemaVO tableSchemaVO : dataSetSchemaVO.getTableSchemas()) {
        if (tableSchemaId.equals(tableSchemaVO.getIdTableSchema())) {
          for (FieldSchemaVO fieldSchemaVO : tableSchemaVO.getRecordSchema().getFieldSchema()) {
            fieldNames.add(fieldSchemaVO.getName());
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
   * @param tableVO the table VO
   * @param tables the tables
   * @param values the values
   * @param partitionId the partition id
   * @param headers the headers
   * @param idTableSchema the id table schema
   * @param idRecordSchema the id record schema
   * @param fieldSchemaVOS the field schema VOS
   * @param isDesignDataset the is design dataset
   * @param isFixedNumberOfRecords the is fixed number of records
   */
  private void addRecordToTable(TableVO tableVO, final List<TableVO> tables,
      final List<String> values, final Long partitionId, List<FieldSchemaVO> headers,
      final String idTableSchema, final String idRecordSchema, List<FieldSchemaVO> fieldSchemaVOS,
      boolean isDesignDataset, boolean isFixedNumberOfRecords) {
    // Create object Table and set the attributes
    if (null == tableVO.getIdTableSchema()) {
      tableVO.setIdTableSchema(idTableSchema);

      tableVO.setRecords(createRecordsVO(values, partitionId, tableVO.getIdTableSchema(), headers,
          idRecordSchema, fieldSchemaVOS, isDesignDataset, isFixedNumberOfRecords));
      tables.add(tableVO);

    } else {
      tableVO.getRecords().addAll(createRecordsVO(values, partitionId, tableVO.getIdTableSchema(),
          headers, idRecordSchema, fieldSchemaVOS, isDesignDataset, isFixedNumberOfRecords));
    }
  }

  /**
   * Creates the records VO.
   *
   * @param values the values
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @param headers the headers
   * @param idRecordSchema the id record schema
   * @param fieldSchemaVOS the field schema VOS
   * @param isDesignDataset the is design dataset
   * @param isFixedNumberOfRecords the is fixed number of records
   * @return the list
   */
  private List<RecordVO> createRecordsVO(final List<String> values, final Long partitionId,
      final String idTableSchema, List<FieldSchemaVO> headers, final String idRecordSchema,
      List<FieldSchemaVO> fieldSchemaVOS, boolean isDesignDataset, boolean isFixedNumberOfRecords) {
    final List<RecordVO> records = new ArrayList<>();
    final RecordVO record = new RecordVO();
    if (null != idTableSchema) {
      record.setIdRecordSchema(idRecordSchema);
    }
    record.setFields(
        createFieldsVO(values, headers, fieldSchemaVOS, isDesignDataset, isFixedNumberOfRecords));
    record.setDatasetPartitionId(partitionId);
    record.setDataProviderCode(this.providerCode);
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
   * @return the list
   */
  private List<FieldVO> createFieldsVO(final List<String> values, List<FieldSchemaVO> headers,
      List<FieldSchemaVO> headersSchema, boolean isDesignDataset, boolean isFixedNumberOfRecords) {
    final List<FieldVO> fields = new ArrayList<>();
    List<String> idSchema = new ArrayList<>();
    int contAux = 0;

    for (String value : values) {
      // Trim the string if it is too large
      if (value.length() >= fieldMaxLength) {
        value = value.substring(0, fieldMaxLength);
      }
      final FieldVO field = new FieldVO();
      if (contAux < headers.size()) {
        FieldSchemaVO fieldSchemaVO = headers.get(contAux);
        field.setIdFieldSchema(fieldSchemaVO.getId());
        field.setType(fieldSchemaVO.getType());
        field.setValue(DataType.ATTACHMENT.equals(fieldSchemaVO.getType()) ? "" : value);

        if (field.getIdFieldSchema() != null && ((!fieldSchemaVO.getReadOnly() && !isDesignDataset)
            || isDesignDataset || isFixedNumberOfRecords)) {
          fields.add(field);
          idSchema.add(field.getIdFieldSchema());
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
  private void setMissingField(List<FieldSchemaVO> headersSchema, final List<FieldVO> fields,
      List<String> idSchema) {
    headersSchema.stream().forEach(header -> {
      if (!idSchema.contains(header.getId())) {
        final FieldVO field = new FieldVO();
        field.setIdFieldSchema(header.getId());
        field.setType(header.getType());
        field.setValue("");
        fields.add(field);
      }
    });
  }
}
