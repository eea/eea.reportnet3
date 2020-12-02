package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DesignDatasetMapper;
import org.eea.dataset.mapper.FieldSchemaNoRulesMapper;
import org.eea.dataset.mapper.TableSchemaMapper;
import org.eea.dataset.mapper.WebFormMapper;
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
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.ContributorController.ContributorControllerZuul;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.CopySchemaVO;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

  /** The contributor controller zuul. */
  @Autowired
  private ContributorControllerZuul contributorControllerZuul;

  /** The webform mapper. */
  @Autowired
  private WebFormMapper webformMapper;

  /** The record store controller zuul. */
  @Autowired
  private RecordStoreControllerZuul recordStoreControllerZuul;


  /** The time to wait before continue copy. */
  @Value("${wait.continue.copy.ms}")
  private Long timeToWaitBeforeContinueCopy;



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
   * Copy design datasets.
   *
   * @param idDataflowOrigin the id dataflow origin
   * @param idDataflowDestination the id dataflow destination
   * @throws EEAException the EEA exception
   * 
   *         We've got the list of design datasets from the dataflow to copy and the dataflow
   *         destination. The idea is create a new design dataset copied from the original and
   *         change the schema's objectId (dataset, table, record and field level)
   */
  @Async
  @Override
  public void copyDesignDatasets(Long idDataflowOrigin, Long idDataflowDestination)
      throws EEAException {

    // Map to store the objectsId of the schemas (dataset, tables, records and fields, to keep the
    // track).
    // The key is the origin to copy, the value is the new one created, the target
    Map<String, String> dictionaryOriginTargetObjectId = new HashMap<>();

    // map to temporally store the datasetId and the field schema if it's a Link type. Later we'll
    // use this map to modify
    // the field schemas to correct the fk relations, using the updateFieldSchema (only the ones
    // with type Link)
    Map<Long, List<FieldSchemaVO>> mapDatasetIdFKRelations = new HashMap<>();

    // List to store the origin datasetSchemaIds
    List<String> originDatasetSchemaIds = new ArrayList<>();

    // Map to store the origin datasetsId and the new ones crated equivalent
    Map<Long, Long> dictionaryOriginTargetDatasetsId = new HashMap<>();

    // Auxiliary map with the new dataset schemas created and the origin schema
    Map<Long, DataSetSchemaVO> mapDatasetsDestinyAndSchemasOrigin = new HashMap<>();

    LOG.info(
        "The process to copy schemas from one dataflow to another begins. Dataflow Origin: {}. Dataflow Destination: {}",
        idDataflowOrigin, idDataflowDestination);

    // Obtain the list of design datasets to copy
    List<DesignDatasetVO> designs = getDesignDataSetIdByDataflowId(idDataflowOrigin);
    if (designs == null || designs.isEmpty()) {
      // Error. There aren't designs to copy in the dataflow
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.COPY_DATASET_SCHEMA_NOT_FOUND_EVENT,
          null, NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
              .dataflowId(idDataflowDestination).error("No design datasets found to copy").build());
      datasetService.releaseLock(LockSignature.COPY_DATASET_SCHEMA.getValue(), idDataflowOrigin,
          idDataflowDestination);
      throw new EEAException(String.format(EEAErrorMessage.NO_DESIGNS_TO_COPY, idDataflowOrigin));
    } else {
      try {
        for (DesignDatasetVO design : designs) {

          DataSetSchemaVO schemaVO = dataschemaService.getDataSchemaById(design.getDatasetSchema());
          originDatasetSchemaIds.add(design.getDatasetSchema());

          // Create the schema
          String newIdDatasetSchema =
              dataschemaService.createEmptyDataSetSchema(idDataflowDestination).toString();
          // fill the dictionary of origin and news object id created. This will be done during all
          // the process
          dictionaryOriginTargetObjectId.put(schemaVO.getIdDataSetSchema(), newIdDatasetSchema);
          // Create the schema into the metabase
          Future<Long> datasetId = datasetMetabaseService.createEmptyDataset(DatasetTypeEnum.DESIGN,
              nameToClone(schemaVO.getNameDatasetSchema(), idDataflowDestination),
              newIdDatasetSchema, idDataflowDestination, null, null, 0);
          // Fill the new schema dataset with the data of the original schema dataset
          LOG.info("Design dataset created in the copy process with id {}", datasetId.get());
          dictionaryOriginTargetDatasetsId.put(design.getId(), datasetId.get());
          mapDatasetsDestinyAndSchemasOrigin.put(datasetId.get(), schemaVO);
          // Time to wait before continuing the process. If the process goes too fast, it won't find
          // the
          // dataset schema created and the process will fail. By default 3000ms
          Thread.sleep(timeToWaitBeforeContinueCopy);
        }
        // After creating the datasets schemas on the DB, fill them and create the permissions
        for (Map.Entry<Long, DataSetSchemaVO> itemNewDatasetAndSchema : mapDatasetsDestinyAndSchemasOrigin
            .entrySet()) {
          contributorControllerZuul.createAssociatedPermissions(idDataflowDestination,
              itemNewDatasetAndSchema.getKey());
          fillAndUpdateDesignDatasetCopied(itemNewDatasetAndSchema.getValue(),
              dictionaryOriginTargetObjectId
                  .get(itemNewDatasetAndSchema.getValue().getIdDataSetSchema()),
              dictionaryOriginTargetObjectId, itemNewDatasetAndSchema.getKey(),
              mapDatasetIdFKRelations);

        }



        // Modify the FK, if the schemas copied have fields of type Link, to update the relations to
        // the correct ones
        processToModifyTheFK(dictionaryOriginTargetObjectId, mapDatasetIdFKRelations);

        // We use an auxiliary bean to store all the data we need to continue the process. The
        // dictionarys and the dataflowIds involved
        CopySchemaVO copy = new CopySchemaVO();
        copy.setDictionaryOriginTargetObjectId(dictionaryOriginTargetObjectId);
        copy.setOriginDatasetSchemaIds(originDatasetSchemaIds);
        copy.setDataflowIdDestination(idDataflowDestination);
        copy.setDictionaryOriginTargetDatasetsId(dictionaryOriginTargetDatasetsId);
        // Copy the rules
        dictionaryOriginTargetObjectId = rulesControllerZuul.copyRulesSchema(copy);

        // Copy the unique catalogue (in case there are Links in the schemas involved)
        dataschemaService.copyUniqueConstraintsCatalogue(originDatasetSchemaIds,
            dictionaryOriginTargetObjectId);

        // Copy the integrations (in case the design datasets have integrations (to use with FME)
        // created)
        integrationControllerZuul.copyIntegrations(copy);

        // Copy the data inside the design datasets, but only the tables that are prefilled
        datasetService.copyData(dictionaryOriginTargetDatasetsId, dictionaryOriginTargetObjectId);

        // Create the views necessary to the validation in the new datasets created
        dictionaryOriginTargetDatasetsId
            .forEach((Long datasetOrigin, Long datasetDestination) -> recordStoreControllerZuul
                .createUpdateQueryView(datasetDestination));


        // Release the notification
        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.COPY_DATASET_SCHEMA_COMPLETED_EVENT,
            null,
            NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
                .dataflowId(idDataflowDestination).build());

        // Release the lock
        datasetService.releaseLock(LockSignature.COPY_DATASET_SCHEMA.getValue(), idDataflowOrigin,
            idDataflowDestination);

      } catch (Exception e) {
        LOG_ERROR.error("Error during the copy. Message: {}", e.getMessage(), e);
        // Release the error notification
        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.COPY_DATASET_SCHEMA_FAILED_EVENT,
            null,
            NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
                .dataflowId(idDataflowDestination).error("Error copying the schemas").build());
        datasetService.releaseLock(LockSignature.COPY_DATASET_SCHEMA.getValue(), idDataflowOrigin,
            idDataflowDestination);
        throw new EEAException(String.format(EEAErrorMessage.ERROR_COPYING_SCHEMAS,
            idDataflowOrigin, idDataflowDestination), e);
      }

    }
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
   */
  private Map<String, String> fillAndUpdateDesignDatasetCopied(DataSetSchemaVO schemaOrigin,
      String newIdDatasetSchema, Map<String, String> dictionaryOriginTargetObjectId, Long datasetId,
      Map<Long, List<FieldSchemaVO>> mapDatasetIdFKRelations) throws EEAException {

    // We've got the new schema created during the copy process. Now using the dictionary we'll
    // replace the objectIds of the schema, because at this moment the new schema has the origin
    // values, so we'll change it to new ObjectIds and finally we'll update it
    DataSetSchema schema =
        schemasRepository.findByIdDataSetSchema(new ObjectId(newIdDatasetSchema));
    schema.setDescription(schemaOrigin.getDescription());
    schema.setWebform(webformMapper.classToEntity(schemaOrigin.getWebform()));
    // table level
    for (TableSchemaVO tableVO : schemaOrigin.getTableSchemas()) {
      ObjectId newTableId = new ObjectId();
      dictionaryOriginTargetObjectId.put(tableVO.getIdTableSchema(), newTableId.toString());
      TableSchema table = tableSchemaMapper.classToEntity(tableVO);
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
          ObjectId newFieldId = new ObjectId();
          dictionaryOriginTargetObjectId.put(fieldVO.getId(), newFieldId.toString());
          FieldSchema field = fieldSchemaNoRulesMapper.classToEntity(fieldVO);
          field.setIdFieldSchema(newFieldId);
          field.setIdRecord(newRecordId);
          record.getFieldSchema().add(field);

          // if the type is Link we store to later modify the schema id's with the proper fk
          // relations after all the process it's done
          mapLinkResult(datasetId, mapDatasetIdFKRelations, fieldVO, field);
        }
        table.setRecordSchema(record);
      }
      schema.getTableSchemas().add(table);

      // propagate new table into the datasets schema
      datasetService.saveTablePropagation(datasetId, tableSchemaMapper.entityToClass(table));
    }
    // save the schema with the new values
    schemasRepository.updateSchemaDocument(schema);

    return dictionaryOriginTargetObjectId;
  }


  /**
   * Map link result.
   *
   * @param datasetId the dataset id
   * @param mapDatasetIdFKRelations the map dataset id FK relations
   * @param fieldVO the field VO
   * @param field the field
   */
  private void mapLinkResult(Long datasetId, Map<Long, List<FieldSchemaVO>> mapDatasetIdFKRelations,
      FieldSchemaVO fieldVO, FieldSchema field) {
    if (DataType.LINK.equals(field.getType())) {
      List<FieldSchemaVO> listFK = new ArrayList<>();
      if (mapDatasetIdFKRelations.containsKey(datasetId)) {
        listFK = mapDatasetIdFKRelations.get(datasetId);
      }
      listFK.add(fieldVO);
      mapDatasetIdFKRelations.put(datasetId, listFK);
    }
  }



  /**
   * Process to modify the FK.
   *
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param mapDatasetIdFKRelations the map dataset id FK relations
   */
  private void processToModifyTheFK(Map<String, String> dictionaryOriginTargetObjectId,
      Map<Long, List<FieldSchemaVO>> mapDatasetIdFKRelations) {
    // Withe the help of the dictionary and the map of involved datasetIds and it's FieldSchemaVO
    // objects, we replace the objects with the correct ones and finally we make an
    // updateFieldSchema
    mapDatasetIdFKRelations.forEach((datasetId, listFields) -> {
      for (FieldSchemaVO field : listFields) {
        if (dictionaryOriginTargetObjectId.containsKey(field.getId())) {
          field.setId(dictionaryOriginTargetObjectId.get(field.getId()));
        }
        if (dictionaryOriginTargetObjectId.containsKey(field.getIdRecord())) {
          field.setIdRecord(dictionaryOriginTargetObjectId.get(field.getIdRecord()));
        }
        if (field.getReferencedField() != null && DataType.LINK.equals(field.getType())) {
          referenceFieldDictionary(dictionaryOriginTargetObjectId, field);
        }
        // with the fieldVO updated with the objectIds of the cloned dataset, we modify the field to
        // update all the things
        // related to the PK/FK
        dataschemaController.updateFieldSchema(datasetId, field);
      }
    });
  }


  /**
   * Reference field dictionary.
   *
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param field the field
   */
  private void referenceFieldDictionary(Map<String, String> dictionaryOriginTargetObjectId,
      FieldSchemaVO field) {
    if (dictionaryOriginTargetObjectId
        .containsKey(field.getReferencedField().getIdDatasetSchema())) {
      field.getReferencedField().setIdDatasetSchema(
          dictionaryOriginTargetObjectId.get(field.getReferencedField().getIdDatasetSchema()));
    }
    if (dictionaryOriginTargetObjectId.containsKey(field.getReferencedField().getIdPk())) {
      field.getReferencedField()
          .setIdPk(dictionaryOriginTargetObjectId.get(field.getReferencedField().getIdPk()));
    }
    if (dictionaryOriginTargetObjectId.containsKey(field.getReferencedField().getLabelId())) {
      field.getReferencedField()
          .setLabelId(dictionaryOriginTargetObjectId.get(field.getReferencedField().getLabelId()));
    }
    if (dictionaryOriginTargetObjectId
        .containsKey(field.getReferencedField().getLinkedConditionalFieldId())) {
      field.getReferencedField().setLinkedConditionalFieldId(dictionaryOriginTargetObjectId
          .get(field.getReferencedField().getLinkedConditionalFieldId()));
    }
    if (dictionaryOriginTargetObjectId
        .containsKey(field.getReferencedField().getMasterConditionalFieldId())) {
      field.getReferencedField().setMasterConditionalFieldId(dictionaryOriginTargetObjectId
          .get(field.getReferencedField().getMasterConditionalFieldId()));
    }
  }



  /**
   * Name to clone.
   *
   * @param nameDesign the name design
   * @param dataflowIdDestination the dataflow id destination
   * @return the string
   */
  private String nameToClone(String nameDesign, Long dataflowIdDestination) {
    // The name of the dataset copied will be CLONE_whatever. If it exists, it will be
    // CLONE_whatever (1) and so
    List<DesignDatasetVO> designs = getDesignDataSetIdByDataflowId(dataflowIdDestination);
    String result = "CLONE_" + nameDesign;
    int index = 1;
    for (int i = 0; i < designs.size(); i++) {
      if (designs.get(i).getDataSetName().equals(result)) {
        if (result.contains("(" + (index - 1) + ")")) {
          result = result.replace("(" + (index - 1) + ")", "(" + index + ")");
        } else {
          result = result + " (" + index + ")";
        }
        i = 0;
        index++;
      }
    }
    return result;
  }



}
