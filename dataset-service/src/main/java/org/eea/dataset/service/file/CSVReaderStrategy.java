package org.eea.dataset.service.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.service.file.interfaces.ReaderStrategy;
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
 * The Class CSVReaderStrategy.
 */

/**
 * Instantiates a new CSV reader strategy.
 */
@NoArgsConstructor
public class CSVReaderStrategy implements ReaderStrategy {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(CSVReaderStrategy.class);

  /** The delimiter. */
  private char delimiter;

  /**
   * The parse common.
   */
  private ParseCommon parseCommon;


  /**
   * Instantiates a new CSV reader strategy.
   *
   * @param delimiter the delimiter
   * @param parseCommon the parse common
   */
  public CSVReaderStrategy(final char delimiter, final ParseCommon parseCommon) {
    this.delimiter = delimiter;
    this.parseCommon = parseCommon;
  }

  /**
   * Parses the file.
   *
   * @param inputStream the input stream
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   *
   * @return the data set VO
   *
   * @throws InvalidFileException the invalid file exception
   */
  @Override
  public DataSetVO parseFile(final InputStream inputStream, final Long dataflowId,
      final Long partitionId) throws InvalidFileException {
    LOG.info("starting csv file reading");
    return readLines(inputStream, dataflowId, partitionId);
  }

