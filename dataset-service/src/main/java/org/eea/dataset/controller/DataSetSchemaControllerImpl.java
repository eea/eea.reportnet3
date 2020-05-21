package org.eea.dataset.controller;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.OrderVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.uniqueContraintVO.UniqueConstraintVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.netty.util.internal.StringUtil;


/**
 * The Class DataSetSchemaControllerImpl.
 */
@RestController
@RequestMapping("/dataschema")
public class DataSetSchemaControllerImpl implements DatasetSchemaController {

  /**
   * The dataschema service.
   */
  @Autowired
  private DatasetSchemaService dataschemaService;

  /**
   * The dataset service.
   */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /**
   * The dataset metabase service.
   */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /**
   * The dataset snapshot service.
   */
  @Autowired
  private DatasetSnapshotService datasetSnapshotService;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DataSetSchemaControllerImpl.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The record store controller zull.
   */
  @Autowired
  private RecordStoreControllerZull recordStoreControllerZull;

  /**
   * The dataflow controller zuul.
   */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /**
   * The rules controller zuul.
   */
  @Autowired
  private RulesControllerZuul rulesControllerZuul;

  /**
   * The design dataset service.
   */
  @Autowired
  private DesignDatasetService designDatasetService;

  /**
   * Creates the empty dataset schema.
   *
   * @param dataflowId the dataflow id
   * @param datasetSchemaName the dataset schema name
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/createEmptyDatasetSchema")
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN') AND hasRole('DATA_CUSTODIAN')")
  public void createEmptyDatasetSchema(@RequestParam("dataflowId") final Long dataflowId,
      @RequestParam("datasetSchemaName") final String datasetSchemaName) {

    try {
      Future<Long> datasetId = datasetMetabaseService.createEmptyDataset(DatasetTypeEnum.DESIGN,
          datasetSchemaName, dataschemaService.createEmptyDataSetSchema(dataflowId).toString(),
          dataflowId, null, null, 0);
      datasetId.get();
    } catch (InterruptedException | ExecutionException | EEAException e) {
      LOG.error("Aborted DataSetSchema creation: {}", e.getMessage());
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Error creating design dataset", e);
    }
  }

  /**
   * Find data schema by id.
   *
   * @param id the id
   *
   * @return the data set schema VO
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public DataSetSchemaVO findDataSchemaById(@PathVariable("id") String id) {
    return dataschemaService.getDataSchemaById(id);
  }

  /**
   * Find data schema by datasetId.
   *
   * @param datasetId the id dataset
   *
   * @return the data set schema VO
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/datasetId/{datasetId}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_PROVIDER','DATASET_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_PROVIDER', 'DATACOLLECTION_CUSTODIAN')")
  public DataSetSchemaVO findDataSchemaByDatasetId(@PathVariable("datasetId") Long datasetId) {
    try {
      return dataschemaService.getDataSchemaByDatasetId(true, datasetId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Gets the dataset schema id.
   *
   * @param datasetId the dataset id
   *
   * @return the dataset schema id
   */
  @Override
  @GetMapping("/getDataSchema/{datasetId}")
  public String getDatasetSchemaId(@PathVariable("datasetId") Long datasetId) {
    try {
      return dataschemaService.getDatasetSchemaId(datasetId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Find data schema with no rules by dataset id.
   *
   * @param datasetId the dataset id
   *
   * @return the data set schema VO
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "{datasetId}/noRules", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_PROVIDER','DATASCHEMA_CUSTODIAN','DATASCHEMA_PROVIDER')")
  public DataSetSchemaVO findDataSchemaWithNoRulesByDatasetId(
      @PathVariable("datasetId") Long datasetId) {
    try {
      return dataschemaService.getDataSchemaByDatasetId(false, datasetId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }


  /**
   * Delete dataset schema.
   *
   * @param datasetId the dataset id
   * @param forceDelete the force delete
   */
  @Override
  @DeleteMapping(value = "/dataset/{datasetId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  public void deleteDatasetSchema(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "forceDelete", required = false) Boolean forceDelete) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    String schemaId = getDatasetSchemaId(datasetId);
    if (schemaId.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DATASET_NOTFOUND);
    }
    // Check if the dataflow has any PK being referenced by an FK. If so, denies the delete
    // If forceDelete = true, skip this check. Made specially for deleting an entire dataflow
    if ((forceDelete == null || !forceDelete)
        && !dataschemaService.isSchemaAllowedForDeletion(schemaId)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, EEAErrorMessage.PK_REFERENCED);
    }

    // Check if the dataflow its on the correct state to allow delete design datasets
    try {
      Long dataflowId = datasetService.getDataFlowIdById(datasetId);
      DataFlowVO dataflow = dataflowControllerZuul.getMetabaseById(dataflowId);

      if (TypeStatusEnum.DESIGN == dataflow.getStatus()) {
        // delete the schema snapshots too
        datasetSnapshotService.deleteAllSchemaSnapshots(datasetId);

        // delete from the CataloguePK the entries if the schema has FK
        dataschemaService.updatePkCatalogueDeletingSchema(schemaId);

        // delete the schema in Mongo
        dataschemaService.deleteDatasetSchema(schemaId);

        // delete the schema to dataset
        rulesControllerZuul.deleteRulesSchema(schemaId);
        // delete the metabase
        datasetMetabaseService.deleteDesignDataset(datasetId);
        // delete the schema in database
        recordStoreControllerZull.deleteDataset("dataset_" + datasetId);

        // delete the group in keycloak
        dataschemaService.deleteGroup(datasetId, ResourceGroupEnum.DATASCHEMA_CUSTODIAN,
            ResourceGroupEnum.DATASCHEMA_PROVIDER);
        LOG.info("The Design Dataset {} has been deleted", datasetId);
      } else {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
            EEAErrorMessage.NOT_ENOUGH_PERMISSION);
      }
    } catch (EEAException e) {
      LOG_ERROR.error("Error deleting a design dataset. Message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.EXECUTION_ERROR, e);
    }


  }

  /**
   * Creates the table schema.
   *
   * @param datasetId the dataset id
   * @param tableSchemaVO the table schema VO
   *
   * @return the table VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @PostMapping(value = "/{datasetId}/tableSchema", produces = MediaType.APPLICATION_JSON_VALUE)
  public TableSchemaVO createTableSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody TableSchemaVO tableSchemaVO) {
    try {
      TableSchemaVO response = dataschemaService.createTableSchema(
          dataschemaService.getDatasetSchemaId(datasetId), tableSchemaVO, datasetId);
      datasetService.saveTablePropagation(datasetId, tableSchemaVO);
      return response;
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID, e);
    }
  }

  /**
   * Update table schema.
   *
   * @param datasetId the dataset id
   * @param tableSchemaVO the table schema VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @PutMapping("/{datasetId}/tableSchema")
  public void updateTableSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody TableSchemaVO tableSchemaVO) {
    try {
      dataschemaService.updateTableSchema(dataschemaService.getDatasetSchemaId(datasetId),
          tableSchemaVO);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID, e);
    }
  }

  /**
   * Delete table schema.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @DeleteMapping("/{datasetId}/tableSchema/{tableSchemaId}")
  public void deleteTableSchema(@PathVariable("datasetId") Long datasetId,
      @PathVariable("tableSchemaId") String tableSchemaId) {
    try {
      final String datasetSchemaId = dataschemaService.getDatasetSchemaId(datasetId);

      // Delete the Pk if needed from the catalogue, for all the fields of the table
      dataschemaService.deleteFromPkCatalogue(datasetSchemaId, tableSchemaId);

      dataschemaService.deleteTableSchema(datasetSchemaId, tableSchemaId);

      // we delete the rules associate to the table
      rulesControllerZuul.deleteRuleByReferenceId(datasetSchemaId, tableSchemaId);

      datasetService.deleteTableValue(datasetId, tableSchemaId);

    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.EXECUTION_ERROR, e);
    }
  }

  /**
   * Order table schema.
   *
   * @param datasetId the dataset id
   * @param orderVO the order VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @PutMapping("/{datasetId}/tableSchema/order")
  public void orderTableSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody OrderVO orderVO) {
    try {
      // Update the fieldSchema from the datasetSchema
      if (Boolean.FALSE.equals(
          dataschemaService.orderTableSchema(dataschemaService.getDatasetSchemaId(datasetId),
              orderVO.getId(), orderVO.getPosition()))) {
        throw new EEAException(EEAErrorMessage.EXECUTION_ERROR);
      }
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.SCHEMA_NOT_FOUND, e);
    }
  }

  /**
   * Creates the field schema and propagate the mew field to create it in the dataset for testing.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaVO the field schema VO
   *
   * @return the string
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @PostMapping("/{datasetId}/fieldSchema")
  public String createFieldSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody final FieldSchemaVO fieldSchemaVO) {

    if (StringUtil.isNullOrEmpty(fieldSchemaVO.getName())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FIELD_NAME_NULL);
    }
    try {
      String datasetSchemaId = dataschemaService.getDatasetSchemaId(datasetId);
      String response = dataschemaService.createFieldSchema(datasetSchemaId, fieldSchemaVO);
      if (StringUtils.isBlank(response)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.INVALID_OBJECTID);
      }
      // propagate the new field to the existing records in the dataset value
      datasetService.prepareNewFieldPropagation(datasetId, fieldSchemaVO);
      // with that we create the rule automatic required

      if (Boolean.TRUE.equals(fieldSchemaVO.getRequired())) {

        rulesControllerZuul.createAutomaticRule(datasetSchemaId, fieldSchemaVO.getId(),
            fieldSchemaVO.getType(), EntityTypeEnum.FIELD, datasetId, Boolean.TRUE);
      }
      // and with it we create the others automatic rules like number etc
      rulesControllerZuul.createAutomaticRule(datasetSchemaId, fieldSchemaVO.getId(),
          fieldSchemaVO.getType(), EntityTypeEnum.FIELD, datasetId, Boolean.FALSE);

      // Add the Pk if needed to the catalogue
      dataschemaService.addToPkCatalogue(fieldSchemaVO);

      // Add the register into the metabase fieldRelations
      dataschemaService.addForeignRelation(datasetId, fieldSchemaVO);

      return (response);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.INVALID_OBJECTID,
          e);
    }

  }

  /**
   * Update field schema and propagate the rules to dataset.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaVO the field schema VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @PutMapping("/{datasetId}/fieldSchema")
  public void updateFieldSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody FieldSchemaVO fieldSchemaVO) {
    try {
      final String datasetSchema = dataschemaService.getDatasetSchemaId(datasetId);
      // Update the fieldSchema from the datasetSchema
      if (Boolean.TRUE.equals(dataschemaService.checkPkAllowUpdate(datasetSchema, fieldSchemaVO))) {

        // Modify the register into the metabase fieldRelations
        dataschemaService.updateForeignRelation(datasetId, fieldSchemaVO, datasetSchema);

        DataType type = dataschemaService.updateFieldSchema(datasetSchema, fieldSchemaVO);

        // After the update, we create the rules needed and change the type of the field if
        // neccessary
        dataschemaService.propagateRulesAfterUpdateSchema(datasetSchema, fieldSchemaVO, type,
            datasetId);

        // Add the Pk if needed to the catalogue
        dataschemaService.addToPkCatalogue(fieldSchemaVO);
      } else {
        if (fieldSchemaVO.getPk() != null && fieldSchemaVO.getPk()) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.PK_ALREADY_EXISTS);
        }
        if (fieldSchemaVO.getPk() != null && !fieldSchemaVO.getPk()) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.PK_REFERENCED);
        }
      }
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.FIELD_SCHEMA_ID_NOT_FOUND, e);
    }
  }

  /**
   * Delete field schema.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaId the field schema id
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @DeleteMapping("/{datasetId}/fieldSchema/{fieldSchemaId}")
  public void deleteFieldSchema(@PathVariable("datasetId") Long datasetId,
      @PathVariable("fieldSchemaId") String fieldSchemaId) {
    try {
      String datasetSchemaId = dataschemaService.getDatasetSchemaId(datasetId);
      FieldSchemaVO fieldVO = dataschemaService.getFieldSchema(datasetSchemaId, fieldSchemaId);
      // Validate if the field we want to delete is a PK and it's being referenced by another field
      if (!dataschemaService.checkExistingPkReferenced(fieldVO)) {
        // Delete the fieldSchema from the datasetSchema
        if (!dataschemaService.deleteFieldSchema(datasetSchemaId, fieldSchemaId)) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.INVALID_OBJECTID);
        }
        // Delete the rules from the fieldSchema
        rulesControllerZuul.deleteRuleByReferenceId(datasetSchemaId, fieldSchemaId);

        // Delete FK rules
        if (null != fieldVO && fieldVO.getType().equals(DataType.LINK)) {
          rulesControllerZuul.deleteRuleByReferenceFieldSchemaPKId(datasetSchemaId, fieldSchemaId);
        }
        // Delete the fieldSchema from the dataset
        datasetService.deleteFieldValues(datasetId, fieldSchemaId);

        // Delete the Pk if needed from the catalogue
        dataschemaService.deleteFromPkCatalogue(fieldVO);

        // Delete the foreign relation between idDatasets in metabase, if needed
        dataschemaService.deleteForeignRelation(datasetId, fieldVO);
      } else {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.PK_REFERENCED);
      }
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.INVALID_OBJECTID,
          e);
    }
  }

  /**
   * Order field schema.
   *
   * @param datasetId the dataset id
   * @param orderVO the order VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @PutMapping("/{datasetId}/fieldSchema/order")
  public void orderFieldSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody OrderVO orderVO) {
    try {
      // Update the fieldSchema from the datasetSchema
      if (Boolean.FALSE.equals(
          dataschemaService.orderFieldSchema(dataschemaService.getDatasetSchemaId(datasetId),
              orderVO.getId(), orderVO.getPosition()))) {
        throw new EEAException(EEAErrorMessage.EXECUTION_ERROR);
      }
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.SCHEMA_NOT_FOUND,
          e);
    }
  }

  /**
   * Update dataset schema description.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaVO the dataset schema VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @PutMapping("/{datasetId}/datasetSchema")
  public void updateDatasetSchemaDescription(@PathVariable("datasetId") Long datasetId,
      @RequestBody(required = false) DataSetSchemaVO datasetSchemaVO) {
    try {
      if (!dataschemaService.updateDatasetSchemaDescription(
          dataschemaService.getDatasetSchemaId(datasetId), datasetSchemaVO.getDescription())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.EXECUTION_ERROR);
      }
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.SCHEMA_NOT_FOUND,
          e);
    }
  }


  /**
   * Validate schema.
   *
   * @param datasetSchemaId the dataset schema id
   *
   * @return the boolean
   */
  @Override
  @GetMapping(value = "{schemaId}/validate", produces = MediaType.APPLICATION_JSON_VALUE)
  public Boolean validateSchema(@PathVariable("schemaId") String datasetSchemaId) {
    return dataschemaService.validateSchema(datasetSchemaId);
  }


  /**
   * Validate schemas.
   *
   * @param dataflowId the dataflow id
   *
   * @return the boolean
   */
  @Override
  @GetMapping(value = "/validate/dataflow/{dataflowId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Boolean validateSchemas(@PathVariable("dataflowId") Long dataflowId) {
    // Recover the designs datasets of the dataflowId given. And then, for each design dataset
    // executes a validation.
    // At the first wrong design dataset, it stops and returns false. Otherwise it returns true
    DataFlowVO dataflow = dataflowControllerZuul.findById(dataflowId);
    Boolean isValid = false;
    if (dataflow.getDesignDatasets() != null && !dataflow.getDesignDatasets().isEmpty()) {
      isValid = dataflow.getDesignDatasets().parallelStream().noneMatch(
          ds -> Boolean.FALSE.equals(dataschemaService.validateSchema(ds.getDatasetSchema())));
    }
    return isValid;
  }


  /**
   * Find data schemas by id dataflow.
   *
   * @param idDataflow the id dataflow
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/getSchemas/dataflow/{idDataflow}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public List<DataSetSchemaVO> findDataSchemasByIdDataflow(
      @PathVariable("idDataflow") Long idDataflow) {

    List<DataSetSchemaVO> schemas = new ArrayList<>();

    List<DesignDatasetVO> designs = designDatasetService.getDesignDataSetIdByDataflowId(idDataflow);
    designs.stream().forEach(design -> {
      try {
        schemas.add(dataschemaService.getDataSchemaByDatasetId(false, design.getId()));
      } catch (EEAException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.SCHEMA_NOT_FOUND,
            e);
      }
    });
    return schemas;
  }


  /**
   * Gets the unique constraints.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the unique constraints
   */
  @Override
  @GetMapping(value = "{schemaId}/getUniqueConstraints",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<UniqueConstraintVO> getUniqueConstraints(
      @PathVariable("schemaId") String datasetSchemaId) {
    // Return dummy data to mock the service
    UniqueConstraintVO uniqueConstraint1 = new UniqueConstraintVO();
    UniqueConstraintVO uniqueConstraint2 = new UniqueConstraintVO();
    List<String> idFieldSchema1 = new ArrayList<>();
    List<String> idFieldSchema2 = new ArrayList<>();
    List<UniqueConstraintVO> uniques = new ArrayList<>();
    uniqueConstraint1.setDatasetSchemaId(datasetSchemaId);
    uniqueConstraint2.setDatasetSchemaId(datasetSchemaId);
    uniqueConstraint1.setTableSchemaId(new ObjectId().toString());
    uniqueConstraint2.setTableSchemaId(new ObjectId().toString());
    uniqueConstraint1.setUniqueId(new ObjectId().toString());
    uniqueConstraint2.setUniqueId(new ObjectId().toString());
    idFieldSchema1.add(new ObjectId().toString());
    idFieldSchema1.add(new ObjectId().toString());
    idFieldSchema2.add(new ObjectId().toString());
    idFieldSchema2.add(new ObjectId().toString());
    uniqueConstraint1.setFieldSchemaIds(idFieldSchema1);
    uniqueConstraint2.setFieldSchemaIds(idFieldSchema2);
    uniques.add(uniqueConstraint1);
    uniques.add(uniqueConstraint2);
    return uniques;
  }


  /**
   * Creates the unique constraint.
   *
   * @param uniqueConstraint the unique constraint
   */
  @Override
  @PostMapping(value = "/createUniqueConstraint", produces = MediaType.APPLICATION_JSON_VALUE)
  public void createUniqueConstraint(@RequestBody UniqueConstraintVO uniqueConstraint) {
    if (uniqueConstraint != null) {
      if (uniqueConstraint.getDatasetSchemaId() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.IDDATASETSCHEMA_INCORRECT);
      } else if (uniqueConstraint.getTableSchemaId() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.IDTABLESCHEMA_INCORRECT);
      }
    } else {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.UNREPORTED_DATA);
    }
  }

  /**
   * Delete unique constraint.
   *
   * @param uniqueConstraintId the unique constraint id
   */
  @Override
  @DeleteMapping(value = "/deleteUniqueConstraint/{uniqueConstraintId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void deleteUniqueConstraint(
      @PathVariable("uniqueConstraintId") String uniqueConstraintId) {
    if (uniqueConstraintId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.IDUNQUECONSTRAINT_INCORRECT);
    }
  }

  /**
   * Update unique constraint.
   *
   * @param uniqueConstraint the unique constraint
   */
  @Override
  @PutMapping(value = "/updateUniqueConstraint", produces = MediaType.APPLICATION_JSON_VALUE)
  public void updateUniqueConstraint(@RequestBody UniqueConstraintVO uniqueConstraint) {
    if (uniqueConstraint != null) {
      if (uniqueConstraint.getDatasetSchemaId() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.IDDATASETSCHEMA_INCORRECT);
      } else if (uniqueConstraint.getTableSchemaId() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.IDTABLESCHEMA_INCORRECT);
      }
    } else {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.UNREPORTED_DATA);
    }
  }

}
