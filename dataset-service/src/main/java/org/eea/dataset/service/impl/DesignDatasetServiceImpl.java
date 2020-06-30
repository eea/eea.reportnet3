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
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.CopySchemaVO;
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

  /** The dataschema controller. */
  @Autowired
  private DatasetSchemaController dataschemaController;

  /** The rules controller zuul. */
  @Autowired
  private RulesControllerZuul rulesControllerZuul;


  /** The integration controller zuul. */
  @Autowired
  private IntegrationControllerZuul integrationControllerZuul;



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
    // The key is the origin to copy, the value is the new one created, the target
    Map<String, String> dictionaryOriginTargetObjectId = new HashMap<>();

    // map to temporally store the datasetId and the field schema if it's a Link type. Later we'll
    // use this map to modify
    // the field schemas to correct the fk relations
    Map<Long, List<FieldSchemaVO>> mapDatasetIdFKRelations = new HashMap<>();

    // List to store the origin datasetSchemaIds
    List<String> originDatasetSchemaIds = new ArrayList<>();

    // Map to store the old datasetsId and the new ones equivalent
    Map<Long, Long> dictionaryOriginTargetDatasetsId = new HashMap<>();

    for (DesignDatasetVO design : designs) {

      DataSetSchemaVO schemaVO = dataschemaService.getDataSchemaById(design.getDatasetSchema());
      originDatasetSchemaIds.add(design.getDatasetSchema());
      String newIdDatasetSchema;
      try {
        // Create the schema
        newIdDatasetSchema =
            dataschemaService.createEmptyDataSetSchema(idDataflowDestination).toString();
        dictionaryOriginTargetObjectId.put(schemaVO.getIdDataSetSchema(), newIdDatasetSchema);
        // Create the schema into the metabase
        Future<Long> datasetId = datasetMetabaseService.createEmptyDataset(DatasetTypeEnum.DESIGN,
            nameToClone(schemaVO.getNameDatasetSchema(), idDataflowDestination), newIdDatasetSchema,
            idDataflowDestination, null, null, 0);
        // Fill the new schema dataset with the data of the original schema dataset
        LOG.info("Design dataset created in the copy process with id {}", datasetId.get());
        dictionaryOriginTargetDatasetsId.put(design.getId(), datasetId.get());
        Thread.sleep(3000);
        fillAndUpdateDesignDatasetCopied(schemaVO, newIdDatasetSchema,
            dictionaryOriginTargetObjectId, datasetId.get(), mapDatasetIdFKRelations);

      } catch (EEAException | InterruptedException | ExecutionException e) {
        LOG_ERROR.error("Error during the copy", e);
      }
    }

    // Modify the FK
    processToModifyTheFK(dictionaryOriginTargetObjectId, mapDatasetIdFKRelations);


    // Copy the rules
    CopySchemaVO copy = new CopySchemaVO();
    copy.setDictionaryOriginTargetObjectId(dictionaryOriginTargetObjectId);
    copy.setOriginDatasetSchemaIds(originDatasetSchemaIds);
    copy.setDataflowIdDestination(idDataflowDestination);
    copy.setDictionaryOriginTargetDatasetsId(dictionaryOriginTargetDatasetsId);

    dictionaryOriginTargetObjectId = rulesControllerZuul.copyRulesSchema(copy);

    // Copy the unique catalogue
    dataschemaService.copyUniqueConstraintsCatalogue(originDatasetSchemaIds,
        dictionaryOriginTargetObjectId);

    // Copy the integrations
    integrationControllerZuul.copyIntegrations(copy);

    // Copy the data inside the design datasets, but only the tables that are prefilled
    datasetService.copyData(dictionaryOriginTargetDatasetsId, dictionaryOriginTargetObjectId);

    // Release the notification
    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.COPY_DATASET_SCHEMA_COMPLETED_EVENT,
        null, NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
            .dataflowId(idDataflowDestination).build());

  }



  /**
   * Fill and update design dataset copied.
   *
   * @param schemaOrigin the schema origin
   * @param newIdDatasetSchema the new id dataset schema
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param datasetId the dataset id
   * @param mapDatasetIdFKRelations the map dataset id FK relations
   * @return the map
   * @throws EEAException the EEA exception
   * @throws InterruptedException the interrupted exception
   */
  private Map<String, String> fillAndUpdateDesignDatasetCopied(DataSetSchemaVO schemaOrigin,
      String newIdDatasetSchema, Map<String, String> dictionaryOriginTargetObjectId, Long datasetId,
      Map<Long, List<FieldSchemaVO>> mapDatasetIdFKRelations)
      throws EEAException, InterruptedException {

    DataSetSchema schema =
        schemasRepository.findByIdDataSetSchema(new ObjectId(newIdDatasetSchema));
    schema.setDescription(schemaOrigin.getDescription());
    // table level
    for (TableSchemaVO tableVO : schemaOrigin.getTableSchemas()) {
      TableSchema table = new TableSchema();
      ObjectId newTableId = new ObjectId();
      dictionaryOriginTargetObjectId.put(tableVO.getIdTableSchema(), newTableId.toString());
      table = tableSchemaMapper.classToEntity(tableVO);
      table.setIdTableSchema(newTableId);
      // record level
      RecordSchema record = new RecordSchema();
      ObjectId newRecordId = new ObjectId();
      record.setIdRecordSchema(newRecordId);
      if (tableVO.getRecordSchema() != null && tableVO.getRecordSchema().getFieldSchema() != null) {
        dictionaryOriginTargetObjectId.put(tableVO.getRecordSchema().getIdRecordSchema(),
            newRecordId.toString());
        record.setFieldSchema(new ArrayList<>());
        record.setIdTableSchema(newTableId);
        // field level
        for (FieldSchemaVO fieldVO : tableVO.getRecordSchema().getFieldSchema()) {
          FieldSchema field = new FieldSchema();
          ObjectId newFieldId = new ObjectId();
          dictionaryOriginTargetObjectId.put(fieldVO.getId(), newFieldId.toString());
          field = fieldSchemaNoRulesMapper.classToEntity(fieldVO);
          field.setIdFieldSchema(newFieldId);
          field.setIdRecord(newRecordId);
          record.getFieldSchema().add(field);

          // if the type is Link we store to later modify the schema id's with the proper fk
          // relations after all the process it's done
          if (DataType.LINK.equals(field.getType())) {
            List<FieldSchemaVO> listFK = new ArrayList<>();
            if (mapDatasetIdFKRelations.containsKey(datasetId)) {
              listFK = mapDatasetIdFKRelations.get(datasetId);
            }
            listFK.add(fieldVO);
            mapDatasetIdFKRelations.put(datasetId, listFK);
          }
        }
        table.setRecordSchema(record);
      }
      schema.getTableSchemas().add(table);

      // propagate new table into the datasets schema
      datasetService.saveTablePropagation(datasetId, tableSchemaMapper.entityToClass(table));
    }
    // save
    schemasRepository.updateSchemaDocument(schema);

    return dictionaryOriginTargetObjectId;
  }



  /**
   * Process to modify the FK.
   *
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param mapDatasetIdFKRelations the map dataset id FK relations
   */
  private void processToModifyTheFK(Map<String, String> dictionaryOriginTargetObjectId,
      Map<Long, List<FieldSchemaVO>> mapDatasetIdFKRelations) {

    mapDatasetIdFKRelations.forEach((datasetId, listFields) -> {
      for (FieldSchemaVO field : listFields) {
        if (dictionaryOriginTargetObjectId.containsKey(field.getId())) {
          field.setId(dictionaryOriginTargetObjectId.get(field.getId()));
        }
        if (dictionaryOriginTargetObjectId.containsKey(field.getIdRecord())) {
          field.setIdRecord(dictionaryOriginTargetObjectId.get(field.getIdRecord()));
        }
        if (field.getReferencedField() != null) {
          if (dictionaryOriginTargetObjectId
              .containsKey(field.getReferencedField().getIdDatasetSchema())) {
            field.getReferencedField().setIdDatasetSchema(dictionaryOriginTargetObjectId
                .get(field.getReferencedField().getIdDatasetSchema()));
          }
          if (dictionaryOriginTargetObjectId.containsKey(field.getReferencedField().getIdPk())) {
            field.getReferencedField()
                .setIdPk(dictionaryOriginTargetObjectId.get(field.getReferencedField().getIdPk()));
          }
        }
        // with the fieldVO updated with the objectIds of the cloned dataset, we modify the field to
        // update all the things
        // related to the PK/FK
        dataschemaController.updateFieldSchema(datasetId, field);
      }
    });
  }



  /**
   * Name to clone.
   *
   * @param nameDesign the name design
   * @param dataflowIdDestination the dataflow id destination
   * @return the string
   */
  private String nameToClone(String nameDesign, Long dataflowIdDestination) {

    List<DesignDatasetVO> designs = getDesignDataSetIdByDataflowId(dataflowIdDestination);
    String result = "CLONE_" + nameDesign;
    int index = 1;
    for (int i = 0; i < designs.size(); i++) {
      if (designs.get(i).getDataSetName().equals(result)) {
        result = result + " (" + index + ")";
        i = 0;
        index++;
        break;
      }
    }
    return result;
  }



}