  /**
   * Read lines.
   *
   * @param inputStream the input stream
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @return the data set VO
   * @throws InvalidFileException the invalid file exception
   */
  private DataSetVO readLines(final InputStream inputStream, final Long dataflowId,
      final Long partitionId) throws InvalidFileException {
    LOG.info("Processing entries at method readLines");
    // Init variables
    String[] line;
    TableVO tableVO = new TableVO();
    final List<TableVO> tables = new ArrayList<>();
    final DataSetVO dataset = new DataSetVO();
    List<FieldSchemaVO> headers = new ArrayList<>();

    // Get DataSetSchema
    DataSetSchemaVO dataSetSchema = parseCommon.getDataSetSchema(dataflowId);

    try (Reader buf =
        new BufferedReader(new InputStreamReader(inputStream, Charset.forName("ISO-8859-15")))) {

      // Init de library of reader file
      final CSVReader reader = initReader(buf);

      // through the file
      while ((line = reader.readNext()) != null) {
        final List<String> values = Arrays.asList(line);
        tableVO = sanitizeAndCreateDataSet(partitionId, line, tableVO, tables, values,
            dataSetSchema, headers);
        headers = tableVO.getHeaders();
      }
      dataset.setTableVO(tables);
      // Set the dataSetSchemaId of MongoDB
      if (null != dataSetSchema) {
        dataset.setIdDatasetSchema(
            null != dataSetSchema.getIdDataSetSchema() ? dataSetSchema.getIdDataSetSchema() : null);
      }
    } catch (final IOException e) {
      LOG_ERROR.error(e.getMessage());
      throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE, e);
    }
    LOG.info("Reading Csv File Completed");
    return dataset;

  }

  /**
   * Sanitize and create data set.
   *
   * @param partitionId the partition id
   * @param line the line
   * @param tableVO the table VO
   * @param tables the tables
   * @param values the values
   * @param dataSetSchema the data set schema
   * @param headers the headers
   * @return the table VO
   * @throws InvalidFileException the invalid file exception
   */
  private TableVO sanitizeAndCreateDataSet(final Long partitionId, final String[] line,
      TableVO tableVO, final List<TableVO> tables, final List<String> values,
      DataSetSchemaVO dataSetSchema, List<FieldSchemaVO> headers) throws InvalidFileException {
    if (null != values && !values.isEmpty()) {
      // if the line is white then skip it
      if (line.length == 1 && line[0].isEmpty()) {
        return tableVO;
      } else if (line.length == 1) {
        // File format is invalid
        throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE);
      }
      // determine whether the row is a header
      if (parseCommon.isHeader(values.get(0))) {
        headers = setHeaders(values);
        tableVO.setHeaders(headers);
      } else {
        tableVO = createTableVO(tableVO, tables, values, partitionId, dataSetSchema, headers);
      }
    }
    return tableVO;
  }

  /**
   * Inits the reader.
   *
   * @param buf the buf
   *
   * @return the CSV reader
   */
  private static CSVReader initReader(final Reader buf) {
    // Init CSV Library and select | as a delimiter
    final CSVParser csvParser = new CSVParserBuilder().withSeparator(delimiter).build();
    return new CSVReaderBuilder(buf).withCSVParser(csvParser).build();
  }

  /**
   * Sets the headers.
   *
   * @param values the values
   *
   * @return the list
   */
  private List<FieldSchemaVO> setHeaders(final List<String> values) {
    List<FieldSchemaVO> headers = new ArrayList<>();

    for (final String value : values) {
      if (!parseCommon.isHeader(value)) {
        final FieldSchemaVO header = new FieldSchemaVO();
        header.setName(value);
        headers.add(header);
      }
    }
    return headers;
  }

  /**
   * Creates the table VO.
   *
   * @param tableVO the table VO
   * @param tables the tables
   * @param values the values
   * @param partitionId the partition id
   * @param dataSetSchema the data set schema
   * @param headers the headers
   * @return the table VO
   * @throws InvalidFileException the invalid file exception
   */
  private TableVO createTableVO(TableVO tableVO, final List<TableVO> tables,
      final List<String> values, final Long partitionId, DataSetSchemaVO dataSetSchema,
      List<FieldSchemaVO> headers) throws InvalidFileException {
    // Create object Table and set he attributes
    if (headers.isEmpty()) {
      throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE);
    }
    if (!values.get(0).equals(tableVO.getName())) {
      tableVO = new TableVO();
      tableVO.setHeaders(headers);
      tableVO.setName(values.get(0));
      if (null != dataSetSchema) {
        tableVO.setIdTableSchema(parseCommon.findIdTable(tableVO.getName(), dataSetSchema));
      }
      tableVO.setRecords(
          createRecordsVO(values, partitionId, tableVO.getIdTableSchema(), dataSetSchema, headers));
      tables.add(tableVO);

    } else {
      tableVO.getRecords().addAll(
          createRecordsVO(values, partitionId, tableVO.getIdTableSchema(), dataSetSchema, headers));
    }
    return tableVO;
  }

  /**
   * Creates the records VO.
   *
   * @param values the values
   * @param partitionId the partition id
   * @param idTablaSchema the id tabla schema
   * @param dataSetSchema the data set schema
   * @param headers the headers
   * @return the list
   */
  private List<RecordVO> createRecordsVO(final List<String> values, final Long partitionId,
      final String idTablaSchema, DataSetSchemaVO dataSetSchema, List<FieldSchemaVO> headers) {
    final List<RecordVO> records = new ArrayList<>();
    final RecordVO record = new RecordVO();
    if (null != idTablaSchema) {
      record.setIdRecordSchema(parseCommon.findIdRecord(idTablaSchema, dataSetSchema));
    }
    record.setFields(createFieldsVO(values, idTablaSchema, dataSetSchema, headers));
    record.setDatasetPartitionId(partitionId);
    records.add(record);
    return records;
  }

  /**
   * Creates the fields VO.
   *
   * @param values the values
   * @param idTablaSchema the id tabla schema
   * @param dataSetSchema the data set schema
   * @param headers the headers
   * @return the list
   */
  private List<FieldVO> createFieldsVO(final List<String> values, final String idTablaSchema,
      DataSetSchemaVO dataSetSchema, List<FieldSchemaVO> headers) {
    final List<FieldVO> fields = new ArrayList<>();
    values.size();
    int contAux = 0;
    for (final String value : values.subList(1, values.size())) {
      final FieldVO field = new FieldVO();
      if (idTablaSchema != null) {
        final FieldSchemaVO fieldSchema = parseCommon
            .findIdFieldSchema(headers.get(contAux).getName(), idTablaSchema, dataSetSchema);
        if (fieldSchema != null) {
          headers.get(contAux).setId(fieldSchema.getId());
          field.setIdFieldSchema(fieldSchema.getId());
          field.setType(fieldSchema.getType());
        }
      }
      field.setValue(value);
      fields.add(field);
      contAux++;
    }

    return fields;
  }


}
