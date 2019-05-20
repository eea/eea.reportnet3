package org.eea.dataset.service.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eea.dataset.schemas.repository.SchemasRepository;
import org.eea.dataset.service.file.interfaces.ReaderStrategy;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
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

  /** The Constant TABLE_HEADER. */
  private static final String TABLE_HEADER = "_TABLE";

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(CSVReaderStrategy.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  private SchemasRepository schemasRepository;


  public CSVReaderStrategy(SchemasRepository schemasRepository) {
    this.schemasRepository = schemasRepository;
  }

  /**
   * Parses the file.
   *
   * @param inputStream the input stream
   * @return the data set VO
   */
  @Override
  public DataSetVO parseFile(InputStream inputStream, String datasetId, String username) {
    try (Reader buf = new BufferedReader(new InputStreamReader(inputStream))) {
      return readLines(buf);
    } catch (IOException e) {
      LOG_ERROR.error(e.getMessage());
      return null;
    }
  }

  /**
   * Read lines.
   *
   * @param buf the reader
   * @return the data set VO
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private DataSetVO readLines(Reader buf) throws IOException {
    String[] line;
    TableVO tableVO = new TableVO();
    List<TableVO> tables = new ArrayList<>();
    List<FieldSchemaVO> headers = new ArrayList<>();
    DataSetVO dataset = new DataSetVO();

    CSVReader reader = initReader(buf);

    while ((line = reader.readNext()) != null) {
      List<String> values = Arrays.asList(line);
      if (null != values && !values.isEmpty()) {
        if (isHeader(values.get(0))) {
          headers = setHeaders(values);
        } else {
          tableVO = createTableVO(tableVO, tables, headers, values);
        }
      }
    }
    dataset.setTableVO(tables);

    return dataset;

  }

  /**
   * Inits the reader.
   *
   * @param buf the buf
   * @return the CSV reader
   */
  private CSVReader initReader(Reader buf) {
    CSVParser csvParser = new CSVParserBuilder().withSeparator(PIPE_DELIMITER).build();
    return new CSVReaderBuilder(buf).withCSVParser(csvParser).build();
  }

  /**
   * Creates the table VO.
   *
   * @param tableVO the table VO
   * @param tables the tables
   * @param headers the headers
   * @param values the values
   * @return the table VO
   */
  private TableVO createTableVO(TableVO tableVO, List<TableVO> tables, List<FieldSchemaVO> headers,
      List<String> values) {
    if (!values.get(0).equals(tableVO.getName())) {
      tableVO = new TableVO();
      tableVO.setHeaders(headers);
      tableVO.setName(values.get(0));
      tableVO.setRecords(createRecordsVO(values));
      tables.add(tableVO);
    } else {
      tableVO.getRecords().addAll(createRecordsVO(values));
    }
    return tableVO;
  }

  /**
   * Checks if is header.
   *
   * @param value the value
   * @return the boolean
   */
  private Boolean isHeader(String value) {
    return TABLE_HEADER.equalsIgnoreCase(value.trim());
  }

  /**
   * Sets the headers.
   *
   * @param values the values
   * @return the list
   */
  private List<FieldSchemaVO> setHeaders(List<String> values) {
    List<FieldSchemaVO> headers = new ArrayList<>();

    for (String value : values) {
      if (!isHeader(value)) {
        FieldSchemaVO header = new FieldSchemaVO();
        header.setName(value);
        headers.add(header);
      }
    }
    return headers;
  }

  /**
   * Creates the records VO.
   *
   * @param values the values
   * @return the list
   */
  private List<RecordVO> createRecordsVO(List<String> values) {
    List<RecordVO> records = new ArrayList<>();
    RecordVO record = new RecordVO();
    record.setFields(createFieldsVO(values));
    records.add(record);
    return records;
  }

  /**
   * Creates the fields VO.
   *
   * @param values the values
   * @return the list
   */
  private List<FieldVO> createFieldsVO(List<String> values) {

    List<FieldVO> fields = new ArrayList<>();
    for (String value : values.subList(1, values.size())) {
      FieldVO field = new FieldVO();
      field.setValue(value);
      fields.add(field);
    }
    return fields;
  }

}
