package org.eea.dataset.service.file;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.eea.dataset.exception.InvalidFileException;
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
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @return the data set VO
   * @throws InvalidFileException the invalid file exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public byte[] writeFile(final Long dataflowId, final Long partitionId,
      final String idTableSchema) {
    LOG.info("starting csv file writter");

    DataSetSchemaVO dataSetSchema = parseCommon.getDataSetSchema(dataflowId);

    // Init the writer
    StringWriter writer = new StringWriter();
    CSVWriter csvWriter = new CSVWriter(writer, delimiter, CSVWriter.NO_QUOTE_CHARACTER,
        CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

    setLines(idTableSchema, dataSetSchema, csvWriter);

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
   */
  private void setLines(final String idTableSchema, DataSetSchemaVO dataSetSchema,
      CSVWriter csvWriter) {
    List<RecordValue> records = parseCommon.getRecordValues(idTableSchema);
    List<FieldSchemaVO> fieldSchemas = parseCommon.getFieldSchemas(idTableSchema, dataSetSchema);
    List<String> headers = new ArrayList<>();

    // Writting the headers
    fieldSchemas.stream().forEach(fieldSchema -> headers.add(fieldSchema.getName()));
    csvWriter.writeNext(headers.stream().toArray(String[]::new));
    // Writting the values
    for (RecordValue recordValue : records) {
      List<String> fieldsList = new ArrayList<>();
      fieldSchemas.stream().forEach(fieldSchema -> {
        Boolean isWhite = true;
        if (recordValue.getFields() != null) {
          for (FieldValue field : recordValue.getFields()) {
            if (fieldSchema.getId().equals(field.getIdFieldSchema())) {
              fieldsList.add(field.getValue());
              isWhite = false;
            }
          }
        }
        if (isWhite) {
          // if the value does not exist we write a white space
          fieldsList.add("");
        }
      });
      // Write Line
      csvWriter.writeNext(fieldsList.stream().toArray(String[]::new));
    }
  }



}
