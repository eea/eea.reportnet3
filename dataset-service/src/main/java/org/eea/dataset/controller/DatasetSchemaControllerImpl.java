package org.eea.dataset.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.ContributorController.ContributorControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
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
import org.eea.interfaces.vo.dataset.schemas.SimpleDatasetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaIdNameVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.uniqueContraintVO.UniqueConstraintVO;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.netty.util.internal.StringUtil;

/**
 * The Class DatasetSchemaControllerImpl.
 */
@RestController
@RequestMapping("/dataschema")
public class DatasetSchemaControllerImpl implements DatasetSchemaController {

  /** The Constant REGEX_NAME: {@value}. */
  private static final String REGEX_NAME = "[a-zA-Z0-9\\s_-]+";

  /** The Constant REGEX_NAME_SCHEMA: {@value}. */
  private static final String REGEX_NAME_SCHEMA = "[a-zA-Z0-9\\s\\(\\)_-]+";

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetSchemaControllerImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The dataset service. */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /** The dataschema service. */
  @Autowired
  private DatasetSchemaService dataschemaService;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The dataset snapshot service. */
  @Autowired
  private DatasetSnapshotService datasetSnapshotService;

  /** The record store controller zuul. */
  @Autowired
  private RecordStoreControllerZuul recordStoreControllerZuul;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The rules controller zuul. */
  @Autowired
  private RulesControllerZuul rulesControllerZuul;

  /** The design dataset service. */
  @Autowired
  private DesignDatasetService designDatasetService;

  /** The contributor controller zuul. */
  @Autowired
  private ContributorControllerZuul contributorControllerZuul;

  /** The integration controller zuul. */
  @Autowired
  private IntegrationControllerZuul integrationControllerZuul;

