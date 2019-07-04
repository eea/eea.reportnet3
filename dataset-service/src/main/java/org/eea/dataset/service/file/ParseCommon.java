package org.eea.dataset.service.file;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.repository.RecordRepository;
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
   * The data set schema service.
   */
  @Autowired
  private DatasetSchemaService dataSetSchemaService;


  @Autowired
  private RecordRepository recordRepository;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ParseCommon.class);


  /**
   * Find id record.
   *
   * @param idTableMongo the id table mongo
   * @param dataSetSchema the data set schema
   *
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
   *
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
   *
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
   *
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

  public String getTableName(String idTableSchema, DataSetSchemaVO dataSetSchema) {
    TableSchemaVO table = findTableSchema(idTableSchema, dataSetSchema);
    return table != null ? table.getNameTableSchema() : idTableSchema;
  }

  public List<FieldSchemaVO> getFieldSchemas(String idTableSchema, DataSetSchemaVO dataSetSchema) {
    TableSchemaVO table = findTableSchema(idTableSchema, dataSetSchema);
    return table != null && table.getRecordSchema() != null
        ? table.getRecordSchema().getFieldSchema()
        : null;
  }

  public List<RecordValue> getRecordValues(String idTableSchema) {
    return sanitizeRecords(recordRepository.findByTableValueIdTableSchema(idTableSchema));
  }


  // public String[] getListFieldsValues(String idTableSchema, DataSetSchemaVO dataSetSchema) {
  // List<FieldValue> fieldValues;
  // getFieldSchemas(idTableSchema, dataSetSchema).stream().forEach(fieldSchema -> {
  // List<FieldValue> taka = fieldRepository.findByIdFieldSchema(fieldSchema.getId());
  // });
  //
  // }

  // public List<FieldValue> getListFieldsValues() {
  //
  // }

  private List<RecordValue> sanitizeRecords(List<RecordValue> records) {
    List<RecordValue> sanitizedRecords = new ArrayList<>();
    Set<Long> processedRecords = new HashSet<>();
    for (RecordValue recordValue : records) {
      if (!processedRecords.contains(recordValue.getId())) {
        processedRecords.add(recordValue.getId());
        recordValue.getFields().stream().forEach(field -> field.setFieldValidations(null));
        sanitizedRecords.add(recordValue);
      }

    }
    return sanitizedRecords;

  }


}
