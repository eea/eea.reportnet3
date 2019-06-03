package org.eea.dataset.service.file;

import java.util.List;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class ParseCommon.
 */
@Component
public class ParseCommon {


  /**
   * The Constant TABLE_HEADER.
   */
  private static final String TABLE_HEADER = "_TABLE";

  /**
   * The data set schema service.
   */
  @Autowired
  private DatasetSchemaService dataSetSchemaService;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ParseCommon.class);


  /**
   * Find id table.
   *
   * @param tableName the table name
   * @param dataSetSchema the data set schema
   * @return the string
   */
  public String findIdTable(String tableName, DataSetSchemaVO dataSetSchema) {
    // Find the Id of tableSchema in MongoDB
    String idTable = null;
    List<TableSchemaVO> tablesSchema = null;
    if (null != dataSetSchema) {
      tablesSchema = dataSetSchema.getTableSchemas();
    }
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
   * @param dataSetSchema the data set schema
   * @return the string
   */
  public String findIdRecord(String idTableMongo, DataSetSchemaVO dataSetSchema) {
    // Find the idrecordSchema from MongoDB
    TableSchemaVO tableS = findTableSchema(idTableMongo, dataSetSchema);
    String result = null;
    if (null != tableS) {
      result =
          null != tableS.getRecordSchema() ? tableS.getRecordSchema().getIdRecordSchema() : null;
    }
    return result;
  }

  /**
   * Find table schema.
   *
   * @param idTableSchema the id table mongo
   * @param dataSetSchema the data set schema
   * @return the table schema
   */
  private TableSchemaVO findTableSchema(String idTableSchema, DataSetSchemaVO dataSetSchema) {
    // Find the tableSchema of MongoDB
    List<TableSchemaVO> tablesSchema = null;
    if (null != dataSetSchema) {
      tablesSchema = dataSetSchema.getTableSchemas();
    }
    if (null != tablesSchema) {
      for (TableSchemaVO tableSchema : tablesSchema) {
      if (tableSchema.getIdTableSchema().equalsIgnoreCase(idTableSchema)) {
          return tableSchema;
        }
      }
    }
    return null;
  }


  /**
   * Find id field schema.
   *
   * @param nameSchema the name schema
   * @param idTablaSchema the id tabla schema
   * @param dataSetSchema the data set schema
   * @return the field schema
   */
  public FieldSchemaVO findIdFieldSchema(String nameSchema, String idTablaSchema,
      DataSetSchemaVO dataSetSchema) {
    // Find the idFieldSchema
    TableSchemaVO recordSchemas = findTableSchema(idTablaSchema, dataSetSchema);
    RecordSchemaVO recordSchema = null != recordSchemas ? recordSchemas.getRecordSchema() : null;
    if (null != recordSchema && null != recordSchema.getFieldSchema()) {
      for (FieldSchemaVO fieldSchema : recordSchema.getFieldSchema()) {
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
   * @return the data set schema
   */
  public DataSetSchemaVO getDataSetSchema(Long dataflowId) {
    LOG.info("Getting DataSchema from Mongo DB");
    DataSetSchemaVO dataSetSchema = null;
    // get data set schema of mongo DB
    if (null != dataflowId) {
      dataSetSchema = dataSetSchemaService.getDataSchemaByIdFlow(dataflowId);
    }
    return dataSetSchema;
  }


  /**
   * Checks if is header.
   *
   * @param value the value
   *
   * @return the boolean
   */
  public Boolean isHeader(String value) {
    return TABLE_HEADER.equalsIgnoreCase(value.trim());
  }


}
