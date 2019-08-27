package org.eea.dataset.service.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
   * @param idTableSchema the id table schema
   * @return the byte[]
   */
  @Override
  public byte[] writeFile(Long dataflowId, Long datasetId, String idTableSchema) {

    DataSetSchemaVO dataset = fileCommon.getDataSetSchema(dataflowId);
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    // Get all tablesSchemas for the case the given idTableSchema doesn't exist
    List<TableSchemaVO> tables =
        dataset.getTableSchemas() != null ? dataset.getTableSchemas() : new ArrayList<>();
    TableSchemaVO table = fileCommon.findTableSchema(idTableSchema, dataset);

    // If the given idTableSchema exists, replace all tables with it
    if (null != table) {
      tables.clear();
      tables.add(table);
    }

    try (Workbook workbook = createWorkbook()) {

      LOG.info("Starting writing Excel({}) file", mimeType);

      // Add one sheet per table
      for (TableSchemaVO tableSchema : tables) {
        writeSheet(workbook, tableSchema, datasetId);
      }

      workbook.write(out);

      LOG.info("Finishing writing Excel({}) file", mimeType);

    } catch (IOException e) {
      LOG_ERROR.error(e.getMessage());
    }

    return out.toByteArray();
  }

  /**
   * Creates the correct Workbook object according to the file extension.
   *
   * @return A new Workbook object.
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private Workbook createWorkbook() throws IOException {

    switch (mimeType) {
      case "xls":
        return new HSSFWorkbook();
      case "xlsx":
        return new XSSFWorkbook();
      default:
        throw new IOException("Unknow MIME type: " + mimeType);
    }
  }

  /**
   * Writes a sheet containing a DataSet table into the given workbook.
   *
   * @param workbook the workbook
   * @param table the table
   * @param datasetId the DataSet id
   */
  private void writeSheet(Workbook workbook, TableSchemaVO table, Long datasetId) {

    Sheet sheet = workbook.createSheet(table.getNameTableSchema());
    List<FieldSchemaVO> fieldSchemas = table.getRecordSchema().getFieldSchema();

    // Used to map each fieldValue with the correct fieldSchema
    Map<String, Integer> indexMap = new HashMap<>();

    // Set headers
    int nHeaders = 0;
    Row rowhead = sheet.createRow(0);
    for (FieldSchemaVO fieldSchema : fieldSchemas) {
      rowhead.createCell(nHeaders).setCellValue(fieldSchema.getName());
      indexMap.put(fieldSchema.getId(), nHeaders++);
    }

    // Set records
    int nRow = 1;
    for (RecordValue record : fileCommon.getRecordValues(datasetId, table.getIdTableSchema())) {

      Row row = sheet.createRow(nRow++);
      List<FieldValue> fields = record.getFields();
      int nextUnknownCellNumber = nHeaders;

      for (int i = 0; i < fields.size(); i++) {
        FieldValue field = fields.get(i);
        Integer cellNumber = indexMap.get(field.getIdFieldSchema());

        if (cellNumber == null) {
          cellNumber = nextUnknownCellNumber++;
        }

        row.createCell(cellNumber).setCellValue(field.getValue());
      }
    }
  }

}
