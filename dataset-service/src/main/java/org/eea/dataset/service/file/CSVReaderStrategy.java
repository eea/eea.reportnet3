package org.eea.dataset.service.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.file.interfaces.ReaderStrategy;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.TypeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * The Class CSVReaderStrategy.
 */
@Component
public class CSVReaderStrategy implements ReaderStrategy {

  /** The Constant PIPE_DELIMITER. */
  private static final char PIPE_DELIMITER = '|';

  /** The Constant TABLE_HEADER. */
  private static final String TABLE_HEADER = "_TABLE";

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(CSVReaderStrategy.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The schemas repository. */
  private SchemasRepository schemasRepository;

  /** The data set schema. */
  private DataSetSchema dataSetSchema;

  /** The headers. */
  private List<FieldSchemaVO> headers;

  /** The tables schema. */
  private List<TableSchema> tablesSchema;


  /**
   * Instantiates a new CSV reader strategy.
   *
   * @param schemasRepository the schemas repository
   */
  public CSVReaderStrategy(SchemasRepository schemasRepository) {
    this.schemasRepository = schemasRepository;
  }

  /**
   * Parses the file.
   *
   * @param inputStream the input stream
   * @param datasetId the dataset id
   * @param partitionId the partition id
   * @return the data set VO
   * @throws InvalidFileException the invalid file exception
   */
  @Override
  public DataSetVO parseFile(InputStream inputStream, Long dataflowId, Long partitionId) {
      throws InvalidFileException {
    try (Reader buf = new BufferedReader(new InputStreamReader(inputStream))) {
      return readLines(buf, datasetId, partitionId);
    } catch (IOException e) {
      LOG_ERROR.error(e.getMessage());
      throw new InvalidFileException(e);
    }
  }

  /**
   * Read lines.
   *
   * @param buf the reader
   * @param datasetId the dataset id
   * @param partitionId the partition id
   * @return the data set VO
   * @throws InvalidFileException the invalid file exception
   */
  private DataSetVO readLines(Reader buf, String datasetId, Long partitionId)
      throws InvalidFileException {
    String[] line;
    TableVO tableVO = new TableVO();
    List<TableVO> tables = new ArrayList<>();
    headers = new ArrayList<>();
    DataSetVO dataset = new DataSetVO();

    getDataSetSchema();
    CSVReader reader = initReader(buf);

    try {

      while ((line = reader.readNext()) != null) {
        List<String> values = Arrays.asList(line);
        if (null != values && !values.isEmpty()) {
          // Clear White Line
          if (line.length == 1 && line[0].isEmpty()) {
            continue;
          } else if (line.length == 1) {
            throw new InvalidFileException("Invalid Format File");
          }

          if (isHeader(values.get(0))) {
            headers = setHeaders(values);
          } else {
            tableVO = createTableVO(tableVO, tables, values, partitionId);
          }
        }
      }
      dataset.setTableVO(tables);
      dataset.setIdMongo(dataSetSchema.getIdDataSetSchema().toString());
    } catch (IOException e) {
      LOG_ERROR.error(e.getMessage());
      throw new InvalidFileException("Invalid Format File");
    }
    return dataset;

  }

  /**
   * Inits the reader.
   *
   * @param buf the buf
   * @return the CSV reader
   */
  private CSVReader initReader(Reader buf) {
    // Init CSV Library
    CSVParser csvParser = new CSVParserBuilder().withSeparator(PIPE_DELIMITER).build();
    return new CSVReaderBuilder(buf).withCSVParser(csvParser).build();
  }

  /**
   * Sets the headers.
   *
   * @param values the values
   * @return the list
   */
  private List<FieldSchemaVO> setHeaders(List<String> values) {
    List<FieldSchemaVO> headers = new ArrayList<>();

    for (String value : values) {
      if (!isHeader(value)) {
        FieldSchemaVO header = new FieldSchemaVO();
        header.setName(value);
        headers.add(header);
      }
    }
    return headers;
  }

  /**
   * Creates the table VO.
   *
   * @param tableVO the table VO
   * @param tables the tables
   * @param values the values
   * @param partitionId the partition id
   * @return the table VO
   * @throws InvalidFileException the invalid file exception
   */
  private TableVO createTableVO(TableVO tableVO, List<TableVO> tables, List<String> values,
      Long partitionId) throws InvalidFileException {
    // Create object Table and setter the attributes
    if (headers.isEmpty()) {
      throw new InvalidFileException("Invalid Format File");
    }
    if (!values.get(0).equals(tableVO.getName())) {
      tableVO = new TableVO();
      tableVO.setHeaders(headers);
      tableVO.setName(values.get(0));
      tableVO.setIdMongo(findIdTable(tableVO.getName()));
      tableVO.setRecords(createRecordsVO(values, partitionId, tableVO.getIdMongo()));
      tables.add(tableVO);

    } else {
      tableVO.getRecords().addAll(createRecordsVO(values, partitionId, tableVO.getIdMongo()));
    }
    return tableVO;
  }

  /**
   * Creates the records VO.
   *
   * @param values the values
   * @param partitionId the partition id
   * @param idTablaSchema the id tabla schema
   * @return the list
   */
  private List<RecordVO> createRecordsVO(List<String> values, Long partitionId,
      String idTablaSchema) {
    List<RecordVO> records = new ArrayList<>();
    RecordVO record = new RecordVO();
    record.setIdMongo(findIdRecord(idTablaSchema));
    record.setFields(createFieldsVO(values, partitionId, idTablaSchema));
    records.add(record);
    return records;
  }

  /**
   * Creates the fields VO.
   *
   * @param values the values
   * @param partitionId the partition id
   * @param idTablaSchema the id tabla schema
   * @return the list
   */
  private List<FieldVO> createFieldsVO(List<String> values, Long partitionId,
      String idTablaSchema) {
  
    List<FieldVO> fields = new ArrayList<>();
    values.size();
    int contAux = 0;
    for (String value : values.subList(1, values.size())) {
      FieldVO field = new FieldVO();
      FieldSchema fieldSchema = findIdFieldSchema(headers.get(contAux).getName(), idTablaSchema);
      headers.get(contAux).setId(fieldSchema.getIdFieldSchema().toString());
      field.setIdFieldSchema(fieldSchema.getIdFieldSchema().toString());
      field.setValue(value);
      field.setType(fieldSchema.getType().toString());
      fields.add(field);
      contAux++;
    }
  
    FieldVO fieldPartition = new FieldVO();
    fieldPartition.setType(TypeData.INTEGER.toString());
    fieldPartition.setValue(null != partitionId ? partitionId.toString() : "");
    fields.add(fieldPartition);
  
    return fields;
  }

  /**
   * Gets the data set schema.
   *
   * @return the data set schema
   */
  private void getDataSetSchema() {
    List<DataSetSchema> dataSetsSchemas = schemasRepository.findByIdDataFlow(new Long(1));
    if (null != dataSetsSchemas && !dataSetsSchemas.isEmpty()) {
      dataSetSchema = dataSetsSchemas.get(0);
      tablesSchema = dataSetSchema.getTableSchemas();
    }
  }


  /**
   * Find id table.
   *
   * @param tableName the table name
   * @return the string
   */
  private String findIdTable(String tableName) {
    String idTable = null;
    for (TableSchema tableSchema : tablesSchema) {
      if (tableSchema.getNameTableSchema().equalsIgnoreCase(tableName)) {
        idTable = tableSchema.getIdTableSchema().toString();
      }
    }
    return idTable;
  }

  /**
   * Find id record.
   *
   * @param idTableMongo the id table mongo
   * @return the string
   */
  private String findIdRecord(String idTableMongo) {
    if (null != findTableSchema(idTableMongo)) {
      return findTableSchema(idTableMongo).getRecordSchema().getIdRecordSchema().toString();
    }
    return null;
  }

  /**
   * Find table schema.
   *
   * @param idTableMongo the id table mongo
   * @return the table schema
   */
  private TableSchema findTableSchema(String idTableMongo) {
    String idRecord = null;
    for (TableSchema tableSchema : tablesSchema) {
      if (tableSchema.getIdTableSchema().toString().equalsIgnoreCase(idTableMongo)) {
        return tableSchema;
      }
    }
    return null;
  }



  /**
   * Find id field schema.
   *
   * @param nameSchema the name schema
   * @param idTablaSchema the id tabla schema
   * @return the field schema
   */
  private FieldSchema findIdFieldSchema(String nameSchema, String idTablaSchema) {
    RecordSchema recordSchema = findTableSchema(idTablaSchema).getRecordSchema();
    List<FieldSchema> FieldsSchemas = recordSchema.getFieldSchema();
    String idFieldSchema = null;
    for (FieldSchema fieldSchema : FieldsSchemas) {
      if (fieldSchema.getHeaderName().equalsIgnoreCase(nameSchema)) {
        return fieldSchema;
      }
    }
    return null;
  }

  /**
   * Checks if is header.
   *
   * @param value the value
   * @return the boolean
   */
  private Boolean isHeader(String value) {
    return TABLE_HEADER.equalsIgnoreCase(value.trim());
  }


}
