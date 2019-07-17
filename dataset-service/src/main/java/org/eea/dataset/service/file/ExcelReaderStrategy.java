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

public class ExcelReaderStrategy implements ReaderStrategy {

  /** The parse common. */
  private ParseCommon parseCommon;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(CSVReaderStrategy.class);

  /**
   * Instantiates a new excel reader strategy.
   *
   * @param parseCommon the parse common
   */
  public ExcelReaderStrategy(final ParseCommon parseCommon) {
    this.parseCommon = parseCommon;
  }

  /**
   * Parses the file reading each sheet of the file and creating a table with the sheet's name.
   * idTableSchema is not used in this method.
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

    LOG.info("Starting Excel file read");

    DataSetSchemaVO dataSetSchema = parseCommon.getDataSetSchema(dataflowId);

    List<TableVO> tables;
    DataSetVO dataset;

    // Open Excel file
    try (Workbook workbook = WorkbookFactory.create(inputStream)) {

      // Initialize the data formatter for cells content
      DataFormatter dataFormatter = new DataFormatter();

      tables = readOverSheets(partitionId, dataSetSchema, workbook, dataFormatter);

      // Create a new dataset (DataSetVO)
      dataset = createDataSetVO(dataSetSchema, tables);

      inputStream.close();

      LOG.info("Ending Excel file read");

      return dataset;

    } catch (EncryptedDocumentException | InvalidFormatException | IOException e) {
      throw new InvalidFileException(InvalidFileException.ERROR_MESSAGE, e);
    }
  }

  /**
   * Read over sheets.
   *
   * @param partitionId the partition id
   * @param dataSetSchema the data set schema
   * @param workbook the workbook
   * @param dataFormatter the data formatter
   * @return the list
   */
  private List<TableVO> readOverSheets(Long partitionId, DataSetSchemaVO dataSetSchema,
      Workbook workbook, DataFormatter dataFormatter) {

    List<TableVO> tables = new ArrayList<>();
    String idTableSchema;
    List<FieldSchemaVO> headers;
    List<RecordVO> records;

    for (Sheet sheet : workbook) {

      idTableSchema = parseCommon.getIdTableSchema(sheet.getSheetName(), dataSetSchema);

      // Get the row iterator
      Iterator<Row> rowIterator = sheet.rowIterator();

      // Get the first row (it should contain headers)
      Row headersRow = rowIterator.next();

      // Read and create headers (FieldSchemaVO)
      headers = readHeaders(headersRow, dataFormatter, idTableSchema, dataSetSchema);

      // Read and create records (RecordVO)
      records = readRecords(rowIterator, headers, dataFormatter, partitionId, idTableSchema,
          dataSetSchema);

      tables.add(createTableVO(records, idTableSchema));
    }

    return tables;
  }

  /**
   * Read headers from a given Row object.
   *
   * @param headersRow the headers row
   * @param dataFormatter the data formatter
   * @param idTableSchema the id table schema
   * @param dataSetSchema the data set schema
   * @return the list
   */
  private List<FieldSchemaVO> readHeaders(Row headersRow, DataFormatter dataFormatter,
      String idTableSchema, DataSetSchemaVO dataSetSchema) {

    LOG.info("Reading headers");

    List<FieldSchemaVO> headers = new ArrayList<>();
    FieldSchemaVO header;
    String value;

    // Create a header from each cell
    for (Cell cell : headersRow) {

      // Read and parse the cell content
      value = dataFormatter.formatCellValue(cell);

      // Check if the cell is empty to stop adding headers
      if (value.isEmpty()) {
        break;
      }

      // Search the FieldSchema with the given name (value)
      header = parseCommon.findIdFieldSchema(value, idTableSchema, dataSetSchema);

      // Create a new FieldSchema if it wasn't found
      if (header == null) {
        header = new FieldSchemaVO();
        header.setName(value);
      }

      headers.add(header);
    }

    return headers;
  }

  /**
   * Read records from a given Row Iterator.
   *
   * @param rowIterator the row iterator
   * @param headers the headers
   * @param dataFormatter the data formatter
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @param dataSetSchema the data set schema
   * @return the list
   */
  private List<RecordVO> readRecords(Iterator<Row> rowIterator, List<FieldSchemaVO> headers,
      DataFormatter dataFormatter, Long partitionId, String idTableSchema,
      DataSetSchemaVO dataSetSchema) {

    LOG.info("Reading records");

    List<RecordVO> records = new ArrayList<>();
    int headersSize = headers.size();

    // Create a new record from each row
    while (rowIterator.hasNext()) {

      boolean rowIsEmpty = true;

      // Get the next row
      Row recordRow = rowIterator.next();
      RecordVO record = new RecordVO();
      List<FieldVO> fields = new ArrayList<>();

      // Read each cell and create a field (FieldVO)
      for (int i = 0; i < headersSize; i++) {
        FieldVO field = new FieldVO();
        field.setIdFieldSchema(headers.get(i).getId());
        field.setType(headers.get(i).getType());
        field.setValue(dataFormatter.formatCellValue(recordRow.getCell(i)));
        fields.add(field);
        if (!field.getValue().isEmpty()) {
          rowIsEmpty = false;
        }
      }

      // Create the new record if the entire row is not empty
      if (!rowIsEmpty) {
        record.setFields(fields);
        record.setIdRecordSchema(parseCommon.findIdRecord(idTableSchema, dataSetSchema));
        record.setDatasetPartitionId(partitionId);
        records.add(record);
      }
    }

    return records;
  }

  /**
   * Creates the table VO.
   *
   * @param records the records
   * @param idTableSchema the id table schema
   * @return the table VO
   */
  private TableVO createTableVO(List<RecordVO> records, String idTableSchema) {

    TableVO tableVO = new TableVO();

    tableVO.setRecords(records);
    tableVO.setIdTableSchema(idTableSchema);

    return tableVO;
  }

  /**
   * Creates the DataSetVO.
   *
   * @param dataSetSchema the data set schema
   * @param records the records
   * @param idTableSchema the id table schema
   * @return the data set VO
   */
  private DataSetVO createDataSetVO(DataSetSchemaVO dataSetSchema, List<TableVO> tables) {

    LOG.info("Creating DataSetVO");

    DataSetVO dataset = new DataSetVO();

    dataset.setTableVO(tables);

    // Set the idDatasetSchema if the dataSetSchema exists
    if (dataSetSchema != null) {
      dataset.setIdDatasetSchema(dataSetSchema.getIdDataSetSchema());
    }

    return dataset;
  }
}
