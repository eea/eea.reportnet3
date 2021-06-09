package org.eea.dataset.service.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.validation.ValidationController.ValidationControllerZuul;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.multitenancy.DatasetId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * The Class FileCommonUtils.
 */
@Component
public class FileCommonUtils {


  /**
   * The data set schema service.
   */
  @Lazy
  @Autowired
  private DatasetSchemaService dataSetSchemaService;


  /**
   * The record repository.
   */
  @Autowired
  private RecordRepository recordRepository;

  /**
   * The design dataset repository.
   */
  @Autowired
  private DesignDatasetRepository designDatasetRepository;

  /** The validation controller. */
  @Autowired
  private ValidationControllerZuul validationController;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(FileCommonUtils.class);

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
   * Gets the id table schema.
   *
   * @param tableName the table name
   * @param dataSetSchema the data set schema
   *
   * @return the id table schema
   */
  public String getIdTableSchema(String tableName, DataSetSchemaVO dataSetSchema) {
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
   * Find table schema.
   *
   * @param idTableSchema the id table mongo
   * @param dataSetSchema the data set schema
   *
   * @return the table schema
   */
  public TableSchemaVO findTableSchema(String idTableSchema, DataSetSchemaVO dataSetSchema) {
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
   * Find field schemas.
   *
   * @param idTablaSchema the id tabla schema
   * @param dataSetSchema the data set schema
   * @return the list
   */
  public List<FieldSchemaVO> findFieldSchemas(String idTablaSchema, DataSetSchemaVO dataSetSchema) {
    // Find the FieldSchemas
    TableSchemaVO recordSchemas = findTableSchema(idTablaSchema, dataSetSchema);
    RecordSchemaVO recordSchema = null != recordSchemas ? recordSchemas.getRecordSchema() : null;
    if (null != recordSchema) {
      return recordSchema.getFieldSchema();
    }
    return null;
  }

  /**
   * Gets the data set schema.
   *
   * @param dataflowId the dataflow id
   * @param datasetId the dataset id
   * @return the data set schema
   * @throws EEAException the EEA exception
   */
  public DataSetSchemaVO getDataSetSchema(Long dataflowId, Long datasetId) throws EEAException {
    DataSetSchemaVO dataSetSchema = null;
    // get dataset schema from mongo DB
    if (null != dataflowId) {
      dataSetSchema = dataSetSchemaService.getDataSchemaByDatasetId(false, datasetId);
    }
    return dataSetSchema;
  }

  /**
   * Gets the table name.
   *
   * @param idTableSchema the id table schema
   * @param dataSetSchema the data set schema
   *
   * @return the table name
   */
  public String getTableName(String idTableSchema, DataSetSchemaVO dataSetSchema) {
    TableSchemaVO table = findTableSchema(idTableSchema, dataSetSchema);
    return table != null ? table.getNameTableSchema() : idTableSchema;
  }

  /**
   * Gets the field schemas.
   *
   * @param idTableSchema the id table schema
   * @param dataSetSchema the data set schema
   *
   * @return the field schemas
   */
  public List<FieldSchemaVO> getFieldSchemas(String idTableSchema, DataSetSchemaVO dataSetSchema) {
    TableSchemaVO table = findTableSchema(idTableSchema, dataSetSchema);
    return table != null && table.getRecordSchema() != null
        ? table.getRecordSchema().getFieldSchema()
        : null;
  }

  /**
   * Gets the record values.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   *
   * @return the record values
   */
  public List<RecordValue> getRecordValues(@DatasetId Long datasetId, String idTableSchema) {
    return sanitizeRecords(recordRepository.findByTableValueIdTableSchema(idTableSchema));
  }


  /**
   * Sanitize records.
   *
   * @param records the records
   *
   * @return the list
   */
  private List<RecordValue> sanitizeRecords(List<RecordValue> records) {
    List<RecordValue> sanitizedRecords = new ArrayList<>();
    Set<String> processedRecords = new HashSet<>();
    for (RecordValue recordValue : records) {
      if (!processedRecords.contains(recordValue.getId())) {
        processedRecords.add(recordValue.getId());
        recordValue.getFields().stream().forEach(field -> field.setFieldValidations(null));
        sanitizedRecords.add(recordValue);
      }

    }
    return sanitizedRecords;

  }

  /**
   * Checks if is design dataset.
   *
   * @param datasetId the dataset id
   * @return true, if is design dataset
   */
  public boolean isDesignDataset(Long datasetId) {
    return designDatasetRepository.existsById(datasetId);
  }

  /**
   * Gets the errors.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param datasetSchema the dataset schema
   * @return the errors
   */
  public FailedValidationsDatasetVO getErrors(Long datasetId, String idTableSchema,
      DataSetSchemaVO datasetSchema) {
    return validationController.getFailedValidationsByIdDataset(datasetId, 0, null, null, true,
        null, Arrays.asList(EntityTypeEnum.FIELD, EntityTypeEnum.RECORD),
        getTableName(idTableSchema, datasetSchema), null);
  }

}
