package org.eea.dataset.service.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.service.file.interfaces.ReaderStrategy;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ExcelReaderStrategy.
 */
public class ExcelReaderStrategy implements ReaderStrategy {

  /** The file common. */
  private FileCommonUtils fileCommon;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(CSVReaderStrategy.class);

  /**
   * Instantiates a new excel reader strategy.
   *
   * @param fileCommon the file common
   */
  public ExcelReaderStrategy(final FileCommonUtils fileCommon) {
    this.fileCommon = fileCommon;
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
   */
  @Override
  public DataSetVO parseFile(InputStream inputStream, Long dataflowId, Long partitionId,
      String idTableSchema) throws InvalidFileException {

    DataSetSchemaVO dataSetSchema = fileCommon.getDataSetSchema(dataflowId);

    try (Workbook workbook = WorkbookFactory.create(inputStream)) {

      List<TableVO> tables = new ArrayList<>();

      if (null == idTableSchema) {
        LOG.info("Reading all Excel's file pages");
        for (Sheet sheet : workbook) {
          tables.add(
              createTable(sheet, fileCommon.getIdTableSchema(sheet.getSheetName(), dataSetSchema),
                  dataSetSchema, partitionId));
        }
      } else {
        LOG.info("Reading the first Excel's file page");
        tables.add(createTable(workbook.getSheetAt(0), idTableSchema, dataSetSchema, partitionId));
      }

      LOG.info("Finishing reading Exel file");
      return createDataSetVO(dataSetSchema, tables);

    } catch (EncryptedDocumentException | InvalidFormatException | IllegalArgumentException
        | IOException e) {
      throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE, e);
    }
  }

  /**
   * Creates the table.
   *
   * @param sheet the sheet
   * @param idTableSchema the id table schema
   * @param dataSetSchema the data set schema
   * @param partitionId the partition id
   * @return the table VO
   */
  private TableVO createTable(Sheet sheet, String idTableSchema, DataSetSchemaVO dataSetSchema,
      Long partitionId) {

    TableVO tableVO = new TableVO();
    Iterator<Row> rows = sheet.rowIterator();
    Row headersRow = rows.next();
    List<FieldSchemaVO> headers = readHeaders(headersRow, idTableSchema, dataSetSchema);
    List<RecordVO> records = readRecords(rows, headers, partitionId, idTableSchema, dataSetSchema);

    tableVO.setRecords(records);
    tableVO.setIdTableSchema(idTableSchema);

    return tableVO;
  }

  /**
   * Read headers.
   *
   * @param headersRow the headers row
   * @param idTableSchema the id table schema
   * @param dataSetSchema the data set schema
   * @return the list
   */
  private List<FieldSchemaVO> readHeaders(Row headersRow, String idTableSchema,
      DataSetSchemaVO dataSetSchema) {

    DataFormatter dataFormatter = new DataFormatter();
    List<FieldSchemaVO> headers = new ArrayList<>();

    for (Cell cell : headersRow) {

      String value = dataFormatter.formatCellValue(cell);
      FieldSchemaVO header = fileCommon.findIdFieldSchema(value, idTableSchema, dataSetSchema);

      if (header == null) {
        header = new FieldSchemaVO();
        header.setName(value);
      }

      headers.add(header);
    }

    return headers;
  }

  /**
   * Read records.
   *
   * @param rows the rows
   * @param headers the headers
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @param dataSetSchema the data set schema
   * @return the list
   */
  private List<RecordVO> readRecords(Iterator<Row> rows, List<FieldSchemaVO> headers,
      Long partitionId, String idTableSchema, DataSetSchemaVO dataSetSchema) {

    DataFormatter dataFormatter = new DataFormatter();
    List<RecordVO> records = new ArrayList<>();
    int headersSize = headers.size();

    while (rows.hasNext()) {

      Row recordRow = rows.next();
      RecordVO record = new RecordVO();
      List<FieldVO> fields = new ArrayList<>();

      for (int i = 0; i < headersSize; i++) {
        FieldVO field = new FieldVO();
        field.setIdFieldSchema(headers.get(i).getId());
        field.setType(headers.get(i).getType());
        field.setValue(dataFormatter.formatCellValue(recordRow.getCell(i)));
        fields.add(field);
      }

      record.setFields(fields);
      record.setIdRecordSchema(fileCommon.findIdRecord(idTableSchema, dataSetSchema));
      record.setDatasetPartitionId(partitionId);
      records.add(record);
    }

    return records;
  }

  /**
   * Creates the data set VO.
   *
   * @param dataSetSchema the data set schema
   * @param tables the tables
   * @return the data set VO
   */
  private DataSetVO createDataSetVO(DataSetSchemaVO dataSetSchema, List<TableVO> tables) {

    DataSetVO dataset = new DataSetVO();

    dataset.setTableVO(tables);

    if (dataSetSchema != null) {
      dataset.setIdDatasetSchema(dataSetSchema.getIdDataSetSchema());
    }

    return dataset;
  }
}
