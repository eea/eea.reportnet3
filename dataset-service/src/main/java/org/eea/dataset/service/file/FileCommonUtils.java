package org.eea.dataset.service.file;


import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.multitenancy.DatasetId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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

  /** The dataset service. */
  @Lazy
  @Autowired
  private DatasetService datasetService;

  /**
   * The record repository.
   */
  @Autowired
  private RecordRepository recordRepository;

  /** The table repository. */
  @Autowired
  private TableRepository tableRepository;

  /**
   * The design dataset repository.
   */
  @Autowired
  private DesignDatasetRepository designDatasetRepository;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The data set metabase repository. */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;



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
  public String findIdRecord(String idTableMongo, DataSetSchema dataSetSchema) {
    // Find the idrecordSchema from MongoDB
    TableSchema tableS = findTableSchema(idTableMongo, dataSetSchema);
    String result = null;
    if (null != tableS) {
      result =
          null != tableS.getRecordSchema() ? tableS.getRecordSchema().getIdRecordSchema().toString()
              : null;
    }
    return result;
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
    TableSchemaVO tableS = findTableSchemaVO(idTableMongo, dataSetSchema);
    String result = null;
    if (null != tableS) {
      result =
          null != tableS.getRecordSchema() ? tableS.getRecordSchema().getIdRecordSchema().toString()
              : null;
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
  public String getIdTableSchema(String tableName, DataSetSchema dataSetSchema) {
    // Find the Id of tableSchema in MongoDB
    String idTable = null;
    List<TableSchema> tablesSchema = null;
    if (null != dataSetSchema) {
      tablesSchema = dataSetSchema.getTableSchemas();
    }
    if (null != tablesSchema) {
      for (TableSchema tableSchema : tablesSchema) {
        if (tableSchema.getNameTableSchema().equalsIgnoreCase(tableName)) {
          idTable = tableSchema.getIdTableSchema().toString();
        }
      }
    }
    return idTable;
  }

  /**
   * Gets the id table schema.
   *
   * @param tableName the table name
   * @param dataSetSchema the data set schema
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
  public TableSchema findTableSchema(String idTableSchema, DataSetSchema dataSetSchema) {
    // Find the tableSchema of MongoDB
    List<TableSchema> tablesSchema = null;
    if (null != dataSetSchema) {
      tablesSchema = dataSetSchema.getTableSchemas();
    }
    if (null != tablesSchema) {
      for (TableSchema tableSchema : tablesSchema) {
        if (tableSchema.getIdTableSchema().toString().equalsIgnoreCase(idTableSchema)) {
          return tableSchema;
        }
      }
    }
    return null;
  }

  /**
   * Find table schema VO.
   *
   * @param idTableSchema the id table schema
   * @param dataSetSchema the data set schema
   * @return the table schema VO
   */
  public TableSchemaVO findTableSchemaVO(String idTableSchema, DataSetSchemaVO dataSetSchema) {
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
  public FieldSchema findIdFieldSchema(String nameSchema, String idTablaSchema,
      DataSetSchema dataSetSchema) {
    // Find the idFieldSchema
    TableSchema recordSchemas = findTableSchema(idTablaSchema, dataSetSchema);
    RecordSchema recordSchema = null != recordSchemas ? recordSchemas.getRecordSchema() : null;
    if (null != recordSchema && null != recordSchema.getFieldSchema()) {
      for (FieldSchema fieldSchema : recordSchema.getFieldSchema()) {
        if (null != fieldSchema.getHeaderName()
            && fieldSchema.getHeaderName().equalsIgnoreCase(nameSchema)) {
          return fieldSchema;
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
   * @return the field schema VO
   */
  public FieldSchemaVO findIdFieldSchema(String nameSchema, String idTablaSchema,
      DataSetSchemaVO dataSetSchema) {
    // Find the idFieldSchema
    TableSchemaVO recordSchemas = findTableSchemaVO(idTablaSchema, dataSetSchema);
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
  public List<FieldSchema> findFieldSchemas(String idTablaSchema, DataSetSchema dataSetSchema) {
    // Find the FieldSchemas
    TableSchema recordSchemas = findTableSchema(idTablaSchema, dataSetSchema);
    RecordSchema recordSchema = null != recordSchemas ? recordSchemas.getRecordSchema() : null;
    if (null != recordSchema) {
      return recordSchema.getFieldSchema();
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
    TableSchemaVO recordSchemas = findTableSchemaVO(idTablaSchema, dataSetSchema);
    RecordSchemaVO recordSchema = null != recordSchemas ? recordSchemas.getRecordSchema() : null;
    if (null != recordSchema) {
      return recordSchema.getFieldSchema();
    }
    return null;
  }

  /**
   * Gets the data set schema VO.
   *
   * @param dataflowId the dataflow id
   * @param datasetId the dataset id
   * @return the data set schema VO
   * @throws EEAException the EEA exception
   */
  public DataSetSchemaVO getDataSetSchemaVO(Long dataflowId, Long datasetId) throws EEAException {
    DataSetSchemaVO dataSetSchema = null;
    // get dataset schema from mongo DB
    if (null != dataflowId) {
      dataSetSchema = dataSetSchemaService.getDataSchemaByDatasetId(false, datasetId);
    }
    return dataSetSchema;
  }


  /**
   * Gets the data set schema VO.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the data set schema VO
   * @throws EEAException the EEA exception
   */
  public DataSetSchemaVO getDataSetSchemaVO(String datasetSchemaId) throws EEAException {
    return dataSetSchemaService.getDataSchemaById(datasetSchemaId);
  }

  /**
   * Gets the table name.
   *
   * @param idTableSchema the id table schema
   * @param dataSetSchema the data set schema
   *
   * @return the table name
   */
  public String getTableName(String idTableSchema, DataSetSchema dataSetSchema) {
    TableSchema table = findTableSchema(idTableSchema, dataSetSchema);
    return table != null ? table.getNameTableSchema() : idTableSchema;
  }

  /**
   * Gets the table name.
   *
   * @param idTableSchema the id table schema
   * @param dataSetSchema the data set schema
   * @return the table name
   */
  public String getTableName(String idTableSchema, DataSetSchemaVO dataSetSchema) {
    TableSchemaVO table = findTableSchemaVO(idTableSchema, dataSetSchema);
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
  public List<FieldSchema> getFieldSchemas(String idTableSchema, DataSetSchema dataSetSchema) {
    TableSchema table = findTableSchema(idTableSchema, dataSetSchema);
    return table != null && table.getRecordSchema() != null
        ? table.getRecordSchema().getFieldSchema()
        : null;
  }

  /**
   * Gets the field schemas.
   *
   * @param idTableSchema the id table schema
   * @param dataSetSchema the data set schema
   * @return the field schemas
   */
  public List<FieldSchemaVO> getFieldSchemas(String idTableSchema, DataSetSchemaVO dataSetSchema) {
    TableSchemaVO table = findTableSchemaVO(idTableSchema, dataSetSchema);
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
    return datasetService.getTotalFailedValidationsByIdDataset(datasetId, idTableSchema);
  }

  /**
   * Count records by table schema.
   *
   * @param idTableSchema the id table schema
   * @return the long
   */
  public Long countRecordsByTableSchema(String idTableSchema) {
    return recordRepository.countByTableSchema(idTableSchema);
  }

  /**
   * Gets the record values paginated.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @return the record values paginated
   */
  public List<RecordValue> getRecordValuesPaginated(@DatasetId Long datasetId, String idTableSchema,
      Pageable pageable) {
    return recordRepository.findOrderedNativeRecord(
        tableRepository.findIdByIdTableSchema(idTableSchema), datasetId, pageable);
  }

  /**
   * Schema contains fixed records.
   *
   * @param datasetId the dataset id
   * @param schema the schema
   * @param tableSchemaId the table schema id
   * @return true, if successful
   */
  public boolean schemaContainsFixedRecords(Long datasetId, DataSetSchema schema,
      String tableSchemaId) {

    boolean rtn = false;

    if (!TypeStatusEnum.DESIGN.equals(dataflowControllerZuul
        .getMetabaseById(dataSetMetabaseRepository.findDataflowIdById(datasetId)).getStatus())) {
      if (null == tableSchemaId) {
        rtn = schema.getTableSchemas().stream()
            .anyMatch(tableSchema -> Boolean.TRUE.equals(tableSchema.getFixedNumber()));
      } else {
        rtn = schema.getTableSchemas().stream()
            .anyMatch(tableSchema -> tableSchemaId.equals(tableSchema.getIdTableSchema().toString())
                && Boolean.TRUE.equals(tableSchema.getFixedNumber()));
      }
    }

    return rtn;
  }

  /**
   * Persist imported dataset.
   *
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param replace the replace
   * @param schema the schema
   * @param dataset the dataset
   * @param manageFixedRecords the manage fixed records
   * @param connectionDataVO the connection data VO
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public void persistImportedDataset(final String idTableSchema, Long datasetId, String fileName,
      boolean replace, DataSetSchema schema, DatasetValue dataset, boolean manageFixedRecords,
      ConnectionDataVO connectionDataVO) throws EEAException, IOException, SQLException {
    if (dataset == null || CollectionUtils.isEmpty(dataset.getTableValues())) {
      throw new EEAException("Error processing file " + fileName);
    }

    // Check if the table with idTableSchema has been populated already
    Long oldTableId = tableRepository.findIdByIdTableSchema(idTableSchema);
    fillTableId(idTableSchema, dataset.getTableValues(), oldTableId);
    LOG.info("RN3-Import - Filled tableId: datasetId={}, fileName={}", datasetId, fileName);

    // Save empty table
    if (null == oldTableId) {
      LOG.info("RN3-Import - Saving table: datasetId={}, fileName={}", datasetId, fileName);
      datasetService.saveTable(datasetId, dataset.getTableValues().get(0));
      LOG.info("RN3-Import - Table saved: datasetId={}, fileName={}", datasetId, fileName);
    }

    if (Boolean.TRUE.equals(manageFixedRecords)) {
      if (replace) {
        ObjectId tableSchemaIdTemp = new ObjectId(idTableSchema);
        TableSchema tableSchema = schema.getTableSchemas().stream()
            .filter(tableSchemaIt -> tableSchemaIt.getIdTableSchema().equals(tableSchemaIdTemp))
            .findFirst().orElse(null);
        if (tableSchema != null) {
          datasetService.updateRecordsWithConditions(dataset.getTableValues().get(0).getRecords(),
              datasetId, tableSchema);
        }
      }
    } else {
      datasetService.storeRecords(datasetId, dataset.getTableValues().get(0).getRecords(),
          connectionDataVO);
    }
  }

  /**
   * Fill table id.
   *
   * @param idTableSchema the id table schema
   * @param listTableValues the list table values
   * @param oldTableId the old table id
   */
  private void fillTableId(final String idTableSchema, final List<TableValue> listTableValues,
      Long oldTableId) {
    if (oldTableId != null) {
      listTableValues.stream()
          .filter(tableValue -> tableValue.getIdTableSchema().equals(idTableSchema))
          .forEach(tableValue -> tableValue.setId(oldTableId));
    }
  }

}
