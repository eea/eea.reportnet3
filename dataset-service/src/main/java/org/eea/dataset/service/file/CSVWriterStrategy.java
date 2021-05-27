package org.eea.dataset.service.file;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.service.file.interfaces.WriterStrategy;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.opencsv.CSVWriter;
import lombok.NoArgsConstructor;

/**
 * Instantiates a new CSV writer strategy.
 */
@NoArgsConstructor
public class CSVWriterStrategy implements WriterStrategy {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(CSVWriterStrategy.class);

  /** The delimiter. */
  private char delimiter;

  /** The file common. */
  private FileCommonUtils fileCommon;

  /**
   * Instantiates a new CSV writer strategy.
   *
   * @param delimiter the delimiter
   * @param fileCommon the file common
   */
  public CSVWriterStrategy(char delimiter, FileCommonUtils fileCommon) {
    super();
    this.delimiter = delimiter;
    this.fileCommon = fileCommon;
  }

  /**
   * Write file.
   *
   * @param dataflowId the dataflow id
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param includeCountryCode the include country code
   * @return the byte[]
   * @throws EEAException the EEA exception
   */
  @Override
  public byte[] writeFile(final Long dataflowId, final Long datasetId, final String tableSchemaId,
      boolean includeCountryCode) throws EEAException {
    LOG.info("starting csv file writter");

    DataSetSchemaVO dataSetSchema = fileCommon.getDataSetSchema(dataflowId, datasetId);

    // Init the writer
    StringWriter writer = new StringWriter();
    CSVWriter csvWriter = new CSVWriter(writer, delimiter, CSVWriter.DEFAULT_QUOTE_CHARACTER,
        CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

    setLines(tableSchemaId, dataSetSchema, csvWriter, datasetId, includeCountryCode);

    // Once read we convert it to string
    String csv = writer.getBuffer().toString();

    return csv.getBytes();

  }

  /**
   * Write file list.
   *
   * @param dataflowId the dataflow id
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param includeCountryCode the include country code
   * @return the byte[]
   * @throws EEAException the EEA exception
   */
  @Override
  public List<byte[]> writeFileList(final Long dataflowId, final Long datasetId,
      final String tableSchemaId, boolean includeCountryCode) throws EEAException {
    LOG.info("starting csv file writter");

    DataSetSchemaVO dataSetSchema = fileCommon.getDataSetSchema(dataflowId, datasetId);

    // Init the writer
    StringWriter writer = new StringWriter();
    CSVWriter csvWriter = new CSVWriter(writer, delimiter, CSVWriter.DEFAULT_QUOTE_CHARACTER,
        CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

    List<byte[]> byteList = new ArrayList<>();
    if ("validations".equals(tableSchemaId)) {
      setValidationLines(csvWriter, datasetId);
    } else if (tableSchemaId != null) {
      setLines(tableSchemaId, dataSetSchema, csvWriter, datasetId, includeCountryCode);

      // Once read we convert it to string
      byteList.add(writer.getBuffer().toString().getBytes());
    } else {
      dataSetSchema.getTableSchemas().forEach(tableSchemaVO -> {
        setLines(tableSchemaVO.getIdTableSchema(), dataSetSchema, csvWriter, datasetId,
            includeCountryCode);

        // Once read we convert it to string
        byteList.add(writer.getBuffer().toString().getBytes());
      });
    }

    return byteList;

  }

  private void setValidationLines(CSVWriter csvWriter, Long datasetId) {
    // TODO Auto-generated method stub

  }

  /**
   * Sets the lines.
   *
   * @param idTableSchema the id table schema
   * @param dataSetSchema the data set schema
   * @param csvWriter the csv writer
   * @param datasetId the dataset id
   * @param includeCountryCode the include country code
   */
  private void setLines(final String idTableSchema, DataSetSchemaVO dataSetSchema,
      CSVWriter csvWriter, Long datasetId, boolean includeCountryCode) {

    List<FieldSchemaVO> fieldSchemas = fileCommon.getFieldSchemas(idTableSchema, dataSetSchema);
    List<RecordValue> records = fileCommon.getRecordValues(datasetId, idTableSchema);
    Map<String, Integer> indexMap = new HashMap<>();

    // If we don't have fieldSchemas, return an empty file.
    if (fieldSchemas != null) {

      int nHeaders = setHeaders(fieldSchemas, indexMap, csvWriter, includeCountryCode);

      // If we don't have records, return a file only with headers.
      if (records != null) {
        setRecords(records, indexMap, nHeaders, csvWriter, includeCountryCode);
      }
    }
  }

  /**
   * Sets the headers.
   *
   * @param fieldSchemas the field schemas
   * @param indexMap the index map
   * @param csvWriter the csv writer
   * @param includeCountryCode the include country code
   * @return the int
   */
  private int setHeaders(List<FieldSchemaVO> fieldSchemas, Map<String, Integer> indexMap,
      CSVWriter csvWriter, boolean includeCountryCode) {

    List<String> headers = new ArrayList<>();
    int nHeaders = 0;

    if (includeCountryCode) {
      headers.add("Country code");
      nHeaders++;
    }

    for (FieldSchemaVO fieldSchema : fieldSchemas) {
      headers.add(fieldSchema.getName());
      indexMap.put(fieldSchema.getId(), nHeaders++);
    }
    csvWriter.writeNext(headers.stream().toArray(String[]::new), false);

    return nHeaders;
  }

  /**
   * Sets the records.
   *
   * @param records the records
   * @param indexMap the index map
   * @param nHeaders the n headers
   * @param csvWriter the csv writer
   * @param includeCountryCode the include country code
   */
  private void setRecords(List<RecordValue> records, Map<String, Integer> indexMap, int nHeaders,
      CSVWriter csvWriter, boolean includeCountryCode) {

    for (RecordValue recordValue : records) {
      List<FieldValue> fields = recordValue.getFields();
      List<String> unknownColumns = new ArrayList<>();
      String[] fieldsToWrite = new String[nHeaders];

      if (includeCountryCode) {
        fieldsToWrite[0] = recordValue.getDataProviderCode();
      }

      for (FieldValue field : fields) {
        if (null != field.getIdFieldSchema()) {
          fieldsToWrite[indexMap.get(field.getIdFieldSchema())] = field.getValue();
        } else {
          unknownColumns.add(field.getValue());
        }
      }

      csvWriter.writeNext(joinOutputArray(unknownColumns, fieldsToWrite), false);
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
