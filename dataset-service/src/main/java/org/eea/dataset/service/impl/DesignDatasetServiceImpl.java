package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DesignDatasetMapper;
import org.eea.dataset.mapper.FieldSchemaNoRulesMapper;
import org.eea.dataset.mapper.TableSchemaMapper;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


/**
 * The Class DesignDatasetServiceImpl.
 */
@Service("designDatasetService")
public class DesignDatasetServiceImpl implements DesignDatasetService {


  /** The design dataset repository. */
  @Autowired
  private DesignDatasetRepository designDatasetRepository;


  /** The design dataset mapper. */
  @Autowired
  private DesignDatasetMapper designDatasetMapper;

  /** The file common. */
  @Autowired
  private FileCommonUtils fileCommon;

  /** The dataschema service. */
  @Autowired
  private DatasetSchemaService dataschemaService;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The dataset service. */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /** The table schema mapper. */
  @Autowired
  private TableSchemaMapper tableSchemaMapper;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The field schema no rules mapper. */
  @Autowired
  FieldSchemaNoRulesMapper fieldSchemaNoRulesMapper;


  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DesignDatasetServiceImpl.class);



  /**
   * Gets the design data set id by dataflow id.
   *
   * @param idFlow the id flow
   * @return the design data set id by dataflow id
   */
  @Override
  public List<DesignDatasetVO> getDesignDataSetIdByDataflowId(Long idFlow) {

    List<DesignDataset> datasets = designDatasetRepository.findByDataflowId(idFlow);
    return designDatasetMapper.entityListToClass(datasets);
  }


  /**
   * Gets the file name design.
   *
   * @param mimeType the mime type
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   * @return the file name design
   * @throws EEAException the EEA exception
   */
  @Override
  public String getFileNameDesign(String mimeType, String idTableSchema, Long datasetId)
      throws EEAException {

    final DesignDataset designDataset =
        designDatasetRepository.findById(datasetId).orElse(new DesignDataset());

    DataSetSchemaVO dataSetSchema =
        fileCommon.getDataSetSchema(designDataset.getDataflowId(), datasetId);
    return null == fileCommon.getFieldSchemas(idTableSchema, dataSetSchema)
        ? designDataset.getDataSetName() + "." + mimeType
        : fileCommon.getTableName(idTableSchema, dataSetSchema) + "." + mimeType;
  }



  /**
   * Copy design datasets.
   *
   * @param designs the designs
   * @param idDataflowDestination the id dataflow destination
   * @throws EEAException the EEA exception
   * 
   *         We've got the list of design datasets from the dataflow to copy and the dataflow
   *         destination. The idea is create a new design dataset copied from the original and
   *         change the schema's objectId (dataset, table, record and field level)
   */
  @Async
  @Override
  public void copyDesignDatasets(List<DesignDatasetVO> designs, Long idDataflowDestination)
      throws EEAException {

    // Map to store the objectsId of the schemas (dataset, tables, records and fields, to keep the
    // track).
    // The key is the older to copy, the value is the new one created
    Map<String, String> mapOldNewObjectId = new HashMap<>();
    for (DesignDatasetVO design : designs) {

      DataSetSchemaVO schemaVO = dataschemaService.getDataSchemaById(design.getDatasetSchema());
      String newIdDatasetSchema;
      try {
        // Create the schema
        newIdDatasetSchema =
            dataschemaService.createEmptyDataSetSchema(idDataflowDestination).toString();
        mapOldNewObjectId.put(schemaVO.getIdDataSetSchema(), newIdDatasetSchema);
        // Create the schema into the metabase
        Future<Long> datasetId = datasetMetabaseService.createEmptyDataset(DatasetTypeEnum.DESIGN,
            nameToClone(schemaVO.getNameDatasetSchema(), designs), newIdDatasetSchema,
            idDataflowDestination, null, null, 0);
        // Fill the new schema dataset with the data of the original schema dataset
        LOG.info("Design dataset created in the copy process with id {}", datasetId.get());
        Thread.sleep(2000);
        fillDesignDatasetCopied(schemaVO, newIdDatasetSchema, mapOldNewObjectId, datasetId.get());

      } catch (EEAException | InterruptedException | ExecutionException e) {
        LOG_ERROR.error("Error during the copy", e);
      }
    }
    // Release the notification
    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.COPY_DATASET_SCHEMA_COMPLETED_EVENT,
        null, NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
            .dataflowId(idDataflowDestination).build());

  }


  /**
   * Fill design dataset copied.
   *
   * @param schemaOrigin the schema origin
   * @param newIdDatasetSchema the new id dataset schema
   * @param mapOldNewObjectId the map old new object id
   * @param datasetId the dataset id
   * @return the map
   * @throws EEAException the EEA exception
   * @throws InterruptedException the interrupted exception
   */
  private Map<String, String> fillDesignDatasetCopied(DataSetSchemaVO schemaOrigin,
      String newIdDatasetSchema, Map<String, String> mapOldNewObjectId, Long datasetId)
      throws EEAException, InterruptedException {

    DataSetSchema schema =
        schemasRepository.findByIdDataSetSchema(new ObjectId(newIdDatasetSchema));

    schema.setDescription(schemaOrigin.getDescription());
    // table level
    for (TableSchemaVO tableVO : schemaOrigin.getTableSchemas()) {
      TableSchema table = new TableSchema();
      ObjectId newTableId = new ObjectId();
      mapOldNewObjectId.put(tableVO.getIdTableSchema(), newTableId.toString());
      table = tableSchemaMapper.classToEntity(tableVO);
      table.setIdTableSchema(newTableId);
      // record level
      RecordSchema record = new RecordSchema();
      ObjectId newRecordId = new ObjectId();
      record.setIdRecordSchema(newRecordId);
      if (tableVO.getRecordSchema() != null && tableVO.getRecordSchema().getFieldSchema() != null) {
        mapOldNewObjectId.put(tableVO.getRecordSchema().getIdRecordSchema(),
            newRecordId.toString());
        record.setFieldSchema(new ArrayList<>());
        record.setIdTableSchema(newTableId);
        // field level
        for (FieldSchemaVO fieldVO : tableVO.getRecordSchema().getFieldSchema()) {
          FieldSchema field = new FieldSchema();
          ObjectId newFieldId = new ObjectId();
          mapOldNewObjectId.put(fieldVO.getId(), newFieldId.toString());
          field = fieldSchemaNoRulesMapper.classToEntity(fieldVO);
          field.setIdFieldSchema(newFieldId);

          record.getFieldSchema().add(field);
        }
        table.setRecordSchema(record);

      }
      schema.getTableSchemas().add(table);

      // propagate new table into the datasets schema
      datasetService.saveTablePropagation(datasetId, tableSchemaMapper.entityToClass(table));
    }
    // save
    schemasRepository.updateSchemaDocument(schema);

    return mapOldNewObjectId;
  }


  /**
   * Name to clone.
   *
   * @param nameDesign the name design
   * @param designs the designs
   * @return the string
   */
  private String nameToClone(String nameDesign, List<DesignDatasetVO> designs) {

    String result = "CLONE_" + nameDesign;

    return result;
  }



}
