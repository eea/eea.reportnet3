package org.eea.dataset.service.file;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
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
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.service.file.interfaces.ReaderStrategy;
import org.eea.exception.EEAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.NoArgsConstructor;

/**
 * The Class ExcelReaderStrategy.
 */

/**
 * Instantiates a new excel reader strategy.
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

  /** the provider Code. */
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

  /**
   * Instantiates a new excel reader strategy.
   *
   * @param fileCommon the file common
   * @param datasetId the dataset id
   * @param fieldMaxLength the field max length
   * @param providerCode the provider code
   */
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
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param replace the replace
   * @param schema the schema
   * @return the data set VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void parseFile(InputStream inputStream, Long dataflowId, Long partitionId,
      String idTableSchema, Long datasetId, String fileName, boolean replace, DataSetSchema schema)
      throws EEAException {

    try (Workbook workbook = WorkbookFactory.create(inputStream)) {

      List<TableValue> tables = new ArrayList<>();

      if (null == idTableSchema) {
        LOG.info("Reading all Excel's file pages");
        for (Sheet sheet : workbook) {
          tables.add(createTable(sheet, fileCommon.getIdTableSchema(sheet.getSheetName(), schema),
              schema, partitionId));
        }
      } else {
        LOG.info("Reading the first Excel's file page");
        tables.add(createTable(workbook.getSheetAt(0), idTableSchema, schema, partitionId));
      }

      LOG.info("Finishing reading Exel file");
      createDataSet(schema, tables, idTableSchema, fileName, replace, schema);
    } catch (EncryptedDocumentException | InvalidFormatException | IOException | SQLException
        | IllegalArgumentException e) {
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
  private TableValue createTable(Sheet sheet, String idTableSchema, DataSetSchema dataSetSchema,
      Long partitionId) {

    TableValue table = new TableValue();
    Iterator<Row> rows = sheet.rowIterator();
    Row headersRow = rows.next();
    List<FieldSchema> headers = readHeaders(headersRow, idTableSchema, dataSetSchema);
    List<RecordValue> records =
        readRecords(rows, headers, partitionId, idTableSchema, dataSetSchema, table);

    table.setRecords(records);
    table.setIdTableSchema(idTableSchema);

    return table;
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
  private List<FieldSchema> readHeaders(Row headersRow, String idTableSchema,
      DataSetSchema dataSetSchema) {

    DataFormatter dataFormatter = new DataFormatter();
    List<FieldSchema> headers = new ArrayList<>();

    for (Cell cell : headersRow) {

      String value = dataFormatter.formatCellValue(cell);
      FieldSchema header = fileCommon.findIdFieldSchema(value, idTableSchema, dataSetSchema);

      if (header == null) {
        header = new FieldSchema();
        header.setHeaderName(value);
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
   * @param tableValue the table value
   * @return the list
   */
  private List<RecordValue> readRecords(Iterator<Row> rows, List<FieldSchema> headers,
      Long partitionId, String idTableSchema, DataSetSchema dataSetSchema, TableValue tableValue) {

    DataFormatter dataFormatter = new DataFormatter();
    List<RecordValue> records = new ArrayList<>();
    int headersSize = headers.size();

    while (rows.hasNext()) {

      Row recordRow = rows.next();
      RecordValue record = new RecordValue();
      List<FieldValue> fields = new ArrayList<>();
      List<String> idSchema = new ArrayList<>();

      // Reads the same number of cells as headers we have
      for (int i = 0; i < headersSize; i++) {
        String value = dataFormatter.formatCellValue(recordRow.getCell(i));
        FieldSchema fieldSchema = headers.get(i);
        FieldValue field = new FieldValue();
        if (null != fieldSchema && null != fieldSchema.getIdFieldSchema()) {
          field.setIdFieldSchema(fieldSchema.getIdFieldSchema().toString());
          field.setType(fieldSchema.getType());
          field.setRecord(record);
          field.setValue(value);
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
      }
      setMissingField(fileCommon.findFieldSchemas(idTableSchema, dataSetSchema), fields, idSchema);

      // Creates the record with the fields readen
      record.setFields(fields);
      record.setIdRecordSchema(fileCommon.findIdRecord(idTableSchema, dataSetSchema));
      record.setDatasetPartitionId(partitionId);
      record.setDataProviderCode(providerCode);
      record.setTableValue(tableValue);
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
  private void setMissingField(List<FieldSchema> headersSchema, final List<FieldValue> fields,
      List<String> idSchema) {
    headersSchema.stream().forEach(header -> {
      if (!idSchema.contains(header.getIdFieldSchema().toString())) {
        final FieldValue field = new FieldValue();
        field.setIdFieldSchema(header.getIdFieldSchema().toString());
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
   * @param idTableSchema the id table schema
   * @param fileName the file name
   * @param replace the replace
   * @param schema the schema
   * @return the data set VO
   * @throws Exception
   */
  private void createDataSet(DataSetSchema dataSetSchema, List<TableValue> tables,
      String idTableSchema, String fileName, boolean replace, DataSetSchema schema)
      throws EEAException, IOException, SQLException {
    try {

      DatasetValue dataset = new DatasetValue();

      dataset.setTableValues(tables);

      if (dataSetSchema != null) {
        dataset.setIdDatasetSchema(dataSetSchema.getIdDataSetSchema().toString());
      }
      boolean manageFixedRecords =
          fileCommon.schemaContainsFixedRecords(datasetId, dataSetSchema, idTableSchema);
      fileCommon.persistImportedDataset(idTableSchema, datasetId, fileName, replace, schema,
          dataset, manageFixedRecords);
    } catch (EEAException | IOException | SQLException e) {
      LOG.error("error persisting excel file", e);
      throw e;
    }
  }
}
