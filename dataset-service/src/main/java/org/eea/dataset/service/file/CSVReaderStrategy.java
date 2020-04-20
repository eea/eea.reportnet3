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
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
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

  /** The parse common. */
  private FileCommonUtils fileCommon;

  /** The field max length. */
  private int fieldMaxLength;

  /**
   * Instantiates a new CSV reader strategy.
   *
   * @param delimiter the delimiter
   * @param fileCommon the parse common
   * @param datasetId the dataset id
   */
  public CSVReaderStrategy(final char delimiter, final FileCommonUtils fileCommon, Long datasetId,
      int fieldMaxLength) {
    this.delimiter = delimiter;
    this.fileCommon = fileCommon;
    this.datasetId = datasetId;
    this.fieldMaxLength = fieldMaxLength;
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

      // through the file
      while ((line = reader.readNext()) != null) {
        final List<String> values = Arrays.asList(line);
        sanitizeAndCreateDataSet(partitionId, tableVO, tables, values, dataSetSchema, headers,
            idTableSchema);
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
   * @param dataSetSchema the data set schema
   * @param headers the headers
   * @param idTableSchema the id table schema
   * @throws InvalidFileException the invalid file exception
   */
  private void sanitizeAndCreateDataSet(final Long partitionId, TableVO tableVO,
      final List<TableVO> tables, final List<String> values, DataSetSchemaVO dataSetSchema,
      List<FieldSchemaVO> headers, final String idTableSchema) throws InvalidFileException {
    // if the line is white then skip it
    if (null != values && !values.isEmpty() && !(values.size() == 1 && "".equals(values.get(0)))) {
      addRecordToTable(tableVO, tables, values, partitionId, dataSetSchema, headers, idTableSchema);
    }
  }

  /**
   * Inits the reader.
   *
   * @param buf the buf
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
   */
  private List<FieldSchemaVO> setHeaders(final List<String> values, final String idTableSchema,
      DataSetSchemaVO dataSetSchema) {
    List<FieldSchemaVO> headers = new ArrayList<>();

    for (String value : values) {
      final FieldSchemaVO header = new FieldSchemaVO();
      if (idTableSchema != null) {
        final FieldSchemaVO fieldSchema =
            fileCommon.findIdFieldSchema(value, idTableSchema, dataSetSchema);
        if (null != fieldSchema) {
          header.setId(fieldSchema.getId());
          header.setType(fieldSchema.getType());
        }
      }
      header.setName(value);
      headers.add(header);
    }
    return headers;
  }

  /**
   * Adds the record to table.
   *
   * @param tableVO the table VO
   * @param tables the tables
   * @param values the values
   * @param partitionId the partition id
   * @param dataSetSchema the data set schema
   * @param headers the headers
   * @param idTableSchema the id table schema
   */
  private void addRecordToTable(TableVO tableVO, final List<TableVO> tables,
      final List<String> values, final Long partitionId, DataSetSchemaVO dataSetSchema,
      List<FieldSchemaVO> headers, final String idTableSchema) {
    // Create object Table and set the attributes
    if (null == tableVO.getIdTableSchema()) {
      tableVO.setIdTableSchema(idTableSchema);

      tableVO.setRecords(
          createRecordsVO(values, partitionId, tableVO.getIdTableSchema(), dataSetSchema, headers));
      tables.add(tableVO);

    } else {
      tableVO.getRecords().addAll(
          createRecordsVO(values, partitionId, tableVO.getIdTableSchema(), dataSetSchema, headers));
    }
  }

  /**
   * Creates the records VO.
   *
   * @param values the values
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @param dataSetSchema the data set schema
   * @param headers the headers
   * @return the list
   */
  private List<RecordVO> createRecordsVO(final List<String> values, final Long partitionId,
      final String idTableSchema, DataSetSchemaVO dataSetSchema, List<FieldSchemaVO> headers) {
    final List<RecordVO> records = new ArrayList<>();
    final RecordVO record = new RecordVO();
    if (null != idTableSchema) {
      record.setIdRecordSchema(fileCommon.findIdRecord(idTableSchema, dataSetSchema));
    }
    record.setFields(
        createFieldsVO(values, headers, fileCommon.findFieldSchemas(idTableSchema, dataSetSchema)));
    record.setDatasetPartitionId(partitionId);
    records.add(record);
    return records;
  }

  /**
   * Creates the fields VO.
   *
   * @param values the values
   * @param headers the headers
   * @param headersSchema the headers schema
   * @return the list
   */
  private List<FieldVO> createFieldsVO(final List<String> values, List<FieldSchemaVO> headers,
      List<FieldSchemaVO> headersSchema) {
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
        field.setIdFieldSchema(headers.get(contAux).getId());
        field.setType(headers.get(contAux).getType());
        field.setValue(value);
        if (field.getIdFieldSchema() != null) {
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
