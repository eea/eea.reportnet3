package org.eea.dataset.service.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.service.file.interfaces.WriterStrategy;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ExcelWriterStrategy.
 */
public class ExcelWriterStrategy implements WriterStrategy {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CSVWriterStrategy.class);

  /** The parse common. */
  private ParseCommon parseCommon;

  /** The mime type. */
  private String mimeType;

  /**
   * Instantiates a new excel writer strategy.
   *
   * @param parseCommon the parse common
   * @param mimeType the mime type
   */
  public ExcelWriterStrategy(ParseCommon parseCommon, String mimeType) {
    super();
    this.parseCommon = parseCommon;
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
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @return the byte[]
   */
  @Override
  public byte[] writeFile(Long dataflowId, Long datasetId, String idTableSchema) {

    DataSetSchemaVO dataset = parseCommon.getDataSetSchema(dataflowId);
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    // Get all tablesSchemas for the case the given idTableSchema doesn't exist
    List<TableSchemaVO> tables =
        dataset.getTableSchemas() == null ? dataset.getTableSchemas() : new ArrayList<>();
    TableSchemaVO table = parseCommon.findTableSchema(idTableSchema, dataset);

    // If the given idTableSchema exists, replace all tables with it
    if (null != table) {
      tables.clear();
      tables.add(table);
    }

    // Write to xls
    if (mimeType.equals("xls")) {
      LOG.info("Start writing xls file");
      try (HSSFWorkbook workbook = new HSSFWorkbook()) {
        for (TableSchemaVO tableSchema : tables) {
          writeHSSFSheet(workbook, tableSchema, datasetId);
        }
        workbook.write(out);
      } catch (IOException e) {
        LOG_ERROR.error(e.getMessage());
      }
      // Write to xlsx
    } else {
      LOG.info("Start writing xlsx file");
      try (XSSFWorkbook workbook = new XSSFWorkbook()) {
        for (TableSchemaVO tableSchema : tables) {
          writeXSSFSheet(workbook, tableSchema, datasetId);
        }
        workbook.write(out);
      } catch (IOException e) {
        LOG_ERROR.error(e.getMessage());
      }
    }

    LOG.info("Finish writing xlsx file");

    return out.toByteArray();
  }

  /**
   * Write HSSF sheet.
   *
   * @param workbook the workbook
   * @param table the table
   */
  public void writeHSSFSheet(HSSFWorkbook workbook, TableSchemaVO table, Long datasetId) {

    LOG.info("Writing table " + table.getNameTableSchema());

    HSSFSheet sheet = workbook.createSheet(table.getNameTableSchema());
    List<FieldSchemaVO> fieldSchemas = table.getRecordSchema().getFieldSchema();
    Map<String, Integer> mapa = new HashMap<>();

    // Set headers
    int nHeaders = 0;
    HSSFRow rowhead = sheet.createRow(0);
    for (FieldSchemaVO fieldSchema : fieldSchemas) {
      rowhead.createCell(nHeaders).setCellValue(fieldSchema.getName());
      mapa.put(fieldSchema.getId(), nHeaders++);
    }

    // Set records
    int nRow = 1;
    for (RecordValue record : parseCommon.getRecordValues(datasetId, table.getIdTableSchema())) {

      HSSFRow row = sheet.createRow(nRow++);
      List<FieldValue> fields = record.getFields();

      for (int i = 0; i < nHeaders; i++) {
        FieldValue field = fields.get(i);
        row.createCell(mapa.get(field.getIdFieldSchema())).setCellValue(field.getValue());
      }
    }
  }

  /**
   * Write XSSF sheet.
   *
   * @param workbook the workbook
   * @param table the table
   */
  public void writeXSSFSheet(XSSFWorkbook workbook, TableSchemaVO table, Long datasetId) {

    LOG.info("Writing table " + table.getNameTableSchema());

    XSSFSheet sheet = workbook.createSheet(table.getNameTableSchema());
    List<FieldSchemaVO> fieldSchemas = table.getRecordSchema().getFieldSchema();
    Map<String, Integer> mapa = new HashMap<>();

    // Set headers
    int nHeaders = 0;
    XSSFRow rowhead = sheet.createRow(0);
    for (FieldSchemaVO fieldSchema : fieldSchemas) {
      rowhead.createCell(nHeaders).setCellValue(fieldSchema.getName());
      mapa.put(fieldSchema.getId(), nHeaders++);
    }

    // Set records
    int nRow = 1;
    for (RecordValue record : parseCommon.getRecordValues(datasetId, table.getIdTableSchema())) {

      XSSFRow row = sheet.createRow(nRow++);
      List<FieldValue> fields = record.getFields();

      for (int i = 0; i < nHeaders; i++) {
        FieldValue field = fields.get(i);
        // Comprobar que el field contiene un idFieldSchema para posicionarlo donde corresponda
        // (consultar la libreta con las anotaciones de JL)
        row.createCell(mapa.get(field.getIdFieldSchema())).setCellValue(field.getValue());
      }
    }
  }
}
