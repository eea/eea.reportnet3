package org.eea.dataset.service.file;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.service.file.interfaces.WriterStrategy;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.opencsv.CSVWriter;
import lombok.NoArgsConstructor;


/**
 * Instantiates a new CSV writer strategy.
 */

/**
 * Instantiates a new CSV writer strategy.
 */

/**
 * Instantiates a new CSV writer strategy.
 */
@NoArgsConstructor
public class CSVWriterStrategy implements WriterStrategy {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CSVWriterStrategy.class);

  /**
   * The delimiter.
   */
  private char delimiter;

  /** The parse common. */
  private ParseCommon parseCommon;


  /** The response. */
  private HttpServletResponse response;


  /**
   * Instantiates a new CSV writer strategy.
   *
   * @param delimiter the delimiter
   * @param parseCommon the parse common
   */
  public CSVWriterStrategy(char delimiter, ParseCommon parseCommon) {
    super();
    this.delimiter = delimiter;
    this.parseCommon = parseCommon;
  }



  /**
   * Parses the file.
   *
   * @param dataflowId the dataflow id
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @return the data set VO
   */
  @Override
  public byte[] writeFile(final Long dataflowId, final Long datasetId, final String idTableSchema) {
    LOG.info("starting csv file writter");

    DataSetSchemaVO dataSetSchema = parseCommon.getDataSetSchema(dataflowId);

    // Init the writer
    StringWriter writer = new StringWriter();
    CSVWriter csvWriter = new CSVWriter(writer, delimiter, CSVWriter.NO_QUOTE_CHARACTER,
        CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

    setLines(idTableSchema, dataSetSchema, csvWriter, datasetId);

    // once read we convert it to string
    String csv = writer.getBuffer().toString();

    return csv.getBytes();

  }



  /**
   * Sets the lines.
   *
   * @param idTableSchema the id table schema
   * @param dataSetSchema the data set schema
   * @param csvWriter the csv writer
   * @param datasetId the dataset id
   */
  private void setLines(final String idTableSchema, DataSetSchemaVO dataSetSchema,
      CSVWriter csvWriter, Long datasetId) {
    List<RecordValue> records = parseCommon.getRecordValues(datasetId, idTableSchema);
    List<FieldSchemaVO> fieldSchemas = parseCommon.getFieldSchemas(idTableSchema, dataSetSchema);
    List<String> headers = new ArrayList<>();
    Map<String, Integer> indexMap = new HashMap<>();

    // Writting the headers
    int nHeaders = 0;
    for (FieldSchemaVO fieldSchema : fieldSchemas) {
      headers.add(fieldSchema.getName());
      indexMap.put(fieldSchema.getId(), nHeaders++);
    }

    csvWriter.writeNext(headers.stream().toArray(String[]::new));

    // Writting the values
    for (RecordValue recordValue : records) {
      List<FieldValue> fields = recordValue.getFields();
      List<String> unknownColumns = new ArrayList<>();
      String[] fieldsToWrite = new String[nHeaders];
      for (int i = 0; i < fields.size(); i++) {
        FieldValue field = fields.get(i);
        if (null != field.getIdFieldSchema()) {
          fieldsToWrite[indexMap.get(field.getIdFieldSchema())] = field.getValue();
        } else {
          unknownColumns.add(field.getValue());
        }
      }

      csvWriter.writeNext(joinOutputArray(unknownColumns, fieldsToWrite));
    }
  }



  /**
   * Join output array.
   *
   * @param unknownColumns the unknown columns
   * @param fieldsToWrite the fields to write
   * @return the string[]
   */
  private String[] joinOutputArray(List<String> unknownColumns, String[] fieldsToWrite) {
    String[] outputFields = new String[fieldsToWrite.length + unknownColumns.size()];

    System.arraycopy(fieldsToWrite, 0, outputFields, 0, fieldsToWrite.length);
    System.arraycopy(unknownColumns.stream().toArray(String[]::new), 0, outputFields,
        fieldsToWrite.length, unknownColumns.size());
    return outputFields;
  }



}
