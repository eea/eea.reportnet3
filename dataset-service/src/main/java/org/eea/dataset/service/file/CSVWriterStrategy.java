package org.eea.dataset.service.file;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.service.file.interfaces.WriterStrategy;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
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

    setLines(tableSchemaId, dataSetSchema, csvWriter, datasetId, includeCountryCode, false);

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
      boolean includeCountryCode, boolean includeValidations) throws EEAException {
    LOG.info("starting csv file writter");

    DataSetSchemaVO dataSetSchema = fileCommon.getDataSetSchema(dataflowId, datasetId);

    // Init the writer
    StringWriter writer = new StringWriter();
    CSVWriter csvWriter = new CSVWriter(writer, delimiter, CSVWriter.DEFAULT_QUOTE_CHARACTER,
        CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

    List<byte[]> byteList = new ArrayList<>();
    dataSetSchema.getTableSchemas().forEach(tableSchemaVO -> {
      setLines(tableSchemaVO.getIdTableSchema(), dataSetSchema, csvWriter, datasetId,
          includeCountryCode, includeValidations);

      // Once read we convert it to string
      byteList.add(writer.getBuffer().toString().getBytes());
      writer.getBuffer().setLength(0);
    });

    return byteList;

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
      CSVWriter csvWriter, Long datasetId, boolean includeCountryCode, boolean includeValidations) {

    List<FieldSchemaVO> fieldSchemas = fileCommon.getFieldSchemas(idTableSchema, dataSetSchema);
    List<RecordValue> records = fileCommon.getRecordValues(datasetId, idTableSchema);
    Map<String, String> errorsMap = null;
    if (includeValidations) {
      errorsMap = mapErrors(fileCommon.getErrors(datasetId, idTableSchema, dataSetSchema));
    }
    Map<String, Integer> indexMap = new HashMap<>();

    // If we don't have fieldSchemas, return an empty file.
    if (fieldSchemas != null) {

      int nHeaders =
          setHeaders(fieldSchemas, indexMap, csvWriter, includeCountryCode, includeValidations);

      // If we don't have records, return a file only with headers.
      if (records != null) {
        setRecords(records, indexMap, nHeaders, csvWriter, includeCountryCode, errorsMap);
      }
    }
  }

  private Map<String, String> mapErrors(FailedValidationsDatasetVO failedValidationsByIdDataset) {
    Map<String, String> errorsMap = new HashMap<>();
    for (Object error : failedValidationsByIdDataset.getErrors()) {
      LinkedHashMap<?, ?> castedError = (LinkedHashMap<?, ?>) error;
      String value = errorsMap.putIfAbsent(castedError.get("idObject").toString(),
          castedError.get("levelError") + ": " + castedError.get("message"));
      if (value != null) {
        errorsMap.put(castedError.get("idObject").toString(),
            value + " " + castedError.get("levelError") + ": " + castedError.get("message"));
      }
    }
    return errorsMap;
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
      CSVWriter csvWriter, boolean includeCountryCode, boolean includeValidations) {

    List<String> headers = new ArrayList<>();
    int nHeaders = 0;

    if (includeCountryCode) {
      headers.add("Country code");
      nHeaders++;
    }

    for (FieldSchemaVO fieldSchema : fieldSchemas) {
      headers.add(fieldSchema.getName());
      indexMap.put(fieldSchema.getId(), nHeaders++);
      if (includeValidations) {
        headers.add(fieldSchema.getName() + " validations");
        nHeaders++;
      }
    }

    if (includeValidations) {
      headers.add("Record validations");
      nHeaders++;
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
      CSVWriter csvWriter, boolean includeCountryCode, Map<String, String> errorsMap) {

    for (RecordValue recordValue : records) {
      List<FieldValue> fields = recordValue.getFields();
      List<String> unknownColumns = new ArrayList<>();
      String[] fieldsToWrite = new String[nHeaders];

      if (includeCountryCode) {
        fieldsToWrite[0] = recordValue.getDataProviderCode();
      }

      for (FieldValue field : fields) {
        if (null != field.getIdFieldSchema()) {
          Integer index = indexMap.get(field.getIdFieldSchema());
          fieldsToWrite[index] = field.getValue();
          if (errorsMap != null) {
            fieldsToWrite[index + 1] = errorsMap.get(field.getId());
          }
        } else {
          unknownColumns.add(field.getValue());
        }
      }
      if (errorsMap != null) {
        fieldsToWrite[nHeaders - 1] = errorsMap.get(recordValue.getId());
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
