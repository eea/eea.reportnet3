package org.eea.dataset.service.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.file.interfaces.ReaderStrategy;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * The Class CSVReaderStrategy.
 */
@Component
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
   * @throws InvalidFileException
   */
  @Override
  public DataSetVO parseFile(InputStream inputStream, String datasetId, Long partitionId)
      throws InvalidFileException {
    try (Reader buf = new BufferedReader(new InputStreamReader(inputStream))) {
      return readLines(buf, datasetId);
    } catch (IOException e) {
      LOG_ERROR.error(e.getMessage());
      throw new InvalidFileException(e);
    }
  }

  /**
   * Read lines.
   *
   * @param buf the reader
   * @param datasetId
   * @return the data set VO
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidFileException
   */
  private DataSetVO readLines(Reader buf, String datasetId) throws InvalidFileException {
    String[] line;
    TableVO tableVO = new TableVO();
    List<TableVO> tables = new ArrayList<>();
    List<FieldSchemaVO> headers = new ArrayList<>();
    DataSetVO dataset = new DataSetVO();
    schemasRepository.findAll();
    ObjectId paco2 = new ObjectId();
    String paquito = paco2.toHexString();
    String paco = String.format("%016x", Integer.parseInt(datasetId));
    // Optional<DataSetSchema> ds = schemasRepository.findById(new ObjectId(paco));
    CSVReader reader = initReader(buf);

    try {

      while ((line = reader.readNext()) != null) {
        List<String> values = Arrays.asList(line);
        if (null != values && !values.isEmpty()) {
          // Clear White Line
          if (line.length == 1 && line[0].isEmpty()) {
            continue;
          } else if (line.length == 1) {
            throw new InvalidFileException("Invalid Format File");
          }

          if (isHeader(values.get(0))) {
            headers = setHeaders(values);
          } else {
            tableVO = createTableVO(tableVO, tables, headers, values);
          }
        }
      }
      dataset.setTableVO(tables);
    } catch (IOException e) {
      LOG_ERROR.error(e.getMessage());
      throw new InvalidFileException("Invalid Format File");
    }
    return dataset;

  }


  /**
   * Inits the reader.
   *
   * @param buf the buf
   * @return the CSV reader
   */
  private CSVReader initReader(Reader buf) {
    // Init CSV Library
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
   * @throws InvalidFileException
   */
  private TableVO createTableVO(TableVO tableVO, List<TableVO> tables, List<FieldSchemaVO> headers,
      List<String> values) throws InvalidFileException {
    // Create object Table and setter the attributes
    if (headers.isEmpty()) {
      throw new InvalidFileException("Invalid Format File");
    }
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
    values.size();
    for (String value : values.subList(1, values.size())) {
      FieldVO field = new FieldVO();
      field.setValue(value);
      fields.add(field);
    }

    FieldVO fieldPartition = new FieldVO();
    fieldPartition.setType("Integer");
    fieldPartition.setValue("PARTITIONID");
    fields.add(fieldPartition);

    return fields;
  }

}
