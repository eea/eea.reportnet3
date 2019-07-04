package org.eea.dataset.service.file;

import java.io.IOException;
import java.io.StringWriter;
import javax.servlet.http.HttpServletResponse;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.service.file.interfaces.WriterStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.opencsv.CSVWriter;
import lombok.NoArgsConstructor;


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

  private ParseCommon parseCommon;


  private HttpServletResponse response;


  public CSVWriterStrategy(char delimiter, ParseCommon parseCommon, HttpServletResponse response) {
    super();
    this.delimiter = delimiter;
    this.parseCommon = parseCommon;
    this.response = response;
  }



  /**
   * Parses the file.
   *
   * @param inputStream the input stream
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @return the data set VO
   * @throws InvalidFileException the invalid file exception
   * @throws IOException
   */
  @Override
  public String writeFile(final Long dataflowId, final Long partitionId, final String idTableSchema)
      throws InvalidFileException, IOException {
    LOG.info("starting csv file writter");

    // DataSetSchemaVO dataSetSchema = parseCommon.getDataSetSchema(dataflowId);
    //
    // // CSVWriter csvWriter = new CSVWriter(response.getWriter(), CSVWriter.DEFAULT_SEPARATOR,
    // // CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER,
    // // CSVWriter.DEFAULT_LINE_END);
    //
    // List<FieldSchemaVO> fieldSchemas = parseCommon.getFieldSchemas(idTableSchema, dataSetSchema);
    //
    //
    //
    // Map<String, String[]> fields = new HashMap<>();
    // fieldSchemas.stream().forEach(fieldSchema -> {
    // fieldSchema.getId();
    //
    //
    // // fields.put(fieldSchema.getName(), );
    // });
    // // write all users to csv file
    // // String[] headerRecord = {"Name", "Email", "Phone", "Country"};
    // // csvWriter.writeNext(headerRecord);
    // // writer.write(new DataSetVO());

    StringWriter writer = new StringWriter();
    CSVWriter csvWriter =
        new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
            CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);


    String[] headerRecord = {"Name", "Email", "Phone", "Country"};
    csvWriter.writeNext(headerRecord);
    csvWriter.writeNext(headerRecord);// RESTO DE ELEMENTOS

    // UNA VEZ LEIDO VOLCAMOS A STRING
    String csv = writer.getBuffer().toString();

    return csv;

    // return parseCommon.getTableName(idTableSchema, dataSetSchema);
  }



}
