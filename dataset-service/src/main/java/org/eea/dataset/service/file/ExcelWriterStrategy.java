package org.eea.dataset.service.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.service.file.interfaces.WriterStrategy;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.ErrorsValidationVO;
import org.eea.interfaces.vo.dataset.ExportFilterVO;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.multitenancy.TenantResolver;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;

/**
 * The Class ExcelWriterStrategy.
 */
public class ExcelWriterStrategy implements WriterStrategy {

  /** The Constant INFO: {@value}. */
  private static final String INFO = "INFO:";

  /** The Constant WARNING: {@value}. */
  private static final String WARNING = "WARNING:";

  /** The Constant ERROR: {@value}. */
  private static final String ERROR = "ERROR:";

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ExcelWriterStrategy.class);

  /** The Constant NO_GEOMETRY. */
  private static final String NO_GEOMETRY = "<<GEOMETRIES ARE NOT EXPORTED>>";

  /** The parse common. */
  private FileCommonUtils fileCommon;

  /** The mime type. */
  private String mimeType;

  /**
   * Instantiates a new excel writer strategy.
   *
   * @param fileCommon the parse common
   * @param mimeType the mime type
   */
  public ExcelWriterStrategy(FileCommonUtils fileCommon, String mimeType) {
    super();
    this.fileCommon = fileCommon;
    this.mimeType = mimeType;
  }

  /**
   * Gets the mime type.
   *
   * @return the mime type
   */
  public String getMimeType() {
    return String.valueOf(mimeType);
  }

  /**
   * Sets the mime type.
   *
   * @param mimeType the new mime type
   */
  public void setMimeType(String mimeType) {
    this.mimeType = String.valueOf(mimeType);
  }

  /**
   * Write file.
   *
   * @param dataflowId the dataflow id
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param includeCountryCode the include country code
   * @param includeValidations the include validations
   * @param filters the filters
   * @return the byte[]
   * @throws EEAException the EEA exception
   */
  @Override
  public byte[] writeFile(Long dataflowId, Long datasetId, String tableSchemaId,
      String includeCountryCode, boolean includeValidations, ExportFilterVO filters)
      throws EEAException {

    DataSetSchemaVO dataset = fileCommon.getDataSetSchemaVO(dataflowId, datasetId);
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    // Get all tablesSchemas for the case the given idTableSchema doesn't exist
    List<TableSchemaVO> tables =
        dataset.getTableSchemas() != null ? dataset.getTableSchemas() : new ArrayList<>();
    TableSchemaVO table = fileCommon.findTableSchemaVO(tableSchemaId, dataset);

    // If the given idTableSchema exists, replace all tables with it
    if (null != table) {
      tables.clear();
      tables.add(table);
    }

    try (Workbook workbook = createWorkbook()) {

      LOG.info("Starting writing Excel({}) file", mimeType);

      // Add one sheet per table
      for (TableSchemaVO tableSchema : tables) {
        writeSheet(workbook, tableSchema, datasetId, includeCountryCode, includeValidations,
            dataset, filters);
      }

      workbook.write(out);

      LOG.info("Finishing writing Excel({}) file", mimeType);

    } catch (IOException e) {
      LOG_ERROR.error(e.getMessage());
    }

    return out.toByteArray();
  }

  /**
   * Write file list.
   *
   * @param dataflowId the dataflow id
   * @param datasetId the dataset id
   * @param includeCountryCode the include country code
   * @param includeValidations the include validations
   * @return the list
   * @throws EEAException the EEA exception
   */
  @Override
  public List<byte[]> writeFileList(Long dataflowId, Long datasetId, String includeCountryCode,
      boolean includeValidations) throws EEAException {

    ExportFilterVO filters = new ExportFilterVO();

    List<byte[]> byteList = new ArrayList<>();

    DataSetSchemaVO dataset = fileCommon.getDataSetSchemaVO(dataflowId, datasetId);
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    // Get all tablesSchemas for the case the given idTableSchema doesn't exist
    List<TableSchemaVO> tables =
        dataset.getTableSchemas() != null ? dataset.getTableSchemas() : new ArrayList<>();

    try (Workbook workbook = createWorkbook()) {

      LOG.info("Starting writing Excel({}) file", mimeType);

      // Add one sheet per table
      for (TableSchemaVO tableSchema : tables) {
        writeSheet(workbook, tableSchema, datasetId, includeCountryCode, includeValidations,
            dataset, filters);
      }

      workbook.write(out);

      LOG.info("Finishing writing Excel({}) file", mimeType);

    } catch (IOException e) {
      LOG_ERROR.error(e.getMessage());
    }

    byteList.add(out.toByteArray());

    return byteList;
  }

  /**
   * Creates the correct Workbook object according to the file extension.
   *
   * @return A new Workbook object.
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private Workbook createWorkbook() throws IOException {
    try {
      switch (FileTypeEnum.getEnum(mimeType)) {
        case XLS:
          return new HSSFWorkbook();
        case XLSX:
        case VALIDATIONS:
          return new XSSFWorkbook();
        default:
          throw new IOException("Unknow MIME type: " + mimeType);
      }
    } catch (NullPointerException e) {
      throw new IOException("Unknow MIME type: " + mimeType);
    }
  }

  /**
   * Writes a sheet containing a DataSet table into the given workbook.
   *
   * @param workbook the workbook
   * @param table the table
   * @param datasetId the DataSet id
   * @param includeCountryCode the include country code
   * @param includeValidations the include validations
   * @param dataset the dataset
   * @param filters the filters
   */
  private void writeSheet(Workbook workbook, TableSchemaVO table, Long datasetId,
      String includeCountryCode, boolean includeValidations, DataSetSchemaVO dataset,
      ExportFilterVO filters) {

    String nameSheet = table.getNameTableSchema();
    Sheet sheet =
        createSheetAndHeaders(workbook, table, includeCountryCode, includeValidations, nameSheet);

    List<FieldSchemaVO> fieldSchemas = table.getRecordSchema().getFieldSchema();

    // Used to map each fieldValue with the correct fieldSchema
    Map<String, Integer> indexMap = new HashMap<>();

    // Set headers
    int nHeaders = 0;
    Row rowhead = sheet.createRow(0);

    if (null != includeCountryCode) {
      rowhead.createCell(nHeaders).setCellValue(includeCountryCode);
      nHeaders++;
    }

    for (FieldSchemaVO fieldSchema : fieldSchemas) {
      rowhead.createCell(nHeaders).setCellValue(fieldSchema.getName());
      indexMap.put(fieldSchema.getId(), nHeaders++);
      if (includeValidations) {
        rowhead.createCell(nHeaders).setCellValue(fieldSchema.getName() + " validations");
        nHeaders++;
      }
    }
    if (includeValidations) {
      rowhead.createCell(nHeaders).setCellValue("Record validations");
      nHeaders++;
    }

    // Set records
    int nRow = 1;

    Map<String, String> errorsMap = null;
    if (includeValidations) {
      errorsMap = mapErrors(fileCommon.getErrors(datasetId, table.getIdTableSchema(), dataset));
    }
    CellStyle cs = workbook.createCellStyle();
    cs.setWrapText(true);
    TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, datasetId));
    Long totalRecords = fileCommon.countRecordsByTableSchema(table.getIdTableSchema());
    int batchSize = 50000 / fieldSchemas.size();
    int numSheets = 0;
    for (int numPage = 1; totalRecords >= 0; totalRecords = totalRecords - batchSize, numPage++) {
      for (RecordValue record : fileCommon.getRecordValuesPaginated(datasetId,
          table.getIdTableSchema(), PageRequest.of(numPage, batchSize), filters)) {
        // Number max of rows allowed by excel 1,048,576. Splitting into more sheets when one it's
        // fully
        if (nRow % 1048575 == 0) {
          numSheets++;
          nameSheet = table.getNameTableSchema() + "_" + numSheets;
          sheet = createSheetAndHeaders(workbook, table, includeCountryCode, includeValidations,
              nameSheet);
          nRow = 1;
        }

        Row row = sheet.createRow(nRow++);

        List<FieldValue> fields =
            record.getFields() != null ? new ArrayList<>(record.getFields()) : new ArrayList<>();
        int nextUnknownCellNumber = nHeaders;

        if (null != includeCountryCode) {
          row.createCell(0).setCellValue(record.getDataProviderCode());
        }

        for (int i = 0; i < fields.size(); i++) {
          FieldValue field = fields.get(i);
          Integer cellNumber = indexMap.get(field.getIdFieldSchema());

          if (cellNumber == null) {
            cellNumber = nextUnknownCellNumber++;
          }
          String value = NO_GEOMETRY;
          boolean geometryType = isGeometryFieldType(field);
          if (Boolean.FALSE.equals(geometryType) && field.getValue().startsWith("=")) {
            value = " " + field.getValue();
          } else if (Boolean.FALSE.equals(geometryType)) {
            value = field.getValue();
          }
          row.createCell(cellNumber).setCellValue(value);
          if (errorsMap != null) {
            Cell cell = row.createCell(cellNumber + 1);
            cell.setCellStyle(cs);
            cell.setCellValue(errorsMap.get(field.getId()));
          }
        }
        if (errorsMap != null) {
          Cell cell = row.createCell(nHeaders - 1);
          cell.setCellStyle(cs);
          cell.setCellValue(errorsMap.get(record.getId()));
        }
      }
    }
  }

  /**
   * Checks if is geometry field type.
   *
   * @param field the field
   * @return true, if is geometry field type
   */
  private boolean isGeometryFieldType(FieldValue field) {
    boolean geometryType = false;
    if (DataType.GEOMETRYCOLLECTION.equals(field.getType())
        || DataType.LINESTRING.equals(field.getType())
        || DataType.MULTILINESTRING.equals(field.getType())
        || DataType.MULTIPOINT.equals(field.getType())
        || DataType.MULTIPOLYGON.equals(field.getType()) || DataType.POINT.equals(field.getType())
        || DataType.POLYGON.equals(field.getType())) {
      geometryType = true;
    }
    return geometryType;
  }

  /**
   * Creates the sheet and headers.
   *
   * @param workbook the workbook
   * @param table the table
   * @param includeCountryCode the include country code
   * @param includeValidations the include validations
   * @param nameSheet the name sheet
   * @return the sheet
   */
  private Sheet createSheetAndHeaders(Workbook workbook, TableSchemaVO table,
      String includeCountryCode, boolean includeValidations, String nameSheet) {
    Sheet sheet = workbook.createSheet(nameSheet);
    List<FieldSchemaVO> fieldSchemas = table.getRecordSchema().getFieldSchema();
    // Set headers
    int nHeaders = 0;
    Row rowhead = sheet.createRow(0);

    if (null != includeCountryCode) {
      rowhead.createCell(nHeaders).setCellValue(includeCountryCode);
      nHeaders++;
    }
    Map<String, Integer> indexMap = new HashMap<>();

    for (FieldSchemaVO fieldSchema : fieldSchemas) {
      rowhead.createCell(nHeaders).setCellValue(fieldSchema.getName());
      indexMap.put(fieldSchema.getId(), nHeaders++);
      if (includeValidations) {
        rowhead.createCell(nHeaders).setCellValue(fieldSchema.getName() + " validations");
        nHeaders++;
      }
    }
    if (includeValidations) {
      rowhead.createCell(nHeaders).setCellValue("Record validations");
      nHeaders++;
    }
    return sheet;
  }

  /**
   * Map errors.
   *
   * @param failedValidationsByIdDataset the failed validations by id dataset
   * @return the map
   */
  private Map<String, String> mapErrors(FailedValidationsDatasetVO failedValidationsByIdDataset) {
    Map<String, String> errorsMap = new HashMap<>();
    for (Object error : failedValidationsByIdDataset.getErrors()) {
      ErrorsValidationVO castedError = (ErrorsValidationVO) error;
      String newError = castedError.getLevelError();
      String message = castedError.getMessage();
      String id = castedError.getIdObject();
      String value = errorsMap.putIfAbsent(id, newError + ": " + message);
      if (value != null) {
        insertIntoOrderedPosition(errorsMap, value, id, newError, message);
      }
    }
    return errorsMap;
  }

  /**
   * Insert into ordered position.
   *
   * @param errorsMap the errors map
   * @param value the value
   * @param id the id
   * @param newError the new error
   * @param message the message
   */
  private void insertIntoOrderedPosition(Map<String, String> errorsMap, String value, String id,
      String newError, String message) {
    switch (newError) {
      case "BLOCKER":
        errorsMap.put(id, newError + ": " + message + "\n" + value);
        break;
      case "ERROR":
        if (value.contains(ERROR)) {
          value = value.replaceFirst(ERROR, newError + ": " + message + "\nERROR:");
        } else if (value.contains(WARNING)) {
          value = value.replaceFirst(WARNING, newError + ": " + message + "\nWARNING:");
        } else if (value.contains(INFO)) {
          value = value.replaceFirst(INFO, newError + ": " + message + "\nINFO:");
        } else {
          value = value + "\n " + newError + ": " + message;
        }
        errorsMap.put(id, value);
        break;
      case "WARNING":
        if (value.contains(WARNING)) {
          value = value.replaceFirst(WARNING, newError + ": " + message + "\nWARNING:");
        } else if (value.contains(INFO)) {
          value = value.replaceFirst(INFO, newError + ": " + message + "\nINFO:");
        } else {
          value = value + "\n" + newError + ": " + message;
        }
        errorsMap.put(id, value);
        break;
      default:
        errorsMap.put(id, value + "\n" + newError + ": " + message);
        break;
    }
  }

}
