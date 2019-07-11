package org.eea.dataset.service.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
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

public class ExcelWriterStrategy implements WriterStrategy {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CSVWriterStrategy.class);

  private ParseCommon parseCommon;

  private String mimeType;

  public ExcelWriterStrategy(ParseCommon parseCommon, String mimeType) {
    super();
    this.parseCommon = parseCommon;
    this.mimeType = mimeType;
  }

  public String getMimeType() {
    return String.valueOf(mimeType);
  }

  public void setMimeType(String mimeType) {
    this.mimeType = String.valueOf(mimeType);
  }

  @Override
  public byte[] writeFile(Long dataflowId, Long partitionId, String idTableSchema) {

    DataSetSchemaVO dataset = parseCommon.getDataSetSchema(dataflowId);
    List<TableSchemaVO> tables = dataset.getTableSchemas();
    TableSchemaVO table = parseCommon.findTableSchema(idTableSchema, dataset);
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    if (null != table) {
      tables.clear();
      tables.add(table);
    }

    if (mimeType.equals("xls")) {
      try (HSSFWorkbook workbook = new HSSFWorkbook()) {
        for (TableSchemaVO tableSchema : tables) {
          writeHSSFSheet(workbook, tableSchema);
        }
        workbook.write(out);
      } catch (IOException e) {
        LOG_ERROR.error(e.getMessage());
      }
    } else {
      try (XSSFWorkbook workbook = new XSSFWorkbook()) {
        for (TableSchemaVO tableSchema : tables) {
          writeXSSFSheet(workbook, tableSchema);
        }
        workbook.write(out);
      } catch (IOException e) {
        LOG_ERROR.error(e.getMessage());
      }
    }

    return out.toByteArray();
  }

  public void writeHSSFSheet(HSSFWorkbook workbook, TableSchemaVO table) {

    HSSFSheet sheet = workbook.createSheet(table.getNameTableSchema());

    // Set headers
    int nHeaders = 0;
    HSSFRow rowhead = sheet.createRow(0);
    for (FieldSchemaVO field : table.getRecordSchema().getFieldSchema()) {
      rowhead.createCell(nHeaders++).setCellValue(field.getName());
    }

    // Set records
    int nRow = 1;
    for (RecordValue record : parseCommon.getRecordValues(table.getIdTableSchema())) {
      List<FieldValue> fields = record.getFields();
      HSSFRow row = sheet.createRow(nRow++);
      for (int i = 0; i < nHeaders; i++) {
        row.createCell(nRow).setCellValue(fields.get(i).getValue());
      }
      nRow++;
    }
  }

  public void writeXSSFSheet(XSSFWorkbook workbook, TableSchemaVO table) {

    XSSFSheet sheet = workbook.createSheet(table.getNameTableSchema());

    // Set headers
    int nHeaders = 0;
    XSSFRow rowhead = sheet.createRow(0);
    for (FieldSchemaVO field : table.getRecordSchema().getFieldSchema()) {
      rowhead.createCell(nHeaders++).setCellValue(field.getName());
    }

    // Set records
    int nRow = 1;
    for (RecordValue record : parseCommon.getRecordValues(table.getIdTableSchema())) {
      List<FieldValue> fields = record.getFields();
      XSSFRow row = sheet.createRow(nRow++);
      for (int i = 0; i < nHeaders; i++) {
        row.createCell(i).setCellValue(fields.get(nHeaders - 1 - i).getValue());
      }
    }
  }
}
