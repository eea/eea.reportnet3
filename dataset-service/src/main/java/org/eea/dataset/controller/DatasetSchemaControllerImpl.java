package org.eea.dataset.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.netty.util.internal.StringUtil;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.service.*;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.controller.dataflow.ContributorController.ContributorControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.OrderVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.*;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The Class DatasetSchemaControllerImpl.
 */
@RestController
@RequestMapping("/dataschema")
@Api(tags = "Dataset Schema : Dataset Schema Manager")
public class DatasetSchemaControllerImpl implements DatasetSchemaController {

  /** The Constant REGEX_NAME: {@value}. */
  private static final String REGEX_NAME = "[a-zA-Z0-9\\s_-]+";

  /** The Constant REGEX_NAME_SCHEMA: {@value}. */
  private static final String REGEX_NAME_SCHEMA = "[a-zA-Z0-9\\s\\(\\)_-]+";

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetSchemaControllerImpl.class);

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

  /** The notification controller zuul. */
  @Autowired
  private NotificationControllerZuul notificationControllerZuul;


  /**
   * Creates the empty dataset schema.
   *
   * @param dataflowId the dataflow id
   * @param datasetSchemaName the dataset schema name
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/createEmptyDatasetSchema")
  @PreAuthorize("(secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD','DATAFLOW_EDITOR_WRITE'))")
  @ApiOperation(value = "Create empty Dataset Schema", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully create"),
          @ApiResponse(code = 400, message = EEAErrorMessage.DATASET_NAME_DUPLICATED),
          @ApiResponse(code = 403, message = "invalid dataflow status"),
          @ApiResponse(code = 500, message = "Error creating dataset")})
  public void createEmptyDatasetSchema(
          @ApiParam(type = "Long", value = "Dataflow Id",
                  example = "0") @RequestParam("dataflowId") final Long dataflowId,
          @ApiParam(type = "String", value = "Dataset schema name",
                  example = "abc") @RequestParam("datasetSchemaName") String datasetSchemaName) {

    String nameTrimmed = datasetSchemaName.trim();
    boolean isSchema = true;
    filterName(nameTrimmed, isSchema);
    datasetSchemaName = nameTrimmed;

    if (!TypeStatusEnum.DESIGN
            .equals(dataflowControllerZuul.getMetabaseById(dataflowId).getStatus())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid dataflow status for dataflowId " + dataflowId);
    }

    if (0 != datasetMetabaseService.countDatasetNameByDataflowId(dataflowId, datasetSchemaName)) {
      LOG.error("Error creating duplicated dataset with datasetSchemaName {} for dataflowId {}", datasetSchemaName, dataflowId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DATASET_NAME_DUPLICATED);
    }
    try {
      LOG.info("Creating dataset schema {} for dataflowId {} ", nameTrimmed, dataflowId);
      String datasetSchemaId = dataschemaService.createEmptyDataSetSchema(dataflowId).toString();
      Future<Long> futureDatasetId = datasetMetabaseService.createEmptyDataset(
              DatasetTypeEnum.DESIGN, datasetSchemaName, datasetSchemaId, dataflowId, null, null, 0);

      // we find if the dataflow has any permission to give the permission to this new datasetschema
      contributorControllerZuul.createAssociatedPermissions(dataflowId, futureDatasetId.get());
      integrationControllerZuul.createDefaultIntegration(dataflowId, datasetSchemaId);
      LOG.info("Successfully created dataset schema {} with datasetSchemaId {} for dataflowId {} ", nameTrimmed, datasetSchemaId, dataflowId);
    } catch (InterruptedException | ExecutionException | EEAException e) {
      LOG.error("Aborted DataSetSchema creation for schema {} and dataflowId {}. Error is: {}", nameTrimmed, dataflowId, e.getMessage(), e);
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating design dataset for dataflowId " + dataflowId);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error creating empty dataset schema {} for dataflowId {} Message: {}", datasetSchemaName, dataflowId, e.getMessage());
      throw e;
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
  @GetMapping(value = "/private/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find dataschema by Id", hidden = true)
  public DataSetSchemaVO findDataSchemaById(@ApiParam(type = "String", value = "dataset schema Id",
          example = "5cf0e9b3b793310e9ceca190") @PathVariable("id") String id) {
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
  @GetMapping(value = "/v1/datasetId/{datasetId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#datasetId,'DATASET_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','TESTDATASET_OBSERVER','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT','REFERENCEDATASET_STEWARD') OR hasAnyRole('ADMIN') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @ApiOperation(value = "Get dataset schema by dataset id",
          notes = "Allowed roles: \n\n Reporting dataset: STEWARD, LEAD REPORTER, CUSTODIAN, OBSERVER, REPORTER WRITE, REPORTER READ, NATIONAL COORDINATOR, STEWARD SUPPORT \n\n Data collection: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT\n\n Test dataset: CUSTODIAN, STEWARD ,OBSERVER, STEWARD SUPPORT\n\n Reference dataset: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT\n\n Design dataset: CUSTODIAN, STEWARD, EDITOR READ, EDITOR WRITE\n\n EU dataset: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT ")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 500, message = "Error getting the data")})
  public DataSetSchemaVO findDataSchemaByDatasetId(@ApiParam(type = "Long", value = "Dataset id",
          example = "0") @PathVariable("datasetId") Long datasetId) {
    try {
      return dataschemaService.getDataSchemaByDatasetId(true, datasetId);
    } catch (EEAException e) {
      LOG.error("Error while retrieving dataset schema by id: {}", datasetId, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.RETRIEVING_DATASET_SCHEMA);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving data schema for datasetId {} Message: {}", datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Find data schema by dataset id legacy.
   *
   * @param datasetId the dataset id
   * @return the data set schema VO
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/datasetId/{datasetId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#datasetId,'DATASET_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','TESTDATASET_OBSERVER','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT','REFERENCEDATASET_STEWARD') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @ApiOperation(value = "Get dataset schema by dataset id", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 500, message = "Error getting the data")})
  public DataSetSchemaVO findDataSchemaByDatasetIdLegacy(@ApiParam(type = "Long",
          value = "Dataset id", example = "0") @PathVariable("datasetId") Long datasetId) {
    return this.findDataSchemaByDatasetId(datasetId);
  }

  /**
   * Find data schema by dataset id private.
   *
   * @param datasetId the dataset id
   * @return the data set schema VO
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/publicDatasetId/{datasetId}",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find public dataschema by Dataset Id", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 500, message = "Error getting the data")})
  public DataSetSchemaVO findDataSchemaByDatasetIdPrivate(@ApiParam(type = "Long",
          value = "Dataset Id", example = "0") @PathVariable("datasetId") Long datasetId) {
    try {
      return dataschemaService.getDataSchemaByDatasetId(true, datasetId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error privately retrieving data schema for datasetId {} Message: {}", datasetId, e.getMessage());
      throw e;
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
  @GetMapping("/private/getDataSchema/{datasetId}")
  @ApiOperation(value = "Find dataset schema Id", hidden = true)
  public String getDatasetSchemaId(@ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @PathVariable("datasetId") Long datasetId) {
    try {
      return dataschemaService.getDatasetSchemaId(datasetId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving data schema id for datasetId {} Message: {}", datasetId, e.getMessage());
      throw e;
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
  @ApiOperation(value = "Find data schmea with no rules by dataset Id", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 500, message = "Error getting the data")})
  public DataSetSchemaVO findDataSchemaWithNoRulesByDatasetId(@ApiParam(type = "Long",
          value = "Dataset Id", example = "0") @PathVariable("datasetId") Long datasetId) {
    try {
      return dataschemaService.getDataSchemaByDatasetId(false, datasetId);
    } catch (EEAException e) {
      LOG.error("Error while retrieving dataset schema by id: {}", datasetId, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.RETRIEVING_DATASET_SCHEMA);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving data schema with no rules for datasetId {} Message: {}", datasetId, e.getMessage());
      throw e;
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
  @ApiOperation(value = "Delete Dataset Schema", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully delete"),
          @ApiResponse(code = 400, message = "dataset incorrect or execution error"),
          @ApiResponse(code = 404, message = EEAErrorMessage.DATASET_NOTFOUND),
          @ApiResponse(code = 401, message = EEAErrorMessage.PK_REFERENCED),
          @ApiResponse(code = 403, message = EEAErrorMessage.NOT_ENOUGH_PERMISSION),
          @ApiResponse(code = 500, message = "Error deleting")})
  public void deleteDatasetSchema(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Boolean", value = "force delete", example = "true") @RequestParam(
                  value = "forceDelete", required = false) Boolean forceDelete) {
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
      LOG.info("Deleting dataset schema with datasetId {}", datasetId);
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
      } else {
        LOG.error("Not enough permissions to delete dataset schema with datasetId {}", datasetId);
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                EEAErrorMessage.NOT_ENOUGH_PERMISSION);
      }
    } catch (EEAException e) {
      LOG.error("Error deleting dataset schema with datasetId {}. Message: {}", datasetId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DELETING_DESIGN_DATASET);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error deleting dataset schema for datasetId {} Message: {}", datasetId, e.getMessage());
      throw e;
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
  @ApiOperation(value = "Create Table Schema", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully create table"),
          @ApiResponse(code = 400, message = "dataset incorrect id"),
          @ApiResponse(code = 403, message = "invalid dataflow status")})
  public TableSchemaVO createTableSchema(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(value = "table schmea object") @RequestBody TableSchemaVO tableSchemaVO) {

    String nameTrimmed = tableSchemaVO.getNameTableSchema().trim();
    boolean isSchema = false;
    filterName(nameTrimmed, isSchema);
    tableSchemaVO.setNameTableSchema(nameTrimmed);

    if (!TypeStatusEnum.DESIGN.equals(dataflowControllerZuul
            .getMetabaseById(datasetService.getDataFlowIdById(datasetId)).getStatus())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid dataflow status");
    }

    try {
      LOG.info("Creating table schema for datasetId {}", datasetId);
      ThreadPropertiesManager.setVariable("user",
              SecurityContextHolder.getContext().getAuthentication().getName());
      tableSchemaVO = dataschemaService.createTableSchema(
              dataschemaService.getDatasetSchemaId(datasetId), tableSchemaVO, datasetId);
      datasetService.saveTablePropagation(datasetId, tableSchemaVO);
      // recordStoreControllerZuul.createUpdateQueryView(datasetId, false);
      dataschemaService.releaseCreateUpdateView(datasetId,
              SecurityContextHolder.getContext().getAuthentication().getName(), false);
      LOG.info("Successfully created table schema for datasetId {}", datasetId);
      return tableSchemaVO;
    } catch (EEAException e) {
      LOG.error("Error creating a table schema. DatasetId: {}. Message: {}", datasetId,
              e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DATASET_INCORRECT_ID);
    } catch (Exception e) {
      String tableSchemaId = (tableSchemaVO != null) ? tableSchemaVO.getIdTableSchema() : null;
      LOG.error("Unexpected error! Error creating table schema {} for datasetId {} Message: {}", tableSchemaId, datasetId, e.getMessage());
      throw e;
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
  @ApiOperation(value = "Update table schema", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully update table"),
          @ApiResponse(code = 400,
                  message = "dataset incorrect id,table not found or error updating table schema"),
          @ApiResponse(code = 403, message = "invalid dataflow status")})
  public void updateTableSchema(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(value = "table schema object") @RequestBody TableSchemaVO tableSchemaVO) {
    boolean isSchema = false;
    if (null != tableSchemaVO.getNameTableSchema()) {
      String nameTrimmed = tableSchemaVO.getNameTableSchema().trim();
      filterName(nameTrimmed, isSchema);
      tableSchemaVO.setNameTableSchema(nameTrimmed);
    }
    Long dataflowId = datasetService.getDataFlowIdById(datasetId);
    if (!TypeStatusEnum.DESIGN.equals(dataflowControllerZuul
            .getMetabaseById(dataflowId).getStatus())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid dataflow status");
    }

    ThreadPropertiesManager.setVariable("user",
            SecurityContextHolder.getContext().getAuthentication().getName());

    try {
      Boolean updateMaterializedViews = true;
      DataFlowVO dataFlowVO = dataflowControllerZuul.findById(dataflowId, null);
      if(dataFlowVO.getBigData() != null && dataFlowVO.getBigData()) {
        updateMaterializedViews = false;
      }
      dataschemaService.updateTableSchema(datasetId, tableSchemaVO, updateMaterializedViews);
    } catch (EEAException e) {
      LOG.error("Error updating table schema for datasetId {}. Message: {}", datasetId, e.getMessage(), e);
      if (e.getMessage() != null
              && e.getMessage().equals(String.format(EEAErrorMessage.ERROR_UPDATING_TABLE_SCHEMA,
              tableSchemaVO.getIdTableSchema(), datasetId))) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                String.format(EEAErrorMessage.ERROR_UPDATING_TABLE_SCHEMA,
                        tableSchemaVO.getIdTableSchema(), datasetId));
      }
      if (e.getMessage() != null && e.getMessage().equals(String
              .format(EEAErrorMessage.TABLE_NOT_FOUND, tableSchemaVO.getIdTableSchema(), datasetId))) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String
                .format(EEAErrorMessage.TABLE_NOT_FOUND, tableSchemaVO.getIdTableSchema(), datasetId));
      } else {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                EEAErrorMessage.DATASET_INCORRECT_ID);
      }
    } catch (Exception e) {
      String tableSchemaId = (tableSchemaVO != null) ? tableSchemaVO.getIdTableSchema() : null;
      LOG.error("Unexpected error! Error updating table schema {} for datasetId {} Message: {}", tableSchemaId, datasetId, e.getMessage());
      throw e;
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
  @ApiOperation(value = "Delete table schema", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully delete table"),
          @ApiResponse(code = 500, message = EEAErrorMessage.EXECUTION_ERROR),
          @ApiResponse(code = 403, message = "invalid dataflow status")})
  public void deleteTableSchema(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "String", value = "table Schema Id",
                  example = "5cf0e9b3b793310e9ceca190") @PathVariable("tableSchemaId") String tableSchemaId) {

    if (!TypeStatusEnum.DESIGN.equals(dataflowControllerZuul
            .getMetabaseById(datasetService.getDataFlowIdById(datasetId)).getStatus())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid dataflow status");
    }

    try {
      final String datasetSchemaId = dataschemaService.getDatasetSchemaId(datasetId);

      LOG.info("Deleting table schema with id {} for datasetId {}",tableSchemaId, datasetId);
      // Delete the Pk if needed from the catalogue, for all the fields of the table
      dataschemaService.deleteFromPkCatalogue(datasetSchemaId, tableSchemaId, datasetId);

      // Delete the Uniques constraints in table
      dataschemaService.deleteUniquesConstraintFromTable(tableSchemaId);

      dataschemaService.deleteTableSchema(datasetSchemaId, tableSchemaId, datasetId);

      // we delete the rules associate to the table
      rulesControllerZuul.deleteRuleByReferenceId(datasetSchemaId, tableSchemaId);

      datasetService.deleteTableValue(datasetId, tableSchemaId);

      recordStoreControllerZuul.createUpdateQueryView(datasetId, false);
      LOG.info("Successfully deleted table schema with id {} for datasetId {}",tableSchemaId, datasetId);
    } catch (EEAException e) {
      LOG.error("Error deleting table schema with id {} for datasetId {} Message: {}", tableSchemaId, datasetId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.DELETING_TABLE_SCHEMA);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error deleting table schema {} for datasetId {} Message: {}", tableSchemaId, datasetId, e.getMessage());
      throw e;
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
  @ApiOperation(value = "Order table schema", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully order table"),
          @ApiResponse(code = 404, message = "schema not found"),
          @ApiResponse(code = 403, message = "invalid dataflow status")})
  public void orderTableSchema(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(value = "Order object") @RequestBody OrderVO orderVO) {

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
      LOG.error("Error ordering table schema. DatasetId: {}. Message: {}", datasetId,
              e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.SCHEMA_NOT_FOUND);
    } catch (Exception e) {
      String orderId = (orderVO != null) ? orderVO.getId() : null;
      LOG.error("Unexpected error! Error ordering table schema with orderId {} for datasetId {} Message: {}", orderId, datasetId, e.getMessage());
      throw e;
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
  @ApiOperation(value = "Create field schema", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully create field"),
          @ApiResponse(code = 400, message = "field name null or invalid object id"),
          @ApiResponse(code = 403, message = "invalid dataflow status")})
  public String createFieldSchema(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(value = "Field schema object") @RequestBody final FieldSchemaVO fieldSchemaVO) {

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
      LOG.error("Error creating field schema. DatasetId: {}. Message: {}", datasetId,
              e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.CREATING_FIELD_SCHEMA);
    } catch (Exception e) {
      String fieldSchemaId = (fieldSchemaVO != null) ? fieldSchemaVO.getId() : null;
      LOG.error("Unexpected error! Error creating field schema {} for datasetId {} Message: {}", fieldSchemaId, datasetId, e.getMessage());
      throw e;
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
  @ApiOperation(value = "Update field schema", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully update field"),
          @ApiResponse(code = 400, message = "pk already exist or pk referenced"),
          @ApiResponse(code = 403, message = "invalid dataflow status")})
  public void updateFieldSchema(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(value = "Field schema object") @RequestBody FieldSchemaVO fieldSchemaVO) {

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
      LOG.error("Error updating field schema. DatasetId: {}. Message: {}", datasetId,
              e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.UPDATING_FIELD_SCHEMA);
    } catch (Exception e) {
      String fieldSchemaId = (fieldSchemaVO != null) ? fieldSchemaVO.getId() : null;
      LOG.error("Unexpected error! Error updating field schema {} for datasetId {} Message: {}", fieldSchemaId, datasetId, e.getMessage());
      throw e;
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
  @ApiOperation(value = "delete field schema", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully delete field"),
          @ApiResponse(code = 400, message = "invalid object id or pk referenced"),
          @ApiResponse(code = 403, message = "invalid dataflow status")})
  public void deleteFieldSchema(@ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @PathVariable("datasetId") @LockCriteria(name = "datasetId") Long datasetId,
                                @ApiParam(type = "String", value = "Dataset Id",
                                        example = "5cf0e9b3b793310e9ceca190") @PathVariable("fieldSchemaId") @LockCriteria(
                                        name = "fieldSchemaId") String fieldSchemaId) {

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
      LOG.error("Error deleting field schema. DatasetId: {}. Message: {}", datasetId,
              e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DELETING_FIELD_SCHEMA);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error deleting field schema {} for datasetId {} Message: {}", fieldSchemaId, datasetId, e.getMessage());
      throw e;
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
  @ApiOperation(value = "Order field schema", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully order field"),
          @ApiResponse(code = 400, message = "schema not found"),
          @ApiResponse(code = 403, message = "invalid dataflow status")})
  public void orderFieldSchema(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(value = "order objcet") @RequestBody OrderVO orderVO) {

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
      LOG.error("Error ordering field schema. DatasetId: {}. Message: {}", datasetId,
              e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.SCHEMA_NOT_FOUND);
    } catch (Exception e) {
      String orderId = (orderVO != null) ? orderVO.getId() : null;
      LOG.error("Unexpected error! Error ordering field schema with orderId {} for datasetId {} Message: {}", orderId, datasetId, e.getMessage());
      throw e;
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
  @ApiOperation(value = "Update dataset schema", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully update dataset schema"),
          @ApiResponse(code = 400, message = "schema not found")})
  public void updateDatasetSchema(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(value = "dataset schema object") @RequestBody(
                  required = true) DataSetSchemaVO datasetSchemaVO) {

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
                  datasetSchemaVO.getReferenceDataset());
        }
      }
    } catch (EEAException e) {
      LOG.error("updateDatasetSchema - DatasetSchema not found: datasetId={}", datasetId, e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.SCHEMA_NOT_FOUND);
    } catch (Exception e) {
      String datasetSchemaId = (datasetSchemaVO != null) ? datasetSchemaVO.getIdDataSetSchema() : null;
      LOG.error("Unexpected error! Error updating dataset schema {} for datasetId {} Message: {}", datasetSchemaId, datasetId, e.getMessage());
      throw e;
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
  @ApiOperation(value = "validate schema", hidden = true)
  public Boolean validateSchema(@ApiParam(type = "String", value = "Dataset Schema Id",
          example = "5cf0e9b3b793310e9ceca190") @PathVariable("schemaId") String datasetSchemaId) {
    try {
      LOG.info("Validating schema with datasetSchemaId {}", datasetSchemaId);
      return dataschemaService.validateSchema(datasetSchemaId, null);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error validating dataset schema {} Message: {}", datasetSchemaId, e.getMessage());
      throw e;
    }
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
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_STEWARD_SUPPORT','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_REQUESTER','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR hasAnyRole('ADMIN')")
  @ApiOperation(value = "validate schemas", hidden = true)
  public Boolean validateSchemas(@ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @PathVariable("dataflowId") Long dataflowId) {
    try {
      // Recover the designs datasets of the dataflowId given. And then, for each design dataset
      // executes a validation.
      // At the first wrong design dataset, it stops and returns false. Otherwise it returns true
      LOG.info("Validating schemas for dataflowId {}", dataflowId);
      DataFlowVO dataflow = dataflowControllerZuul.findById(dataflowId, null);
      Boolean isValid = false;
      if (dataflow.getDesignDatasets() != null && !dataflow.getDesignDatasets().isEmpty()) {
        Set<Boolean> results = dataflow.getDesignDatasets().parallelStream()
                .map(ds -> dataschemaService.validateSchema(ds.getDatasetSchema(), dataflow.getType()))
                .collect(Collectors.toSet());
        isValid = results.contains(true) && !results.contains(false);
      }
      LOG.info("Successfully validated schemas for dataflowId {}. Result is {}", dataflowId, isValid);
      return isValid;
    } catch (Exception e) {
      LOG.error("Unexpected error! Error validating dataset schemas for dataflowId {} Message: {}", dataflowId, e.getMessage());
      throw e;
    }
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
  @ApiOperation(value = "Find dataschemas by dataflow Id", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 400, message = "schema not found")})
  public List<DataSetSchemaVO> findDataSchemasByIdDataflow(@ApiParam(type = "Long",
          value = "Dataflow Id", example = "0") @PathVariable("idDataflow") Long idDataflow) {
    try {
      List<DataSetSchemaVO> schemas = new ArrayList<>();

      List<DesignDatasetVO> designs = designDatasetService.getDesignDataSetIdByDataflowId(idDataflow);
      designs.stream().forEach(design -> {
        try {
          schemas.add(dataschemaService.getDataSchemaByDatasetId(false, design.getId()));
        } catch (EEAException e) {
          LOG.error("Error finding dataset schema by dataflowId {}. Message: {}",
                  idDataflow, e.getMessage(), e);
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.SCHEMA_NOT_FOUND);
        } catch (Exception e) {
          Long datasetId = (design != null) ? design.getId() : null;
          LOG.error("Unexpected error! Error retrieving dataset schema with id {} for dataflowId {} Message: {}", datasetId, idDataflow, e.getMessage());
          throw e;
        }
      });
      return schemas;
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving dataset schemas for dataflowId {} Message: {}", idDataflow, e.getMessage());
      throw e;
    }
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
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER','DATAFLOW_NATIONAL_COORDINATOR','DATAFLOW_OBSERVER','DATAFLOW_STEWARD_SUPPORT','DATAFLOW_STEWARD','DATAFLOW_REPORTER_READ','DATAFLOW_REPORTER_WRITE')")
  @GetMapping(value = "{schemaId}/getUniqueConstraints/dataflow/{dataflowId}",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find unique constraints", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 400, message = EEAErrorMessage.IDDATASETSCHEMA_INCORRECT)})
  public List<UniqueConstraintVO> getUniqueConstraints(
          @ApiParam(type = "String", value = "Dataset schema Id",
                  example = "5cf0e9b3b793310e9ceca190") @PathVariable("schemaId") String datasetSchemaId,
          @ApiParam(type = "Long", value = "Dataflow Id",
                  example = "0") @PathVariable("dataflowId") Long dataflowId) {
    if (datasetSchemaId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.IDDATASETSCHEMA_INCORRECT);
    }
    return dataschemaService.getUniqueConstraints(datasetSchemaId);
  }

  /**
   * Gets the public unique constraints.
   *
   * @param datasetSchemaId the dataset schema id
   * @param dataflowId the dataflow id
   * @return the public unique constraints
   */
  @Override
  @GetMapping(value = "{schemaId}/getPublicUniqueConstraints/dataflow/{dataflowId}",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find unique constraints", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 400, message = EEAErrorMessage.IDDATASETSCHEMA_INCORRECT)})
  public List<UniqueConstraintVO> getPublicUniqueConstraints(
          @ApiParam(type = "String", value = "Dataset schema Id",
                  example = "5cf0e9b3b793310e9ceca190") @PathVariable("schemaId") String datasetSchemaId,
          @ApiParam(type = "Long", value = "Dataflow Id",
                  example = "0") @PathVariable("dataflowId") Long dataflowId) {
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
  @ApiOperation(value = "Get unique constraint", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 400, message = "bad request")})
  public UniqueConstraintVO getUniqueConstraint(@ApiParam(type = "String", value = "Unique Id",
          example = "5cf0e9b3b793310e9ceca190") @PathVariable("uniqueId") String uniqueId) {
    try {
      return dataschemaService.getUniqueConstraint(uniqueId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.IDDATASETSCHEMA_INCORRECT);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving unique constraint {} Message: {}", uniqueId, e.getMessage());
      throw e;
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
  @ApiOperation(value = "Create unique constaint", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully create unique"),
          @ApiResponse(code = 400,
                  message = "Data schema id or table schema id is incorrect, unreported fieldschemas or data")})
  public void createUniqueConstraint(@ApiParam(
          value = "Unique constraint object") @RequestBody UniqueConstraintVO uniqueConstraint) {
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
      LOG.error("Error when creating unique constraint because uniqueConstraint object is null");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.UNREPORTED_DATA);
    }
    try {
      dataschemaService.createUniqueConstraint(uniqueConstraint);
    } catch (Exception e) {
      String uniqueId = (uniqueConstraint != null) ? uniqueConstraint.getUniqueId() : null;
      String datasetSchemaId = (uniqueConstraint != null) ? uniqueConstraint.getDatasetSchemaId() : null;
      String tableSchemaId = (uniqueConstraint != null) ? uniqueConstraint.getTableSchemaId() : null;
      LOG.error("Unexpected error! Error creating unique constraint {} for datasetSchemaId {} and tableSchemaId {}. Message: {}", uniqueId, datasetSchemaId, tableSchemaId, e.getMessage());
      throw e;
    }
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
  @ApiOperation(value = "Delete unique constraint", hidden = true)
  @ApiResponses(
          value = {@ApiResponse(code = 200, message = "Successfully delete unique constraint"),
                  @ApiResponse(code = 400, message = "bad request")})
  public void deleteUniqueConstraint(@ApiParam(type = "String", value = "Unique Id",
          example = "5cf0e9b3b793310e9ceca190") @PathVariable("uniqueConstraintId") String uniqueConstraintId,
                                     @ApiParam(type = "Long", value = "Dataflow Id",
                                             example = "0") @PathVariable("dataflowId") Long dataflowId) {
    try {
      dataschemaService.deleteUniqueConstraint(uniqueConstraintId);
    } catch (EEAException e) {
      LOG.error("Error when deleting unique constraint with uid {} for dataflowId {}", uniqueConstraintId, dataflowId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.IDDATASETSCHEMA_INCORRECT);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error deleting unique constraint {} for dataflowId {} Message: {}", uniqueConstraintId, dataflowId, e.getMessage());
      throw e;
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
  @ApiOperation(value = "Update unique constraint", hidden = true)
  public void updateUniqueConstraint(@ApiParam(
          value = "Unique constraint object") @RequestBody UniqueConstraintVO uniqueConstraint) {
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

    try {
      dataschemaService.updateUniqueConstraint(uniqueConstraint);
    } catch (Exception e) {
      String uniqueId = (uniqueConstraint != null) ? uniqueConstraint.getUniqueId() : null;
      String datasetSchemaId = (uniqueConstraint != null) ? uniqueConstraint.getDatasetSchemaId() : null;
      String tableSchemaId = (uniqueConstraint != null) ? uniqueConstraint.getTableSchemaId() : null;
      LOG.error("Unexpected error! Error updating unique constraint {} for datasetSchemaId {} and tableSchemaId {}. Message: {}", uniqueId, datasetSchemaId, tableSchemaId, e.getMessage());
      throw e;
    }
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
  @PreAuthorize("secondLevelAuthorize(#dataflowIdDestination,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN') && secondLevelAuthorize(#dataflowIdOrigin,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN')")
  @LockMethod(removeWhenFinish = false)
  @ApiOperation(value = "Copy designs from dataflow", hidden = true)
  @PostMapping(value = "/copy", produces = MediaType.APPLICATION_JSON_VALUE)
  public void copyDesignsFromDataflow(
          @ApiParam(type = "Long", value = "Dataflow Id Origin",
                  example = "0") @RequestParam("sourceDataflow") final Long dataflowIdOrigin,
          @ApiParam(type = "Long", value = "Dataflow Id destination",
                  example = "0") @RequestParam("targetDataflow") @LockCriteria(
                  name = "dataflowIdDestination") final Long dataflowIdDestination) {

    try {
      // Set the user name on the thread
      ThreadPropertiesManager.setVariable("user",
              SecurityContextHolder.getContext().getAuthentication().getName());
      designDatasetService.copyDesignDatasets(dataflowIdOrigin, dataflowIdDestination);
    } catch (EEAException e) {
      LOG.error(
              "Error copying data from another dataflow. Origin DataflowId {}. Destination DataflowId {}. Message: {}",
              dataflowIdOrigin, dataflowIdDestination, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.COPYING_DESIGN_DATAFLOW);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error copying designs from dataflow for dataflowIdOrigin {} and dataflowIdDestination {}. Message: {}", dataflowIdOrigin, dataflowIdDestination, e.getMessage());
      throw e;
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
  @PreAuthorize("checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_STEWARD','DATASET_CUSTODIAN','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @GetMapping(value = "/v1/getSimpleSchema/dataset/{datasetId}",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get dataset schema by dataset id",
          notes = "Allowed roles: \n\n Reporting dataset: STEWARD, LEAD REPORTER, CUSTODIAN, REPORTER WRITE, REPORTER READ, NATIONAL COORDINATOR \n\n Data collection: CUSTODIAN, STEWARD \n\n Test dataset: CUSTODIAN, STEWARD, STEWARD SUPPORT \n\n Design dataset: CUSTODIAN, STEWARD, EDITOR WRITE \n\n EU dataset: CUSTODIAN, STEWARD ")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 500, message = "Error getting data"),
          @ApiResponse(code = 400, message = EEAErrorMessage.DATASET_INCORRECT_ID)})
  public SimpleDatasetSchemaVO getSimpleSchema(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam("dataflowId") Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      return dataschemaService.getSimpleSchema(datasetId);
    } catch (EEAException e) {
      LOG.error(
              "Error retrieving simple dataset schema by dataflowId. DataflowId {}. DatasetId {}. Message: {}",
              dataflowId, datasetId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.RETRIEVING_DATASET_SIMPLE_SCHEMA);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving simple schema for dataflowId {} datasetId {} and providerId {}. Message: {}", dataflowId, datasetId, providerId, e.getMessage());
      throw e;
    }
  }

  /**
   * Gets the simple schema legacy.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @return the simple schema legacy
   */
  @Override
  @PreAuthorize("checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_STEWARD','DATASET_CUSTODIAN','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @GetMapping(value = "/getSimpleSchema/dataset/{datasetId}",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get dataset schema by dataset id", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 500, message = "Error getting data"),
          @ApiResponse(code = 400, message = EEAErrorMessage.DATASET_INCORRECT_ID)})
  public SimpleDatasetSchemaVO getSimpleSchemaLegacy(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam("dataflowId") Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId) {
    return this.getSimpleSchema(datasetId, dataflowId, providerId);
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
  @PreAuthorize("checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_STEWARD','DATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_STEWARD','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD') OR hasAnyRole('ADMIN')")
  @GetMapping(value = "/v1/getTableSchemasIds/{datasetId}",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get list of dataset table schemas by dataset id",
          notes = "Allowed roles: \n\n Reporting dataset: STEWARD, LEAD REPORTER, CUSTODIAN, REPORTER WRITE, REPORTER READ, NATIONAL COORDINATOR \n\n Data collection: CUSTODIAN, STEWARD \n\n Test dataset: CUSTODIAN, STEWARD, STEWARD SUPPORT \n\n Reference dataset: CUSTODIAN, STEWARD \n\n Design dataset: CUSTODIAN, STEWARD, EDITOR WRITE \n\n EU dataset: CUSTODIAN, STEWARD ")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 500, message = "Error getting data"),
          @ApiResponse(code = 400, message = EEAErrorMessage.DATASET_INCORRECT_ID)})
  public List<TableSchemaIdNameVO> getTableSchemasIds(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam("dataflowId") Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      return dataschemaService.getTableSchemasIds(datasetId);
    } catch (EEAException e) {
      LOG.error(
              "Error retrieving table schema by datasetId. DataflowId {}. DatasetId {}. Message: {}",
              dataflowId, datasetId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.RETRIEVING_TABLE_SCHEMAS);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving table schema ids for dataflowId {} datasetId {} and providerId {}. Message: {}", dataflowId, datasetId, providerId, e.getMessage());
      throw e;
    }
  }

  /**
   * Gets the table schemas ids legacy.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @return the table schemas ids legacy
   */
  @Override
  @PreAuthorize("checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_STEWARD','DATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_STEWARD','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD')")
  @GetMapping(value = "/getTableSchemasIds/{datasetId}",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get list of dataset table schemas by dataset id", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 500, message = "Error getting data"),
          @ApiResponse(code = 400, message = EEAErrorMessage.DATASET_INCORRECT_ID)})
  public List<TableSchemaIdNameVO> getTableSchemasIdsLegacy(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam("dataflowId") Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId) {
    return this.getTableSchemasIds(datasetId, dataflowId, providerId);
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
  @ApiOperation(value = "Export schemas", hidden = true)
  public ResponseEntity<byte[]> exportSchemas(@ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @RequestParam("dataflowId") final Long dataflowId) {
    try {
      // Set the user name on the thread
      ThreadPropertiesManager.setVariable("user",
              SecurityContextHolder.getContext().getAuthentication().getName());
      LOG.info("Exporting schemas from the dataflowId {}", dataflowId);
      byte[] fileZip = dataschemaService.exportSchemas(dataflowId);
      String fileName = "dataflow_export_" + dataflowId + ".zip";
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
      LOG.info("Successfully exported schemas from the dataflowId {}", dataflowId);
      return new ResponseEntity<>(fileZip, httpHeaders, HttpStatus.OK);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error exporting schemas from the dataflowId {}. Message: {}", dataflowId,
              e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.EXPORTING_SCHEMAS);
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
  @LockMethod(removeWhenFinish = false)
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD')")
  @ApiOperation(value = "Import schemas", hidden = true)
  public void importSchemas(
          @ApiParam(type = "Long", value = "Dataflow Id", example = "0") @LockCriteria(
                  name = "dataflowId") @RequestParam(value = "dataflowId") Long dataflowId,
          @ApiParam(value = "File") @RequestParam("file") MultipartFile file) {

    DataFlowVO dataflowVO = dataflowControllerZuul.getMetabaseById(dataflowId);
    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDataflowId(dataflowId);
    userNotificationContentVO.setDataflowName(dataflowVO.getName());
    notificationControllerZuul.createUserNotificationPrivate("IMPORT_DATASET_SCHEMA_INIT",
            userNotificationContentVO);

    if (!TypeStatusEnum.DESIGN
            .equals(dataflowControllerZuul.getMetabaseById(dataflowId).getStatus())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid dataflow status");
    }
    try {
      // Set the user name on the thread
      ThreadPropertiesManager.setVariable("user",
              SecurityContextHolder.getContext().getAuthentication().getName());
      LOG.info("Importing schemas from the dataflowId {}", dataflowId);
      dataschemaService.importSchemas(dataflowId, file.getInputStream(),
              file.getOriginalFilename());
      LOG.info("Successfully imported schemas from the dataflowId {}", dataflowId);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error importing schemas on the dataflowId {}. Message: {}", dataflowId,
              e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.IMPORTING_SCHEMAS);
    }
  }


  /**
   * Export field schemas.
   *
   * @param datasetSchemaId the dataset schema id
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @GetMapping(value = "/v1/{datasetSchemaId}/exportFieldSchemas",
          produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiOperation(value = "Export table definition by table schema id",
          notes = "Allowed roles: CUSTODIAN, STEWARD, EDITOR WRITE")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully export data"),
          @ApiResponse(code = 500, message = "Error exporting data")})
  public ResponseEntity<byte[]> exportFieldSchemas(@ApiParam(type = "String",
          value = "Dataset schema id",
          example = "5cf0e9b3b793310e9ceca190") @PathVariable("datasetSchemaId") String datasetSchemaId,
                                                   @ApiParam(type = "Long", value = "Dataset id",
                                                           example = "0") @RequestParam(value = "datasetId") final Long datasetId,
                                                   @ApiParam(type = "String", value = "Table schema id",
                                                           example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "tableSchemaId",
                                                           required = false) final String tableSchemaId) {

    try {
      LOG.info("Exporting field schemas for datasetId {} and tableSchemaId {}", datasetId, tableSchemaId);
      String fileName = "fieldschemas_export_dataset_" + datasetId + ".csv";
      byte[] file = dataschemaService.exportFieldsSchema(datasetId, datasetSchemaId, tableSchemaId);
      LOG.info("Successfully exported field schemas for datasetId {} and tableSchemaId {}", datasetId, tableSchemaId);
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
      return new ResponseEntity<>(file, httpHeaders, HttpStatus.OK);
    } catch (EEAException e) {
      LOG.error("Error exporting field schemas for datasetId {} and tableSchemaId {}", datasetId, tableSchemaId, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.EXPORTING_FIELD_SCHEMAS);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error exporting field schemas for datasetId {} and tableSchemaId {}. Message: {}", datasetId, tableSchemaId, e.getMessage());
      throw e;
    }
  }

  /**
   * Export field schemas legacy.
   *
   * @param datasetSchemaId the dataset schema id
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @GetMapping(value = "/{datasetSchemaId}/exportFieldSchemas",
          produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiOperation(value = "Export field schemas", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully export data"),
          @ApiResponse(code = 500, message = "Error exporting data")})
  public ResponseEntity<byte[]> exportFieldSchemasLegacy(@ApiParam(type = "String",
          value = "Dataset Schema Id",
          example = "5cf0e9b3b793310e9ceca190") @PathVariable("datasetSchemaId") String datasetSchemaId,
                                                         @ApiParam(type = "Long", value = "Dataset Id",
                                                                 example = "0") @RequestParam(value = "datasetId") final Long datasetId,
                                                         @ApiParam(type = "String", value = "Table schema Id",
                                                                 example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "tableSchemaId",
                                                                 required = false) final String tableSchemaId) {
    return this.exportFieldSchemas(datasetSchemaId, datasetId, tableSchemaId);
  }


  /**
   * Import field schemas.
   *
   * @param datasetSchemaId the dataset schema id
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param file the file
   * @param replace the replace
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/v1/{datasetSchemaId}/importFieldSchemas")
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @ApiOperation(value = "Import table definition by table schema id",
          notes = "Allowed roles: CUSTODIAN, STEWARD, EDITOR WRITE")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully import data"),
          @ApiResponse(code = 400, message = "Error importing file")})
  public void importFieldSchemas(@ApiParam(type = "String", value = "Dataset schema id",
          example = "5cf0e9b3b793310e9ceca190") @PathVariable("datasetSchemaId") String datasetSchemaId,
                                 @ApiParam(type = "Long", value = "Dataset id",
                                         example = "0") @RequestParam(value = "datasetId") Long datasetId,
                                 @ApiParam(type = "String", value = "Table schema id",
                                         example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "tableSchemaId",
                                         required = false) String tableSchemaId,
                                 @ApiParam(value = "File") @RequestParam("file") MultipartFile file,
                                 @ApiParam(type = "Boolean", value = "Replace current definition",
                                         example = "true") @RequestParam(value = "replace", required = false) Boolean replace) {

    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDatasetId(datasetId);
    notificationControllerZuul.createUserNotificationPrivate("IMPORT_TABLE_SCHEMA_INIT",
            userNotificationContentVO);

    try {
      // Set the user name on the thread
      ThreadPropertiesManager.setVariable("user",
              SecurityContextHolder.getContext().getAuthentication().getName());
      LOG.info("Importing field Schemas for datasetId {}", datasetId);
      dataschemaService.importFieldsSchema(tableSchemaId, datasetSchemaId, datasetId,
              file.getInputStream(), replace);
      LOG.info("Successfully imported field Schemas for datasetId {}", datasetId);
    } catch (IOException e) {
      LOG.error("File importing field schemas for datasetId {} failed. fileName={}", datasetId,
              file.getOriginalFilename(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error importing file");
    } catch (Exception e) {
      String fileName = (file != null) ? file.getName() : null;
      LOG.error("Unexpected error! Error importing field schemas from file {} for datasetId {} and tableSchemaId {}. Message: {}", fileName, datasetId, tableSchemaId, e.getMessage());
      throw e;
    }
  }

  /**
   * Import field schemas legacy.
   *
   * @param datasetSchemaId the dataset schema id
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param file the file
   * @param replace the replace
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/{datasetSchemaId}/importFieldSchemas")
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @ApiOperation(value = "Import field schemas", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully import data"),
          @ApiResponse(code = 400, message = "Error importing file")})
  public void importFieldSchemasLegacy(@ApiParam(type = "String", value = "Dataset schema Id",
          example = "5cf0e9b3b793310e9ceca190") @PathVariable("datasetSchemaId") String datasetSchemaId,
                                       @ApiParam(type = "Long", value = "Dataset Id",
                                               example = "0") @RequestParam(value = "datasetId") Long datasetId,
                                       @ApiParam(type = "String", value = "Table schema Id",
                                               example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "tableSchemaId",
                                               required = false) String tableSchemaId,
                                       @ApiParam(value = "File") @RequestParam("file") MultipartFile file,
                                       @ApiParam(type = "Boolean", value = "Replace",
                                               example = "true") @RequestParam(value = "replace", required = false) Boolean replace) {
    this.importFieldSchemas(datasetSchemaId, datasetId, tableSchemaId, file, replace);
  }

  /**
   * Export field schemas from dataset.
   *
   * @param datasetId the dataset id
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @GetMapping(value = "/v1/dataset/{datasetId}/exportFieldSchemas",
          produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiOperation(value = "Export dataset definition by dataset id",
          notes = "Allowed roles: CUSTODIAN, STEWARD, EDITOR WRITE")
  public ResponseEntity<byte[]> exportFieldSchemasFromDataset(@ApiParam(type = "Long",
          value = "Dataset id", example = "0") @PathVariable("datasetId") Long datasetId) {
    try {
      LOG.info("Exporting field Schemas for datasetId {}", datasetId);
      byte[] fileZip = dataschemaService.exportZipFieldSchemas(datasetId);
      String fileName = "fieldschemas_export_dataset_" + datasetId + ".zip";
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
      LOG.info("Successfully exported field Schemas for datasetId {}", datasetId);
      return new ResponseEntity<>(fileZip, httpHeaders, HttpStatus.OK);
    } catch (EEAException e) {
      LOG.error("Error exporting the zip field schemas for datasetId {}", datasetId, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.EXPORTING_DATASET_DEFINITION);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error importing field schemas from datasetId {} Message: {}", datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Export field schemas from dataset legacy.
   *
   * @param datasetId the dataset id
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @GetMapping(value = "/dataset/{datasetId}/exportFieldSchemas",
          produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiOperation(value = "Export field schemas from dataset", hidden = true)
  public ResponseEntity<byte[]> exportFieldSchemasFromDatasetLegacy(@ApiParam(type = "Long",
          value = "Dataset Id", example = "0") @PathVariable("datasetId") Long datasetId) {
    return this.exportFieldSchemasFromDataset(datasetId);
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

  /**
   * Get table schema name
   * @param datasetSchemaId
   * @param tableSchemaId
   * @return
   */
  @Override
  @GetMapping(value = "/private/getTableSchemName")
  public String getTableSchemaName(@RequestParam("datasetSchemaId") String datasetSchemaId, @RequestParam("tableSchemaId") String tableSchemaId) {
    return dataschemaService.getTableSchemaName(datasetSchemaId, tableSchemaId);
  }

  /**
   * Finds table schema
   * @param tableSchemaId
   * @param datasetSchemaId
   * @return
   */
  @Override
  @GetMapping(value = "/private/getTableSchemaVo")
  public TableSchemaVO getTableSchemaVO(@RequestParam("tableSchemaId") String tableSchemaId, @RequestParam("datasetSchemaId") String datasetSchemaId) {
    try {
      return dataschemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);
    } catch (Exception e) {
      LOG.error("Error retrieving table schema for tableSchemaId {} and datasetSchemaId {}", tableSchemaId, datasetSchemaId);
      throw e;
    }
  }

  /**
   * Finds field schema
   * @param datasetSchemaId
   * @param idFieldSchema
   * @return
   */
  @Override
  @GetMapping(value = "/private/getFieldSchemaVo")
  public FieldSchemaVO getFieldSchema(@RequestParam("datasetSchemaId") String datasetSchemaId, @RequestParam("idFieldSchema") String idFieldSchema) {
    return dataschemaService.getFieldSchema(datasetSchemaId, idFieldSchema);
  }

  /**
   * Finds field name
   * @param datasetSchemaId
   * @param tableSchemaId
   * @param parameters
   * @param ruleReferenceId
   * @param ruleReferenceFieldSchemaPKId
   * @return
   */
  @Override
  @GetMapping(value = "/private/getFieldName")
  public String getFieldName(@RequestParam("datasetSchemaId") String datasetSchemaId, @RequestParam("tableSchemaId") String tableSchemaId,
                             @RequestParam(value = "parameters", required = false) List<String> parameters, @RequestParam("ruleReferenceId") String ruleReferenceId,
                             @RequestParam(value = "ruleReferenceFieldSchemaPKId", required = false) String ruleReferenceFieldSchemaPKId) {
    try {
      return dataschemaService.getFieldName(datasetSchemaId, tableSchemaId, parameters, ruleReferenceId, ruleReferenceFieldSchemaPKId);
    } catch (Exception e) {
      LOG.error("Error finding field name for datasetSchemaId {}, tableSchemaId {}", datasetSchemaId, tableSchemaId);
    }
    return null;
  }
}
