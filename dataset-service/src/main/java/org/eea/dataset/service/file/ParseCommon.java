package org.eea.dataset.service.file;

import java.util.List;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;

public class ParseCommon {

  /** The data set schema. */
  private DataSetSchemaVO dataSetSchema;

  /** The tables schema. */
  private List<TableSchemaVO> tablesSchema;

  /** The Constant TABLE_HEADER. */
  private static final String TABLE_HEADER = "_TABLE";


  /**
   * Find id table.
   *
   * @param tableName the table name
   * @return the string
   */
  public String findIdTable(String tableName) {
    // Find the Id of tableSchema in MongoDB
    String idTable = null;
    if (null != tablesSchema) {
      for (TableSchemaVO tableSchema : tablesSchema) {
        if (tableSchema.getNameTableSchema().equalsIgnoreCase(tableName)) {
          idTable = tableSchema.getIdTableSchema();
        }
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
  public String findIdRecord(String idTableMongo) {
    // Find the idrecordSchema of MongoDB
    if (null != findTableSchema(idTableMongo)) {
      TableSchemaVO tableS = findTableSchema(idTableMongo);
      return null != tableS ? tableS.getRecordSchema().getIdRecordSchema() : null;
    }
    return null;
  }

  /**
   * Find table schema.
   *
   * @param idTableMongo the id table mongo
   * @return the table schema
   */
  private TableSchemaVO findTableSchema(String idTableMongo) {
    // Find the tableSchema of MongoDB
    for (TableSchemaVO tableSchema : tablesSchema) {
      if (tableSchema.getIdTableSchema().equalsIgnoreCase(idTableMongo)) {
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
  public FieldSchemaVO findIdFieldSchema(String nameSchema, String idTablaSchema) {
    // Find the idFieldSchema of MongoDB
    TableSchemaVO recordSchemas = findTableSchema(idTablaSchema);
    RecordSchemaVO recordSchema = null != recordSchemas ? recordSchemas.getRecordSchema() : null;
    if (null != recordSchema) {
      List<FieldSchemaVO> fieldsSchemas = recordSchema.getFieldSchema();
      for (FieldSchemaVO fieldSchema : fieldsSchemas) {
        if (null != fieldSchema.getName() && fieldSchema.getName().equalsIgnoreCase(nameSchema)) {
          return fieldSchema;
        }
      }
    }
    return null;
  }


  /**
   * Gets the data set schema.
   *
   * @param dataflowId the dataflow id
   * @return
   * @return the data set schema
   */
  public DataSetSchemaVO getDataSetSchema(Long dataflowId,
      DatasetSchemaService datasetSchemaService) {
    // get data set schema of mongo DB
    if (null != dataflowId) {
      dataSetSchema = datasetSchemaService.getDataSchemaByIdFlow(dataflowId);
      if (null != dataSetSchema) {
        tablesSchema = dataSetSchema.getTableSchemas();
      }
    }
    return dataSetSchema;
  }


  /**
   * Checks if is header.
   *
   * @param value the value
   * @return the boolean
   */
  public Boolean isHeader(String value) {
    return TABLE_HEADER.equalsIgnoreCase(value.trim());
  }



}
