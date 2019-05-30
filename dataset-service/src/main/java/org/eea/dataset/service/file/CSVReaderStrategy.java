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
import org.eea.dataset.service.DatasetSchemaService;
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

/**
 * The Class CSVReaderStrategy.
 */

public class CSVReaderStrategy implements ReaderStrategy {

  /** The Constant PIPE_DELIMITER. */
  private static final char PIPE_DELIMITER = '|';

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The dataset schema service. */
  private DatasetSchemaService datasetSchemaService;

  /** The data set schema. */
  private DataSetSchemaVO dataSetSchema;

  /** The headers. */
  private List<FieldSchemaVO> headers;

  /** The parse common. */
  private ParseCommon parseCommon;



  /**
   * Instantiates a new CSV reader strategy.
   *
   * @param datasetSchemaService the dataset schema service
   * @param parseCommon the parse common
   */
  public CSVReaderStrategy(DatasetSchemaService datasetSchemaService, ParseCommon parseCommon) {
    this.datasetSchemaService = datasetSchemaService;
    this.parseCommon = parseCommon;
  }

  /**
   * Parses the file.
   *
   * @param inputStream the input stream
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @return the data set VO
   * @throws InvalidFileException the invalid file exception
   */
  @Override
  public DataSetVO parseFile(InputStream inputStream, Long dataflowId, Long partitionId)
      throws InvalidFileException {
    Reader buf = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    return readLines(buf, dataflowId, partitionId);

  }

  /**
   * Read lines.
   *
   * @param buf the reader
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @return the data set VO
   * @throws InvalidFileException the invalid file exception
   */
  private DataSetVO readLines(Reader buf, Long dataflowId, Long partitionId)
      throws InvalidFileException {
    // Init variables
    String[] line;
    TableVO tableVO = new TableVO();
    List<TableVO> tables = new ArrayList<>();
    headers = new ArrayList<>();
    DataSetVO dataset = new DataSetVO();

    // Get DataSetSchema
    dataSetSchema = parseCommon.getDataSetSchema(dataflowId, datasetSchemaService);

    // Init de library of reader file
    CSVReader reader = initReader(buf);

    try {
      // through the file
      while ((line = reader.readNext()) != null) {
        List<String> values = Arrays.asList(line);
        tableVO = sanitizeAndCreateDataSet(partitionId, line, tableVO, tables, values);
      }
      dataset.setTableVO(tables);
      // Set the dataSetSchemaId of MongoDB
      if (null != dataSetSchema) {
        dataset.setIdMongo(
            null != dataSetSchema.getIdDataSetSchema() ? dataSetSchema.getIdDataSetSchema() : null);
      }
    } catch (IOException e) {
      LOG_ERROR.error(e.getMessage());
      throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE, e);
    }
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
   * @return the table VO
   * @throws InvalidFileException the invalid file exception
   */
  private TableVO sanitizeAndCreateDataSet(Long partitionId, String[] line, TableVO tableVO,
      List<TableVO> tables, List<String> values) throws InvalidFileException {
    if (null != values && !values.isEmpty()) {
      // if the line is white then skipped
      if (line.length == 1 && line[0].isEmpty()) {
        return tableVO;
      } else if (line.length == 1) {
        // Format of file is invalid
        throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE);
      }
      // know if the row is a header
      if (parseCommon.isHeader(values.get(0))) {
        headers = setHeaders(values);
      } else {
        tableVO = createTableVO(tableVO, tables, values, partitionId);
      }
    }
    return tableVO;
  }

  /**
   * Inits the reader.
   *
   * @param buf the buf
   * @return the CSV reader
   */
  private CSVReader initReader(Reader buf) {
    // Init CSV Library and select | as a delimiter
    CSVParser csvParser = new CSVParserBuilder().withSeparator(PIPE_DELIMITER).build();
    return new CSVReaderBuilder(buf).withCSVParser(csvParser).build();
  }

  /**
   * Sets the headers.
   *
   * @param values the values
   * @return the list
   */
  private List<FieldSchemaVO> setHeaders(List<String> values) {
    headers = new ArrayList<>();

    for (String value : values) {
      if (!parseCommon.isHeader(value)) {
        FieldSchemaVO header = new FieldSchemaVO();
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
   * @return the table VO
   * @throws InvalidFileException the invalid file exception
   */
  private TableVO createTableVO(TableVO tableVO, List<TableVO> tables, List<String> values,
      Long partitionId) throws InvalidFileException {
    // Create object Table and setter the attributes
    if (headers.isEmpty()) {
      throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE);
    }
    if (!values.get(0).equals(tableVO.getName())) {
      tableVO = new TableVO();
      tableVO.setHeaders(headers);
      tableVO.setName(values.get(0));
      if (null != dataSetSchema) {
        tableVO.setIdMongo(parseCommon.findIdTable(tableVO.getName()));
      }
      tableVO.setRecords(createRecordsVO(values, partitionId, tableVO.getIdMongo()));
      tables.add(tableVO);

    } else {
      tableVO.getRecords().addAll(createRecordsVO(values, partitionId, tableVO.getIdMongo()));
    }
    return tableVO;
  }

  /**
   * Creates the records VO.
   *
   * @param values the values
   * @param partitionId the partition id
   * @param idTablaSchema the id tabla schema
   * @return the list
   */
  private List<RecordVO> createRecordsVO(List<String> values, Long partitionId,
      String idTablaSchema) {
    List<RecordVO> records = new ArrayList<>();
    RecordVO record = new RecordVO();
    if (null != idTablaSchema) {
      record.setIdMongo(parseCommon.findIdRecord(idTablaSchema));
    }
    record.setFields(createFieldsVO(values, idTablaSchema));
    record.setDatasetPartitionId(partitionId);
    records.add(record);
    return records;
  }

  /**
   * Creates the fields VO.
   *
   * @param values the values
   * @param idTablaSchema the id tabla schema
   * @return the list
   */
  private List<FieldVO> createFieldsVO(List<String> values, String idTablaSchema) {
    List<FieldVO> fields = new ArrayList<>();
    values.size();
    int contAux = 0;
    for (String value : values.subList(1, values.size())) {
      FieldVO field = new FieldVO();
      if (idTablaSchema != null) {
        FieldSchemaVO fieldSchema =
            parseCommon.findIdFieldSchema(headers.get(contAux).getName(), idTablaSchema);
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
