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
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.NoArgsConstructor;

/**
 * The Class ExcelReaderStrategy.
 */
@NoArgsConstructor
public class ExcelReaderStrategy implements ReaderStrategy {

  /**
   * The file common.
   */
  private FileCommonUtils fileCommon;

  /**
   * The dataset id.
   */
  private Long datasetId;

  /**
   * The field max length.
   */
  private int fieldMaxLength;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ExcelReaderStrategy.class);

  /**
   * the provider Code
   */
  private String providerCode;

  /**
   * Instantiates a new excel reader strategy.
   *
   * @param fileCommon the file common
   * @param datasetId the dataset id
   * @param fieldMaxLength the field max length
   */
  public ExcelReaderStrategy(final FileCommonUtils fileCommon, Long datasetId, int fieldMaxLength) {
    this.fileCommon = fileCommon;
    this.datasetId = datasetId;
    this.fieldMaxLength = fieldMaxLength;
    this.providerCode = "";
  }

  public ExcelReaderStrategy(FileCommonUtils fileCommon, Long datasetId, int fieldMaxLength,
      String providerCode) {
    this.fileCommon = fileCommon;
    this.datasetId = datasetId;
    this.fieldMaxLength = fieldMaxLength;
    this.providerCode = providerCode;
  }

  /**
   * Parses the file.
   *
   * @param inputStream the input stream
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   *
   * @return the data set VO
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public DataSetVO parseFile(InputStream inputStream, Long dataflowId, Long partitionId,
      String idTableSchema) throws EEAException {

    DataSetSchemaVO dataSetSchema = fileCommon.getDataSetSchema(dataflowId, datasetId);

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
   *
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
   *
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
   *
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
      List<String> idSchema = new ArrayList<>();

      // Reads the same number of cells as headers we have
      for (int i = 0; i < headersSize; i++) {
        String value = dataFormatter.formatCellValue(recordRow.getCell(i));
        FieldSchemaVO fieldSchemaVO = headers.get(i);
        FieldVO field = new FieldVO();
        field.setIdFieldSchema(fieldSchemaVO.getId());
        field.setType(fieldSchemaVO.getType());

        if (null == field.getType() && value.length() >= fieldMaxLength) {
          field.setValue(value.substring(0, fieldMaxLength));
        } else {
          switch (field.getType()) {
            case ATTACHMENT:
              field.setValue("");
              break;
            case POINT:
              break;
            case LINESTRING:
              break;
            case POLYGON:
              break;
            case MULTIPOINT:
              break;
            case MULTILINESTRING:
              break;
            case MULTIPOLYGON:
              break;
            case GEOMETRYCOLLECTION:
              break;
            default:
              if (value.length() >= fieldMaxLength) {
                field.setValue(value.substring(0, fieldMaxLength));
              }
          }
        }

        if (field.getIdFieldSchema() != null) {
          fields.add(field);
          idSchema.add(field.getIdFieldSchema());
        }
      }
      setMissingField(fileCommon.findFieldSchemas(idTableSchema, dataSetSchema), fields, idSchema);

      // Creates the record with the fields readen
      record.setFields(fields);
      record.setIdRecordSchema(fileCommon.findIdRecord(idTableSchema, dataSetSchema));
      record.setDatasetPartitionId(partitionId);
      record.setDataProviderCode(providerCode);
      records.add(record);
    }

    return records;
  }

  /**
   * Sets the missing field.
   *
   * @param headersSchema the headers schema
   * @param fields the fields
   * @param idSchema the id schema
   */
  private void setMissingField(List<FieldSchemaVO> headersSchema, final List<FieldVO> fields,
      List<String> idSchema) {
    headersSchema.stream().forEach(header -> {
      if (!idSchema.contains(header.getId())) {
        final FieldVO field = new FieldVO();
        field.setIdFieldSchema(header.getId());
        field.setType(header.getType());
        field.setValue("");
        fields.add(field);
      }
    });
  }

  /**
   * Creates the data set VO.
   *
   * @param dataSetSchema the data set schema
   * @param tables the tables
   *
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