  /**
   * Creates the empty dataset schema.
   *
   * @param dataflowId the dataflow id
   * @param datasetSchemaName the dataset schema name
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/createEmptyDatasetSchema")
  @PreAuthorize("(secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD')) OR (secondLevelAuthorize(#dataflowId,'DATAFLOW_EDITOR_WRITE'))")
  public void createEmptyDatasetSchema(@RequestParam("dataflowId") final Long dataflowId,
      @RequestParam("datasetSchemaName") String datasetSchemaName) {

    String nameTrimmed = datasetSchemaName.trim();
    boolean isSchema = true;
    filterName(nameTrimmed, isSchema);
    datasetSchemaName = nameTrimmed;

    if (!TypeStatusEnum.DESIGN
        .equals(dataflowControllerZuul.getMetabaseById(dataflowId).getStatus())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid dataflow status");
    }

    if (0 != datasetMetabaseService.countDatasetNameByDataflowId(dataflowId, datasetSchemaName)) {
      LOG.error("Error creating duplicated dataset : {}", datasetSchemaName);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_NAME_DUPLICATED);
    }
    try {
      String datasetSchemaId = dataschemaService.createEmptyDataSetSchema(dataflowId).toString();
      Future<Long> futureDatasetId = datasetMetabaseService.createEmptyDataset(
          DatasetTypeEnum.DESIGN, datasetSchemaName, datasetSchemaId, dataflowId, null, null, 0);

      // we find if the dataflow has any permission to give the permission to this new datasetschema
      contributorControllerZuul.createAssociatedPermissions(dataflowId, futureDatasetId.get());

      integrationControllerZuul.createDefaultIntegration(dataflowId, datasetSchemaId);
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
  @GetMapping(value = "/datasetId/{datasetId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#datasetId,'DATASET_STEWARD','DATASET_OBSERVER','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_REPORTER_READ','DATASCHEMA_STEWARD','DATASCHEMA_LEAD_REPORTER','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASCHEMA_CUSTODIAN','DATASCHEMA_REPORTER_READ','DATASCHEMA_LEAD_REPORTER','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','DATASET_NATIONAL_COORDINATOR')")
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  public void deleteDatasetSchema(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "forceDelete", required = false) Boolean forceDelete) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    String datasetSchemaId = getDatasetSchemaId(datasetId);
    if (datasetSchemaId.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DATASET_NOTFOUND);
    }
    // Check if the dataflow has any PK being referenced by an FK. If so, denies the delete
    // If forceDelete = true, skip this check. Made specially for deleting an entire dataflow
    if ((forceDelete == null || !forceDelete)
        && !dataschemaService.isSchemaAllowedForDeletion(datasetSchemaId)) {
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
        dataschemaService.updatePkCatalogueDeletingSchema(datasetSchemaId, datasetId);

        // delete the schema in Mongo
        dataschemaService.deleteDatasetSchema(datasetSchemaId, datasetId);

        // delete from the UniqueConstraint catalog
        dataschemaService.deleteUniquesConstraintFromDataset(datasetSchemaId);

        // delete integrations related to this datasetSchema
        integrationControllerZuul.deleteSchemaIntegrations(datasetSchemaId);

        // delete the schema to dataset
        rulesControllerZuul.deleteRulesSchema(datasetSchemaId, datasetId);
        // delete the metabase
        datasetMetabaseService.deleteDesignDataset(datasetId);
        // delete the schema in database
        recordStoreControllerZuul.deleteDataset("dataset_" + datasetId);

        dataschemaService.deleteGroup(datasetId, ResourceTypeEnum.DATA_SCHEMA);
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @PostMapping(value = "/{datasetId}/tableSchema", produces = MediaType.APPLICATION_JSON_VALUE)
  public TableSchemaVO createTableSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody TableSchemaVO tableSchemaVO) {

    String nameTrimmed = tableSchemaVO.getNameTableSchema().trim();
    boolean isSchema = false;
    filterName(nameTrimmed, isSchema);
    tableSchemaVO.setNameTableSchema(nameTrimmed);

    if (!TypeStatusEnum.DESIGN.equals(dataflowControllerZuul
        .getMetabaseById(datasetService.getDataFlowIdById(datasetId)).getStatus())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid dataflow status");
    }

    try {
      ThreadPropertiesManager.setVariable("user",
          SecurityContextHolder.getContext().getAuthentication().getName());
      tableSchemaVO = dataschemaService.createTableSchema(
          dataschemaService.getDatasetSchemaId(datasetId), tableSchemaVO, datasetId);
      datasetService.saveTablePropagation(datasetId, tableSchemaVO);
      // recordStoreControllerZuul.createUpdateQueryView(datasetId, false);
      dataschemaService.releaseCreateUpdateView(datasetId,
          SecurityContextHolder.getContext().getAuthentication().getName(), false);
      return tableSchemaVO;
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @PutMapping("/{datasetId}/tableSchema")
  public void updateTableSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody TableSchemaVO tableSchemaVO) {

    boolean isSchema = false;
    if (null != tableSchemaVO.getNameTableSchema()) {
      String nameTrimmed = tableSchemaVO.getNameTableSchema().trim();
      filterName(nameTrimmed, isSchema);
      tableSchemaVO.setNameTableSchema(nameTrimmed);
    }

    if (!TypeStatusEnum.DESIGN.equals(dataflowControllerZuul
        .getMetabaseById(datasetService.getDataFlowIdById(datasetId)).getStatus())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid dataflow status");
    }

    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    try {
      dataschemaService.updateTableSchema(datasetId, tableSchemaVO);
      // recordStoreControllerZuul.createUpdateQueryView(datasetId, false);
      dataschemaService.releaseCreateUpdateView(datasetId,
          SecurityContextHolder.getContext().getAuthentication().getName(), false);
    } catch (EEAException e) {
      if (e.getMessage() != null
          && e.getMessage().equals(String.format(EEAErrorMessage.ERROR_UPDATING_TABLE_SCHEMA,
              tableSchemaVO.getIdTableSchema(), datasetId))) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            String.format(EEAErrorMessage.ERROR_UPDATING_TABLE_SCHEMA,
                tableSchemaVO.getIdTableSchema(), datasetId),
            e);
      }
      if (e.getMessage() != null && e.getMessage().equals(String
          .format(EEAErrorMessage.TABLE_NOT_FOUND, tableSchemaVO.getIdTableSchema(), datasetId))) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(String
            .format(EEAErrorMessage.TABLE_NOT_FOUND, tableSchemaVO.getIdTableSchema(), datasetId)),
            e);
      } else {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.DATASET_INCORRECT_ID, e);
      }
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @DeleteMapping("/{datasetId}/tableSchema/{tableSchemaId}")
  public void deleteTableSchema(@PathVariable("datasetId") Long datasetId,
      @PathVariable("tableSchemaId") String tableSchemaId) {

    if (!TypeStatusEnum.DESIGN.equals(dataflowControllerZuul
        .getMetabaseById(datasetService.getDataFlowIdById(datasetId)).getStatus())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid dataflow status");
    }

    try {
      final String datasetSchemaId = dataschemaService.getDatasetSchemaId(datasetId);

      // Delete the Pk if needed from the catalogue, for all the fields of the table
      dataschemaService.deleteFromPkCatalogue(datasetSchemaId, tableSchemaId, datasetId);

      // Delete the Uniques constraints in table
      dataschemaService.deleteUniquesConstraintFromTable(tableSchemaId);

      dataschemaService.deleteTableSchema(datasetSchemaId, tableSchemaId, datasetId);

      // we delete the rules associate to the table
      rulesControllerZuul.deleteRuleByReferenceId(datasetSchemaId, tableSchemaId);

      datasetService.deleteTableValue(datasetId, tableSchemaId);

      recordStoreControllerZuul.createUpdateQueryView(datasetId, false);
      LOG.info("A table has been deleted in the datasetId {}", datasetId);
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @PutMapping("/{datasetId}/tableSchema/order")
  public void orderTableSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody OrderVO orderVO) {

    if (!TypeStatusEnum.DESIGN.equals(dataflowControllerZuul
        .getMetabaseById(datasetService.getDataFlowIdById(datasetId)).getStatus())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid dataflow status");
    }

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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @PostMapping("/{datasetId}/fieldSchema")
  public String createFieldSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody final FieldSchemaVO fieldSchemaVO) {

    if (!TypeStatusEnum.DESIGN.equals(dataflowControllerZuul
        .getMetabaseById(datasetService.getDataFlowIdById(datasetId)).getStatus())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid dataflow status");
    }

    if (StringUtil.isNullOrEmpty(fieldSchemaVO.getName())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FIELD_NAME_NULL);
    }

    String nameTrimmed = fieldSchemaVO.getName().trim();
    boolean isSchema = false;
    filterName(nameTrimmed, isSchema);
    fieldSchemaVO.setName(nameTrimmed);

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
      dataschemaService.addToPkCatalogue(fieldSchemaVO, datasetId);

      // Add the register into the metabase fieldRelations
      dataschemaService.addForeignRelation(datasetId, fieldSchemaVO);

      // Add UniqueConstraint if needed
      dataschemaService.createUniqueConstraintPK(datasetSchemaId, fieldSchemaVO);

      // Create query view
      // recordStoreControllerZuul.createUpdateQueryView(datasetId, false);
      dataschemaService.releaseCreateUpdateView(datasetId,
          SecurityContextHolder.getContext().getAuthentication().getName(), false);
      return (response);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @PutMapping("/{datasetId}/fieldSchema")
  public void updateFieldSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody FieldSchemaVO fieldSchemaVO) {

    if (null != fieldSchemaVO.getName()) {
      String nameTrimmed = fieldSchemaVO.getName().trim();
      boolean isSchema = false;
      filterName(nameTrimmed, isSchema);
      fieldSchemaVO.setName(nameTrimmed);
    }

    if (!TypeStatusEnum.DESIGN.equals(dataflowControllerZuul
        .getMetabaseById(datasetService.getDataFlowIdById(datasetId)).getStatus())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid dataflow status");
    }

    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    try {
      final String datasetSchema = dataschemaService.getDatasetSchemaId(datasetId);
      // Update the fieldSchema from the datasetSchema
      if (Boolean.TRUE.equals(dataschemaService.checkPkAllowUpdate(datasetSchema, fieldSchemaVO))) {

        // Modify the register into the metabase fieldRelations
        dataschemaService.updateForeignRelation(datasetId, fieldSchemaVO, datasetSchema);

        // Clear the attachments if necessary
        if (Boolean.TRUE.equals(
            dataschemaService.checkClearAttachments(datasetId, datasetSchema, fieldSchemaVO))) {
          datasetService.deleteAttachmentByFieldSchemaId(datasetId, fieldSchemaVO.getId());
        }

        DataType type =
            dataschemaService.updateFieldSchema(datasetSchema, fieldSchemaVO, datasetId, false);

        // Create query view
        // recordStoreControllerZuul.createUpdateQueryView(datasetId, false);
        // After the update, we create the rules needed and change the type of the field if
        // neccessary
        dataschemaService.propagateRulesAfterUpdateSchema(datasetSchema, fieldSchemaVO, type,
            datasetId);

        // Add the Pk if needed to the catalogue
        dataschemaService.addToPkCatalogue(fieldSchemaVO, datasetId);
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
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
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
  @LockMethod
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @DeleteMapping("/{datasetId}/fieldSchema/{fieldSchemaId}")
  public void deleteFieldSchema(
      @PathVariable("datasetId") @LockCriteria(name = "datasetId") Long datasetId,
      @PathVariable("fieldSchemaId") @LockCriteria(name = "fieldSchemaId") String fieldSchemaId) {

    if (!TypeStatusEnum.DESIGN.equals(dataflowControllerZuul
        .getMetabaseById(datasetService.getDataFlowIdById(datasetId)).getStatus())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid dataflow status");
    }

    try {
      String datasetSchemaId = dataschemaService.getDatasetSchemaId(datasetId);
      FieldSchemaVO fieldVO = dataschemaService.getFieldSchema(datasetSchemaId, fieldSchemaId);
      // Validate if the field we want to delete is a PK and it's being referenced by another field
      if (!dataschemaService.checkExistingPkReferenced(fieldVO)) {
        // Delete the fieldSchema from the datasetSchema
        if (!dataschemaService.deleteFieldSchema(datasetSchemaId, fieldSchemaId, datasetId)) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.INVALID_OBJECTID);
        }
        // Delete the rules from the fieldSchema
        rulesControllerZuul.deleteRuleByReferenceId(datasetSchemaId, fieldSchemaId);

        // Delete uniques constraints
        dataschemaService.deleteUniquesConstraintFromField(datasetSchemaId, fieldSchemaId);

        // Delete FK rules
        if (null != fieldVO && (DataType.LINK.equals(fieldVO.getType())
            || DataType.EXTERNAL_LINK.equals(fieldVO.getType()))) {
          rulesControllerZuul.deleteRuleByReferenceFieldSchemaPKId(datasetSchemaId, fieldSchemaId);
        }
        // Delete the fieldSchema from the dataset
        datasetService.deleteFieldValues(datasetId, fieldSchemaId);

        // Delete the Pk if needed from the catalogue
        dataschemaService.deleteFromPkCatalogue(fieldVO, datasetId);

        // Delete the foreign relation between idDatasets in metabase, if needed
        dataschemaService.deleteForeignRelation(datasetId, fieldVO);

        // Create query view
        dataschemaService.releaseCreateUpdateView(datasetId,
            SecurityContextHolder.getContext().getAuthentication().getName(), false);
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @PutMapping("/{datasetId}/fieldSchema/order")
  public void orderFieldSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody OrderVO orderVO) {

    if (!TypeStatusEnum.DESIGN.equals(dataflowControllerZuul
        .getMetabaseById(datasetService.getDataFlowIdById(datasetId)).getStatus())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid dataflow status");
    }

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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @PutMapping("/{datasetId}/datasetSchema")
  public void updateDatasetSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody(required = true) DataSetSchemaVO datasetSchemaVO) {

    try {
      String datasetSchemaId = dataschemaService.getDatasetSchemaId(datasetId);

      if (null != datasetSchemaVO.getAvailableInPublic()) {
        dataschemaService.updateDatasetSchemaExportable(datasetSchemaId,
            datasetSchemaVO.getAvailableInPublic());
      }

      if (TypeStatusEnum.DESIGN.equals(dataflowControllerZuul
          .getMetabaseById(datasetService.getDataFlowIdById(datasetId)).getStatus())) {
        if (null != datasetSchemaVO.getDescription()) {
          dataschemaService.updateDatasetSchemaDescription(datasetSchemaId,
              datasetSchemaVO.getDescription());
        }
        if (null != datasetSchemaVO.getWebform()) {
          dataschemaService.updateWebform(datasetSchemaId, datasetSchemaVO.getWebform());
        }
        if (null != datasetSchemaVO.getReferenceDataset()) {
          dataschemaService.updateReferenceDataset(datasetId, datasetSchemaId,
              datasetSchemaVO.getReferenceDataset(), false);
        }
      }
    } catch (EEAException e) {
      LOG_ERROR.error("updateDatasetSchema - DatasetSchema not found: datasetId={}", datasetId);
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
  @PreAuthorize("isAuthenticated()")
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
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_REQUESTER','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId))")
  public Boolean validateSchemas(@PathVariable("dataflowId") Long dataflowId) {
    // Recover the designs datasets of the dataflowId given. And then, for each design dataset
    // executes a validation.
    // At the first wrong design dataset, it stops and returns false. Otherwise it returns true
    DataFlowVO dataflow = dataflowControllerZuul.findById(dataflowId, null);
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
  @PreAuthorize("secondLevelAuthorize(#idDataflow,'DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#idDataflow))")
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
   * @param dataflowId the dataflow id
   *
   * @return the unique constraints
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER','DATAFLOW_NATIONAL_COORDINATOR','DATAFLOW_OBSERVER','DATAFLOW_STEWARD')")
  @GetMapping(value = "{schemaId}/getUniqueConstraints/dataflow/{dataflowId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<UniqueConstraintVO> getUniqueConstraints(
      @PathVariable("schemaId") String datasetSchemaId,
      @PathVariable("dataflowId") Long dataflowId) {
    if (datasetSchemaId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.IDDATASETSCHEMA_INCORRECT);
    }
    return dataschemaService.getUniqueConstraints(datasetSchemaId);
  }

  /**
   * Gets the unique constraints.
   *
   * @param uniqueId the unique id
   *
   * @return the unique constraints
   */
  @Override
  @GetMapping(value = "/private/getUniqueConstraint/{uniqueId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public UniqueConstraintVO getUniqueConstraint(@PathVariable("uniqueId") String uniqueId) {
    try {
      return dataschemaService.getUniqueConstraint(uniqueId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * Creates the unique constraint.
   *
   * @param uniqueConstraint the unique constraint
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#uniqueConstraint.dataflowId,'DATAFLOW_EDITOR_WRITE', 'DATAFLOW_CUSTODIAN', 'DATAFLOW_STEWARD')")
  @PostMapping(value = "/createUniqueConstraint")
  public void createUniqueConstraint(@RequestBody UniqueConstraintVO uniqueConstraint) {
    if (uniqueConstraint != null) {
      if (uniqueConstraint.getDatasetSchemaId() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.IDDATASETSCHEMA_INCORRECT);
      } else if (uniqueConstraint.getTableSchemaId() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.IDTABLESCHEMA_INCORRECT);
      } else if (uniqueConstraint.getFieldSchemaIds() == null
          || uniqueConstraint.getFieldSchemaIds().isEmpty()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.UNREPORTED_FIELDSCHEMAS);
      }
    } else {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.UNREPORTED_DATA);
    }
    dataschemaService.createUniqueConstraint(uniqueConstraint);
  }

  /**
   * Delete unique constraint.
   *
   * @param uniqueConstraintId the unique constraint id
   * @param dataflowId the dataflow id
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_EDITOR_WRITE','DATAFLOW_CUSTODIAN', 'DATAFLOW_STEWARD')")
  @DeleteMapping(value = "/deleteUniqueConstraint/{uniqueConstraintId}/dataflow/{dataflowId}")
  public void deleteUniqueConstraint(@PathVariable("uniqueConstraintId") String uniqueConstraintId,
      @PathVariable("dataflowId") Long dataflowId) {
    try {
      dataschemaService.deleteUniqueConstraint(uniqueConstraintId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * Update unique constraint.
   *
   * @param uniqueConstraint the unique constraint
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#uniqueConstraint.dataflowId,'DATAFLOW_EDITOR_WRITE', 'DATAFLOW_CUSTODIAN', 'DATAFLOW_STEWARD')")
  @PutMapping(value = "/updateUniqueConstraint", produces = MediaType.APPLICATION_JSON_VALUE)
  public void updateUniqueConstraint(@RequestBody UniqueConstraintVO uniqueConstraint) {
    if (uniqueConstraint == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.UNREPORTED_DATA);
    }
    if (uniqueConstraint.getDatasetSchemaId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.IDDATASETSCHEMA_INCORRECT);
    }
    if (uniqueConstraint.getTableSchemaId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.IDTABLESCHEMA_INCORRECT);
    }
    if (uniqueConstraint.getUniqueId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.IDUNQUECONSTRAINT_INCORRECT);
    }
    if (uniqueConstraint.getFieldSchemaIds() == null
        || uniqueConstraint.getFieldSchemaIds().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.UNREPORTED_FIELDSCHEMAS);
    }

    dataschemaService.updateUniqueConstraint(uniqueConstraint);
  }

  /**
   * Copy designs from dataflow.
   *
   * @param dataflowIdOrigin the dataflow id origin
   * @param dataflowIdDestination the dataflow id destination
   *
   *        Copy the design datasets of a dataflow (origin) into the current dataflow (target) It's
   *        an async call. It sends a notification when all the process it's done
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowIdDestination,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN')")
  @LockMethod(removeWhenFinish = false)
  @PostMapping(value = "/copy", produces = MediaType.APPLICATION_JSON_VALUE)
  public void copyDesignsFromDataflow(
      @RequestParam("sourceDataflow") @LockCriteria(
          name = "dataflowIdOrigin") final Long dataflowIdOrigin,
      @RequestParam("targetDataflow") @LockCriteria(
          name = "dataflowIdDestination") final Long dataflowIdDestination) {
    try {
      // Set the user name on the thread
      ThreadPropertiesManager.setVariable("user",
          SecurityContextHolder.getContext().getAuthentication().getName());
      designDatasetService.copyDesignDatasets(dataflowIdOrigin, dataflowIdDestination);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * Gets the simple schema.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   *
   * @return the simple schema
   */
  @Override
  @PreAuthorize("checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_STEWARD','DATASET_CUSTODIAN','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD')")
  @GetMapping(value = "/getSimpleSchema/dataset/{datasetId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public SimpleDatasetSchemaVO getSimpleSchema(@PathVariable("datasetId") Long datasetId,
      @RequestParam("dataflowId") Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      return dataschemaService.getSimpleSchema(datasetId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  /**
   * Gets the table schemas ids.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @return the table schemas ids
   */
  @Override
  @PreAuthorize("checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','DATACOLLECTION_CUSTODIAN','DATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR')")
  @GetMapping(value = "/getTableSchemasIds/{datasetId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<TableSchemaIdNameVO> getTableSchemasIds(@PathVariable("datasetId") Long datasetId,
      @RequestParam("dataflowId") Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      return dataschemaService.getTableSchemasIds(datasetId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }



  /**
   * Export schemas.
   *
   * @param dataflowId the dataflow id
   *
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD')")
  @GetMapping(value = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<byte[]> exportSchemas(@RequestParam("dataflowId") final Long dataflowId) {
    try {
      // Set the user name on the thread
      ThreadPropertiesManager.setVariable("user",
          SecurityContextHolder.getContext().getAuthentication().getName());
      byte[] fileZip = dataschemaService.exportSchemas(dataflowId);
      String fileName = "dataflow_export_" + dataflowId + ".zip";
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
      LOG.info("Schemas from the dataflowId {} exported", dataflowId);
      return new ResponseEntity<>(fileZip, httpHeaders, HttpStatus.OK);
    } catch (Exception e) {
      LOG_ERROR.error("Error exporting schemas from the dataflowId {}. Message: {}", dataflowId,
          e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  /**
   * Import schemas.
   *
   * @param dataflowId the dataflow id
   * @param file the file
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/import")
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD')")
  public void importSchemas(@RequestParam(value = "dataflowId") Long dataflowId,
      @RequestParam("file") MultipartFile file) {
    if (!TypeStatusEnum.DESIGN
        .equals(dataflowControllerZuul.getMetabaseById(dataflowId).getStatus())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid dataflow status");
    }
    try {
      // Set the user name on the thread
      ThreadPropertiesManager.setVariable("user",
          SecurityContextHolder.getContext().getAuthentication().getName());
      dataschemaService.importSchemas(dataflowId, file.getInputStream(),
          file.getOriginalFilename());
    } catch (Exception e) {
      LOG_ERROR.error("Error importing schemas on the dataflowId {}. Message: {}", dataflowId,
          e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }


  /**
   * Filter name.
   *
   * @param nameTrimmed the name trimmed
   * @param isSchema the is schema
   */
  private void filterName(String nameTrimmed, boolean isSchema) {
    if (isSchema) {
      if (!Pattern.matches(REGEX_NAME_SCHEMA, nameTrimmed)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.DATASET_SCHEMA_INVALID_NAME_ERROR);
      }
    } else {
      if (!Pattern.matches(REGEX_NAME, nameTrimmed)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.DATASET_SCHEMA_INVALID_NAME_ERROR);
      }
    }
  }
}
