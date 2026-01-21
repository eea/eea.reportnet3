package org.eea.dataset.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.*;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.service.DatasetTableService;
import org.eea.dataset.service.ResolveSnapshotTable;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaIdNameVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.metabase.ReleaseVO;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobInfoEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.lock.service.LockService;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The Class DatasetSnapshotControllerImpl.
 */
@RestController
@RequestMapping("/snapshot")
@Api(tags = "Dataset Snapshot : Dataset Snapshot Manager")
public class DatasetSnapshotControllerImpl implements DatasetSnapshotController {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetSnapshotControllerImpl.class);

  /** The dataset metabase service. */
  @Autowired
  private DatasetSnapshotService datasetSnapshotService;

  /** The reporting dataset repository. */
  @Autowired
  private ReportingDatasetRepository reportingDatasetRepository;

  /** The dataflow controller zull. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZull;

  /** The notification controller zuul. */
  @Autowired
  private NotificationControllerZuul notificationControllerZuul;

  /** The job controller zuul. */
  @Autowired
  private JobControllerZuul jobControllerZuul;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The process controller zuul */
  @Autowired
  private ProcessControllerZuul processControllerZuul;

  /** The dataset metabase controller zuul */
  @Autowired
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  @Autowired
  private DatasetSchemaService datasetSchemaService;

  @Autowired
  private DatasetTableService datasetTableService;

  @Autowired
  private ResolveSnapshotTable resolveSnapshotTable;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;


  @Value("${eea.authorization.key}")
  private String eeaAuthorizationKey;

  public static String staticEeaAuthorizationKey;

  @PostConstruct
  public void init() {
    staticEeaAuthorizationKey = eeaAuthorizationKey;
  }

  /**
   * The default release process priority
   */
  private int defaultRestoreProcessPriority = 20;

  /**
   * Gets the by id.
   *
   * @param idSnapshot the id snapshot
   * @return the by id
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/{idSnapshot}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "get snapshot by id", hidden = true)
  public SnapshotVO getById(@ApiParam(type = "Long", value = "snapshot Id",
          example = "0") @PathVariable("idSnapshot") Long idSnapshot) {
    SnapshotVO snapshot = null;
    try {
      snapshot = datasetSnapshotService.getById(idSnapshot);
    } catch (EEAException e) {
      LOG.error("Error getting the snapshot for snapshotId {}. Error message: {}", idSnapshot, e.getMessage(), e);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving snapshot with id {} Message: {}", idSnapshot, e.getMessage());
      throw e;
    }
    return snapshot;
  }


  @Override
  @HystrixCommand
  @GetMapping(value = "/private/schemaSnapshot/{idSnapshot}",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "get Schema by id snapshot", hidden = true)
  @PreAuthorize("isAuthenticated()")
  public SnapshotVO getSchemaById(@ApiParam(type = "Long", value = "snapshot Id",
          example = "0") @PathVariable("idSnapshot") Long idSnapshot) {
    SnapshotVO snapshot = null;
    try {
      snapshot = datasetSnapshotService.getSchemaById(idSnapshot);
    } catch (EEAException e) {
      LOG.error("Error getting the snapshot schema. ", e.getMessage(), e);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving snapshot schema with snapshotId {} Message: {}", idSnapshot, e.getMessage());
      throw e;
    }
    return snapshot;
  }

  /**
   * Gets the snapshots enabled by id dataset.
   *
   * @param datasetId the dataset id
   * @return the snapshots enabled by id dataset
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataset/{idDataset}/listSnapshots",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "get snapshots by dataset id", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_NATIONAL_COORDINATOR','DATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD','DATASET_STEWARD_SUPPORT')")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get snapshots"),
          @ApiResponse(code = 400, message = "Dataset id incorrect")})
  public List<SnapshotVO> getSnapshotsEnabledByIdDataset(@ApiParam(type = "Long",
          value = "Dataset Id", example = "0") @PathVariable("idDataset") Long datasetId) {

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    List<SnapshotVO> snapshots = null;
    try {
      snapshots = datasetSnapshotService.getSnapshotsEnabledByIdDataset(datasetId);
    } catch (EEAException e) {
      LOG.error("Error getting the list of snapshots for datasetId {}. Error Message: {}", datasetId, e.getMessage(), e);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving snapshots enabled for datasetId {} Message: {}", datasetId, e.getMessage());
      throw e;
    }
    return snapshots;
  }

  /**
   * Creates the snapshot.
   *
   * @param datasetId the dataset id
   * @param createSnapshot the create snapshot
   */
  @Override
  @LockMethod(removeWhenFinish = false)
  @HystrixCommand
  @PostMapping(value = "/dataset/{idDataset}/create", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Create snapshot", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  public void createSnapshot(
          @ApiParam(type = "Long", value = "Dataset Id", example = "0") @LockCriteria(
                  name = "datasetId") @PathVariable("idDataset") Long datasetId,
          @ApiParam(value = "create snapshot object") @LockCriteria(name = "released",
                  path = "released") @RequestBody CreateSnapshotVO createSnapshot) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
            SecurityContextHolder.getContext().getAuthentication().getName());

    try {
      LOG.info("Adding snapshot for datasetId {}", datasetId);
      // This method will release the lock
      datasetSnapshotService.addSnapshot(datasetId, createSnapshot, null, null, false, null);
      LOG.info("Successfully added snapshot for datasetId {}", datasetId);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error adding snapshot for datasetId {} Message: {}", datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/v1/{idSnapshot}/dataset/{idDataset}/delete")
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#datasetId,'DATASET_STEWARD','DATASET_LEAD_REPORTER','DATASET_CUSTODIAN','DATASET_REPORTER_WRITE','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD') OR hasAnyRole('ADMIN')")
  @ApiOperation(value = "Delete dataset snapshot by snapshot id",
          notes = "Allowed roles: \n\n Reporting dataset: STEWARD, LEAD REPORTER, CUSTODIAN, REPORTER WRITE \n\n Data collection: CUSTODIAN, STEWARD \n\n Test dataset: CUSTODIAN, STEWARD, STEWARD SUPPORT \n\n Reference dataset: CUSTODIAN, STEWARD")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully delete snapshot"),
          @ApiResponse(code = 400, message = "Dataset id incorrect or user request not found")})
  public void deleteSnapshot(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("idDataset") Long datasetId,
          @ApiParam(type = "Long", value = "Snapshot id",
                  example = "0") @PathVariable("idSnapshot") Long idSnapshot) {

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      LOG.info("Removing snapshot with id {} for datasetId {}", idSnapshot, datasetId);
      datasetSnapshotService.removeSnapshot(datasetId, idSnapshot);
      LOG.info("Successfully removed snapshot with id {} for datasetId {}", idSnapshot, datasetId);
    } catch (EEAException e) {
      LOG.error("Error deleting a snapshot with id {} for datasetId {}. Error Message: {}", idSnapshot, datasetId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DELETING_SNAPSHOT);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error deleting snapshot with id {} for datasetId {} Message: {}", idSnapshot, datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete snapshot legacy.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/{idSnapshot}/dataset/{idDataset}/delete")
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#datasetId,'DATASET_STEWARD','DATASET_LEAD_REPORTER','DATASET_CUSTODIAN','DATASET_REPORTER_WRITE','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD')")
  @ApiOperation(value = "Delete dataset snapshot by id", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully delete snapshot"),
          @ApiResponse(code = 400, message = "Dataset id incorrect or user request not found")})
  public void deleteSnapshotLegacy(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @PathVariable("idDataset") Long datasetId,
          @ApiParam(type = "Long", value = "snapshot Id",
                  example = "0") @PathVariable("idSnapshot") Long idSnapshot) {
    this.deleteSnapshot(datasetId, idSnapshot);
  }

  /**
   * Restore snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   */
  @Override
  @HystrixCommand
  @LockMethod(removeWhenFinish = false)
  @PostMapping(value = "/{idSnapshot}/dataset/{idDataset}/restore",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @ApiOperation(value = "Restore snapshot", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully restore snapshot"),
          @ApiResponse(code = 400, message = "Dataset id incorrect"),
          @ApiResponse(code = 423, message = "Locked")})
  public void restoreSnapshot(
          @ApiParam(type = "Long", value = "Dataset Id", example = "0") @LockCriteria(
                  name = "datasetId") @PathVariable("idDataset") Long datasetId,
          @ApiParam(type = "Long", value = "snapshot Id",
                  example = "0") @PathVariable("idSnapshot") Long idSnapshot) {

    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDatasetId(datasetId);
    notificationControllerZuul.createUserNotificationPrivate("RESTORE_DATASET_SNAPSHOT_INIT_INFO",
            userNotificationContentVO);

    DataSetMetabaseVO dataset = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    String processId = UUID.randomUUID().toString();
    LOG.info("Updating process for dataflowId {}, dataProviderId {}, dataset {}, processId {} to status IN_QUEUE", dataset.getDataflowId(), dataset.getDataProviderId(), dataset.getId(), processId);
    processControllerZuul.updateProcess(datasetId, dataset.getDataflowId(),
            ProcessStatusEnum.IN_QUEUE, ProcessTypeEnum.RESTORE_REPORTING_DATASET, processId,
            SecurityContextHolder.getContext().getAuthentication().getName(), defaultRestoreProcessPriority, false);
    LOG.info("Updated process for dataflowId {}, dataProviderId {}, dataset {}, processId {} to status IN_QUEUE", dataset.getDataflowId(), dataset.getDataProviderId(), dataset.getId(), processId);

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
            SecurityContextHolder.getContext().getAuthentication().getName());

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      // Check if the snapshot creation process is running and locked
      Map<String, Object> createSchemaSnapshot = new HashMap<>();
      createSchemaSnapshot.put(LiteralConstants.SIGNATURE,
              LockSignature.CREATE_SCHEMA_SNAPSHOT.getValue());
      createSchemaSnapshot.put(LiteralConstants.DATASETID, datasetId);
      LockVO importLockVO = lockService.findByCriteria(createSchemaSnapshot);
      if (importLockVO != null) {
        LOG.error("Snapshot restoration is locked because creation is in progress. DatasetId is {} and snapshotId is {}", datasetId, idSnapshot);
        LOG.info("Updating process for dataflowId {}, dataProviderId {}, dataset {}, processId {} to status CANCELED", dataset.getDataflowId(), dataset.getDataProviderId(), dataset.getId(), processId);
        processControllerZuul.updateProcess(datasetId, dataset.getDataflowId(),
                ProcessStatusEnum.CANCELED, ProcessTypeEnum.RESTORE_REPORTING_DATASET, processId,
                SecurityContextHolder.getContext().getAuthentication().getName(), defaultRestoreProcessPriority, false);
        LOG.info("Updated process for dataflowId {}, dataProviderId {}, dataset {}, processId {} to status CANCELED", dataset.getDataflowId(), dataset.getDataProviderId(), dataset.getId(), processId);

        throw new ResponseStatusException(HttpStatus.LOCKED,
                "Snapshot restoration is locked because creation is in progress.");
      } else {
        LOG.info("Restoring snapshot with id {} for datasetId {}", idSnapshot, datasetId);
        // This method will release the lock

        LOG.info("Updating process for dataflowId {}, dataProviderId {}, dataset {}, processId {} to status IN_PROGRESS", dataset.getDataflowId(), dataset.getDataProviderId(), dataset.getId(), processId);
        processControllerZuul.updateProcess(datasetId, dataset.getDataflowId(),
                ProcessStatusEnum.IN_PROGRESS, ProcessTypeEnum.RESTORE_REPORTING_DATASET, processId,
                SecurityContextHolder.getContext().getAuthentication().getName(), defaultRestoreProcessPriority, false);
        LOG.info("Updated process for dataflowId {}, dataProviderId {}, dataset {}, processId {} to status IN_PROGRESS", dataset.getDataflowId(), dataset.getDataProviderId(), dataset.getId(), processId);

        datasetSnapshotService.restoreSnapshot(datasetId, idSnapshot, true, processId);
        LOG.info("Successfully restored snapshot with id {} for datasetId {}", idSnapshot, datasetId);
      }
    } catch (EEAException e) {
      LOG.error("Error restoring a snapshot with id {} for datasetId {}. Error Message: {}", idSnapshot, datasetId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DATASET_INCORRECT_ID);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error restoring snapshot with id {} for datasetId {} Message: {}", idSnapshot, datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Release snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   * @param dateRelease the date release
   * @param processId the process id
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/private/{idSnapshot}/dataset/{idDataset}/release",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Release snapshot", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER') OR hasAnyRole('ADMIN')")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 400, message = "Dataset id incorrect or execution error")})
  public void releaseSnapshot(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @PathVariable("idDataset") Long datasetId,
          @ApiParam(type = "Long", value = "snapshot Id",
                  example = "0") @PathVariable("idSnapshot") Long idSnapshot,
          @ApiParam(type = "String",
                  value = "Date release") @RequestParam("dateRelease") String dateRelease,
          @ApiParam(type = "String",
                  value = "Process Id") @RequestParam("processId") String processId) {

    ProcessVO processVO = null;
    if (processId!=null) {
      processVO = processControllerZuul.findById(processId);
    }
    String user = processVO!=null ? processVO.getUser() : SecurityContextHolder.getContext().getAuthentication().getName();

    LOG.info("The user invoking DataSetSnapshotControllerImpl.releaseSnapshot is {} for datasetId {} and snapshotId {} of processId {}",
            user, datasetId, idSnapshot, processId);

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user", user);

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      LOG.info("Releasing snapshot with id {} for datasetId {} processId {} and dateRelease {}", idSnapshot, datasetId, processId, dateRelease);
      datasetSnapshotService.releaseSnapshot(datasetId, idSnapshot, dateRelease, processId);
      LOG.info("Successfully released snapshot with id {} for datasetId {} processId {}", idSnapshot, datasetId, processId);
    } catch (EEAException e) {
      LOG.error("Error releasing a snapshot with id {} for datasetId {} processId {}. Error Message: {}",  idSnapshot, datasetId, processId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.EXECUTION_ERROR);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error releasing snapshot with id {} for datasetId {} processId {} Message: {}", idSnapshot, datasetId, processId, e.getMessage());
      throw e;
    }
  }

  /**
   * Gets the schema snapshots by id dataset.
   *
   * @param datasetId the dataset id
   * @return the schema snapshots by id dataset
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataschema/{idDesignDataset}/listSnapshots",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get schema snapshot by dataset Id", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD')")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 400, message = "Dataset id incorrect")})
  public List<SnapshotVO> getSchemaSnapshotsByIdDataset(@ApiParam(type = "Long",
          value = "Dataset Id", example = "0") @PathVariable("idDesignDataset") Long datasetId) {

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    List<SnapshotVO> snapshots = null;
    try {
      snapshots = datasetSnapshotService.getSchemaSnapshotsByIdDataset(datasetId);
    } catch (EEAException e) {
      LOG.error("Error getting the list of schema snapshots. Error message: {}",
              e.getMessage(), e);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving schema snapshots for datasetId {} Message: {}", datasetId, e.getMessage());
      throw e;
    }
    return snapshots;
  }

  /**
   * Creates the schema snapshot.
   *
   * @param datasetId the dataset id
   * @param idDatasetSchema the id dataset schema
   * @param description the description
   */
  @Override
  @LockMethod(removeWhenFinish = false)
  @HystrixCommand
  @PostMapping(value = "/dataschema/{idDatasetSchema}/dataset/{idDesignDataset}/create",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Create schema Snapshot", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD')")
  public void createSchemaSnapshot(
          @ApiParam(type = "Long", value = "Dataset Id", example = "0") @LockCriteria(
                  name = "datasetId") @PathVariable("idDesignDataset") Long datasetId,
          @ApiParam(type = "String", value = "Dataset Schema Id",
                  example = "5cf0e9b3b793310e9ceca190") @PathVariable("idDatasetSchema") String idDatasetSchema,
          @ApiParam(type = "String", value = "Description",
                  example = "abc") @RequestParam("description") String description) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
            SecurityContextHolder.getContext().getAuthentication().getName());

    try {
      LOG.info("Adding schema snapshot for datasetId {}", datasetId);
      // This method will release the lock
      datasetSnapshotService.addSchemaSnapshot(datasetId, idDatasetSchema, description);
      LOG.info("Successfully added snapshot for datasetId {}", datasetId);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error adding schema snapshots for datasetId {} and datasetSchemaId {} Message: {}", datasetId, idDatasetSchema, e.getMessage());
      throw e;
    }
  }

  /**
   * Restore schema snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   */
  @Override
  @HystrixCommand
  @LockMethod(removeWhenFinish = false)
  @PostMapping(value = "/{idSnapshot}/dataschema/{idDesignDataset}/restore",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Restore schema snapshot", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD')")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully restore schema snapshot"),
          @ApiResponse(code = 400, message = "Dataset id incorrect")})
  public void restoreSchemaSnapshot(
          @ApiParam(type = "Long", value = "Dataset Id", example = "0") @LockCriteria(
                  name = "datasetId") @PathVariable("idDesignDataset") Long datasetId,
          @ApiParam(type = "Long", value = "snapshot Id",
                  example = "0") @PathVariable("idSnapshot") Long idSnapshot) {

    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDatasetId(datasetId);
    notificationControllerZuul.createUserNotificationPrivate("RESTORE_DATASET_SNAPSHOT_INIT_INFO",
            userNotificationContentVO);

    DataSetMetabaseVO dataset = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    String processId = UUID.randomUUID().toString();
    LOG.info("Updating process for dataflowId {}, dataset {}, processId {} to status IN_QUEUE", dataset.getDataflowId(), dataset.getId(), processId);
    processControllerZuul.updateProcess(datasetId, dataset.getDataflowId(),
            ProcessStatusEnum.IN_QUEUE, ProcessTypeEnum.RESTORE_DESIGN_DATASET, processId,
            SecurityContextHolder.getContext().getAuthentication().getName(), defaultRestoreProcessPriority, false);
    LOG.info("Updating process for dataflowId {}, dataset {}, processId {} to status IN_QUEUE", dataset.getDataflowId(), dataset.getId(), processId);

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
            SecurityContextHolder.getContext().getAuthentication().getName());

    if (datasetId == null) {
      LOG.info("Updating process for dataflowId {}, dataset {}, processId {} to status CANCELED", dataset.getDataflowId(), dataset.getId(), processId);
      processControllerZuul.updateProcess(datasetId, dataset.getDataflowId(),
              ProcessStatusEnum.CANCELED, ProcessTypeEnum.RESTORE_DESIGN_DATASET, processId,
              SecurityContextHolder.getContext().getAuthentication().getName(), defaultRestoreProcessPriority, false);
      LOG.info("Updating process for dataflowId {}, dataset {}, processId {} to status CANCELED", dataset.getDataflowId(), dataset.getId(), processId);

      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      LOG.info("Restoring snapshot with id {} for datasetId {}", idSnapshot, datasetId);

      LOG.info("Updating process for dataflowId {}, dataset {}, processId {} to status IN_PROGRESS", dataset.getDataflowId(), datasetId, processId);
      processControllerZuul.updateProcess(datasetId, dataset.getDataflowId(),
              ProcessStatusEnum.IN_PROGRESS, ProcessTypeEnum.RESTORE_DESIGN_DATASET, processId,
              SecurityContextHolder.getContext().getAuthentication().getName(), defaultRestoreProcessPriority, false);
      LOG.info("Updated process for dataflowId {}, dataset {}, processId {} to status IN_PROGRESS", dataset.getDataflowId(), datasetId, processId);

      // This method will release the lock
      datasetSnapshotService.restoreSchemaSnapshot(datasetId, idSnapshot, processId);
      LOG.info("Successfully restored snapshot with id {} for datasetId {}", idSnapshot, datasetId);
    } catch (EEAException | IOException e) {
      LOG.error("Error restoring a schema snapshot with id {} and datasetId {}. Error Message {}", idSnapshot, datasetId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DATASET_INCORRECT_ID);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error restoring schema snapshot with snapshotId {} for datasetId {} Message: {}", idSnapshot, datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete schema snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   * @throws Exception the exception
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/{idSnapshot}/dataschema/{idDesignDataset}/delete")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD')")
  @ApiOperation(value = "Delete schema snapshot", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 400, message = "Dataset id incorrect or user request not found"),
          @ApiResponse(code = 423, message = "locked")})
  public void deleteSchemaSnapshot(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @PathVariable("idDesignDataset") Long datasetId,
          @ApiParam(type = "Long", value = "snapshot Id",
                  example = "0") @PathVariable("idSnapshot") Long idSnapshot)
          throws Exception {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
            SecurityContextHolder.getContext().getAuthentication().getName());

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      Map<String, Object> createSchemaSnapshot = new HashMap<>();
      createSchemaSnapshot.put(LiteralConstants.SIGNATURE,
              LockSignature.CREATE_SCHEMA_SNAPSHOT.getValue());
      createSchemaSnapshot.put(LiteralConstants.DATASETID, datasetId);
      LockVO importLockVO = lockService.findByCriteria(createSchemaSnapshot);
      if (importLockVO != null) {
        LOG.error("Snapshot remove is locked because creation is in progress for snapshotId {} and datasetId {}", idSnapshot, datasetId );
        throw new ResponseStatusException(HttpStatus.LOCKED,
                "Snapshot remove is locked because creation is in progress.");
      } else {
        LOG.info("Removing schema snapshot with id {} for datasetId {} ", idSnapshot, datasetId);
        // This method will release the lock
        datasetSnapshotService.removeSchemaSnapshot(datasetId, idSnapshot);
        LOG.info("Removing schema snapshot with id {} for datasetId {} ", idSnapshot, datasetId);
      }
    } catch (EEAException | IOException e) {
      LOG.error("Error deleting a schema snapshot with id {} for datasetId {}. Error message: {}", idSnapshot, datasetId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DELETING_SCHEMA_SNAPSHOT);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error deleting schema snapshot with snapshotId {} for datasetId {} Message: {}", idSnapshot, datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the receipt PDF.
   *
   * @param response the response
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_LEAD_REPORTER')")
  @GetMapping(value = "/receiptPDF/dataflow/{dataflowId}/dataProvider/{dataProviderId}",
          produces = MediaType.APPLICATION_PDF_VALUE)
  @ApiOperation(value = "Create receipt PDF", hidden = true)
  public ResponseEntity<StreamingResponseBody> createReceiptPDF(HttpServletResponse response,
                                                                @ApiParam(type = "Long", value = "Dataflow Id",
                                                                        example = "0") @PathVariable("dataflowId") Long dataflowId,
                                                                @ApiParam(type = "Long", value = "Provider Id",
                                                                        example = "0") @PathVariable("dataProviderId") Long dataProviderId) {

    try {
      LOG.info("Creating receipt pdf for dataflowId {} and dataProviderId {}", dataflowId, dataProviderId);
      StreamingResponseBody stream =
              out -> datasetSnapshotService.createReceiptPDF(out, dataflowId, dataProviderId);

      response.setContentType("application/pdf");
      response.setHeader("Content-Disposition", "attachment;filename=receipt.pdf");

      return new ResponseEntity<>(stream, HttpStatus.OK);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error creating receipt pdf for dataflowId {} and dataProviderId {} Message: {}", dataflowId, dataProviderId, e.getMessage());
      throw e;
    }
  }


  /**
   * Obtain the dataset historic releases.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/v1/historicReleases", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_NATIONAL_COORDINATOR','DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT') OR checkApiKey(#dataflowId,null,#datasetId,'DATASET_STEWARD','DATASET_CUSTODIAN','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD') OR hasAnyRole('ADMIN')")
  @ApiOperation(value = "Get dataset historic releases by dataset id",
          notes = "Allowed roles: \n\n Reporting dataset: CUSTODIAN, STEWARD \n\n Data collection: CUSTODIAN, STEWARD \n\n EU dataset: CUSTODIAN, STEWARD")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 400, message = "Dataset not found")})
  public List<ReleaseVO> historicReleases(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @RequestParam("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId) {
    List<ReleaseVO> releases;
    // get dataset type
    try {
      releases = datasetSnapshotService.getReleases(datasetId);
    } catch (EEAException e) {
      LOG.error("Error retrieving releases for dataflowId {} and datasetId {}. Error message: {}", dataflowId, datasetId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DATASET_NOTFOUND);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving historic releases for dataflowId {} and datasetId {} Message: {}", dataflowId, datasetId, e.getMessage());
      throw e;
    }

    return releases;
  }

  /**
   * Update the release date of a historic release entry.
   *
   * @param idSnapshot the snapshot id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param newReleaseDate the new release date
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/v1/{snapshotId}/updateReleaseDate", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD') OR hasAnyRole('ADMIN')")
  @ApiOperation(value = "Update release date of a historic release entry", hidden = true,
      notes = "Allowed roles: \n\n Dataflow: CUSTODIAN, STEWARD, ADMIN")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully updated release date"),
      @ApiResponse(code = 400, message = "Dataset id incorrect or user request not found")})
  public void updateHistoricReleaseDate(
      @ApiParam(type = "Long", value = "Snapshot id", example = "0")
      @PathVariable("snapshotId") Long idSnapshot,
      @ApiParam(type = "Long", value = "Dataflow id", example = "0")
      @RequestParam("dataflowId") Long dataflowId,
      @ApiParam(type = "Long", value = "Provider id", example = "0")
      @RequestParam("providerId") Long providerId,
      @ApiParam(type = "String",
          value = "New Release Date") @RequestParam("newReleaseDate") String newReleaseDate) {

    if (dataflowId == null || providerId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DATAFLOW_INCORRECT_ID + " or " + EEAErrorMessage.PROVIDER_INCORRECT_ID);
    }

    try {
      LOG.info("Updating historic release date for snapshotId {} and providerId {} of dataflowId {} to {}",
          idSnapshot, providerId, dataflowId, newReleaseDate);
      datasetSnapshotService.updateHistoricReleaseDate(idSnapshot, dataflowId, providerId, newReleaseDate);
      LOG.info("Successfully updated historic release date for snapshotId {} and providerId {} of dataflowId {}",
          idSnapshot, providerId, dataflowId);
    } catch (EEAException e) {
      LOG.error("Error updating historic release date for snapshotId {} and providerId {} of dataflowId {}. Error: {}",
          idSnapshot, providerId, dataflowId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.EXECUTION_ERROR);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error updating historic release date for snapshotId {} and providerId {} of dataflowId {}. Message: {}",
          idSnapshot, providerId, dataflowId, e.getMessage());
      throw e;
    }
  }

  /**
   * Historic releases legacy.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/historicReleases", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_NATIONAL_COORDINATOR','DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT') OR checkApiKey(#dataflowId,null,#datasetId,'DATASET_STEWARD','DATASET_CUSTODIAN','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD')")
  @ApiOperation(value = "Get historic releases", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 400, message = "Dataset not found")})
  public List<ReleaseVO> historicReleasesLegacy(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @RequestParam("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow Id",
                  example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId) {
    return this.historicReleases(datasetId, dataflowId);
  }

  /**
   * Historic releases dates authorized by consul key.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/historicReleaseDatesAuthorizedByConsul", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("checkAuthorizationKeyFromConsul(#key, T(org.eea.dataset.controller.DatasetSnapshotControllerImpl).staticEeaAuthorizationKey)")
  @ApiOperation(value = "Get historic releases", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 400, message = "Dataset not found")})
  public List<Date> historicReleaseDatesAuthorizedByConsul(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @RequestParam("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow Id",
                  example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId,
          @ApiParam(type = "String", value = "key",
                  example = "HASH-ABC-123") @RequestParam("key") String key) {
    List<Date> releaseDates = new ArrayList<>();
    try {
      releaseDates = datasetSnapshotService.getReleaseDates(datasetId);
    } catch (EEAException e) {
      LOG.error("Error getting release dates for dataset id {}. Error Message: {}", datasetId, e.getMessage(), e);
    }
    return releaseDates;
  }

  /**
   * Datasets historic releases by representative for all datasets involved.
   *
   * @param dataflowId the dataflow id
   * @param representativeId the representative id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/historicReleasesRepresentative",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get historic releases by representative", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_READ','DATAFLOW_REPORTER_WRITE','DATAFLOW_NATIONAL_COORDINATOR','DATAFLOW_OBSERVER','DATAFLOW_STEWARD_SUPPORT') OR hasAnyRole('ADMIN')")
  public List<ReleaseVO> historicReleasesByRepresentative(
          @ApiParam(type = "Long", value = "Dataflow Id",
                  example = "0") @RequestParam("dataflowId") Long dataflowId,
          @ApiParam(type = "Long", value = "Representative Id",
                  example = "0") @RequestParam("representativeId") Long representativeId) {
    List<ReleaseVO> releases = new ArrayList<>();

    List<ReportingDataset> datasets = reportingDatasetRepository.findByDataflowId(dataflowId);
    List<Long> datasetIds =
            datasets.stream().filter(dataset -> dataset.getDataProviderId().equals(representativeId))
                    .map(ReportingDataset::getId).collect(Collectors.toList());
    for (Long id : datasetIds) {
      releases.addAll(datasetSnapshotService.getSnapshotsReleasedByIdDataset(id));
    }
    return releases;
  }

  /**
   * Update snapshot EU release.
   *
   * @param datasetId the dataset id
   */
  @Override
  @HystrixCommand
  @PutMapping("/private/eurelease/{idDataset}")
  @ApiOperation(value = "Update snapshot eu release", hidden = true)
  public void updateSnapshotEURelease(@ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @PathVariable("idDataset") Long datasetId) {
    try {
      LOG.info("Updating snapshot EU Release for datasetId {}", datasetId);
      datasetSnapshotService.updateSnapshotEURelease(datasetId);
      LOG.info("Successfully updated snapshot EU Release for datasetId {}", datasetId);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error updating snapshot EU release for datasetId {} Message: {}", datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Executes the release snapshots.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   */
  @SneakyThrows
  @Override
  @LockMethod(removeWhenFinish = false)
  @HystrixCommand
  @PostMapping(value = "/dataflow/{dataflowId}/dataProvider/{dataProviderId}/release",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_LEAD_REPORTER') OR hasAnyRole('ADMIN')")
  @ApiOperation(value = "Create release snapshots", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully create"),
          @ApiResponse(code = 400, message = "Execution error"),
          @ApiResponse(code = 412, message = "Dataflow not releasable")})
  public void createReleaseSnapshots(
          @ApiParam(type = "Long", value = "Dataflow Id", example = "0") @LockCriteria(
                  name = "dataflowId") @PathVariable(value = "dataflowId", required = true) Long dataflowId,
          @ApiParam(type = "Long", value = "Provider Id", example = "0") @LockCriteria(
                  name = "dataProviderId") @PathVariable(value = "dataProviderId",
                  required = true) Long dataProviderId,
          @ApiParam(type = "boolean", value = "Restric from public", example = "true") @RequestParam(
                  name = "restrictFromPublic", required = true,
                  defaultValue = "false") boolean restrictFromPublic,
          @ApiParam(type = "boolean", value = "Execute validations", example = "true") @RequestParam(
                  name = "validate", required = false, defaultValue = "true") boolean validate,
          @ApiParam(type = "Long", value = "Job id", example = "1") @RequestParam(
                  name = "jobId", required = false) Long jobId) {

    Boolean silentRelease = false;
    JobVO jobVO = null;
    if (jobId!=null) {
      jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.IN_PROGRESS);
      jobVO = jobControllerZuul.findJobById(jobId);
      if(jobVO != null) {
        Map<String, Object> parameters = jobVO.getParameters();
        if (parameters.containsKey("silentRelease")) {
          silentRelease = (Boolean) parameters.get("silentRelease");
        }
      }
    }

    String user = jobVO!=null ? jobVO.getCreatorUsername() : SecurityContextHolder.getContext().getAuthentication().getName();

    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDataflowId(dataflowId);
    userNotificationContentVO.setProviderId(dataProviderId);
    userNotificationContentVO.setUserId(user);
    if(!silentRelease) {
      notificationControllerZuul.createUserNotificationPrivate("RELEASE_START_EVENT", userNotificationContentVO);
    }
    else{
      //the following will not produce any notifications to the user because frontend will never show it in the user notifications
      notificationControllerZuul.createUserNotificationPrivate("SILENT_RELEASE_START_EVENT", userNotificationContentVO);
    }

    ThreadPropertiesManager.setVariable("user", user);
    DataFlowVO dataflow = dataflowControllerZull.getMetabaseById(dataflowId);
    Boolean isBigDataflow = dataflow != null ? dataflow.getBigData() : null;

    LOG.info("The user invoking DataSetSnaphotControllerImpl.createReleaseSnapshots for dataflowId {} and dataProviderId {} with jobId {} is {}",
            dataflowId, dataProviderId, jobId, user);

    List<ReportingDatasetVO> datasets = dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowIdAndProviderId(dataflowId, dataProviderId);
    for(ReportingDatasetVO dataset: datasets){
      List<TableSchemaIdNameVO> tables = datasetSchemaService.getTableSchemasIds(dataset.getId());
      String datasetSchemaId = dataset.getDatasetSchema();
      for(TableSchemaIdNameVO table: tables){
        TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(table.getIdTableSchema(), datasetSchemaId);
        if(tableSchemaVO != null && BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable())
                && BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(dataset.getId(), tableSchemaVO.getIdTableSchema()))) {
          if(jobId != null) {
            LOG.info("Can not release for jobId {} because an iceberg table exists", jobId);
            jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.ERROR_ICEBERG_TABLE_EXISTS, null);
            jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FAILED);
            datasetSnapshotService.releaseLocksRelatedToRelease(dataflowId, dataProviderId);
            if(!silentRelease) {
              //send failed notification
              Map<String, Object> value = new HashMap<>();
              value.put(LiteralConstants.USER, user);
              value.put("release_job_id", jobId);
              kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_FAILED_ICEBERG_EXISTS_EVENT, value,
                      NotificationVO.builder().user(user).dataflowId(dataflowId).providerId(dataset.getDataProviderId())
                              .error("There is an iceberg table for dataflowId " + dataflowId + " and providerId " + dataset.getDataProviderId()).build());
            }
            else{
              LOG.info("Sending SILENT_RELEASE_FAILED_EVENT event for jobId {}", jobId);
              //this event will not produce any notifications to the user because frontend will never show it in the user notifications
              Map<String, Object> value = new HashMap<>();
              value.put(LiteralConstants.USER, user);
              value.put("release_job_id", jobId);
              kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.SILENT_RELEASE_FAILED_EVENT, value,
                      NotificationVO.builder().user(user).dataflowId(dataflowId).providerId(dataset.getDataProviderId())
                              .error("There is an iceberg table for dataflowId " + dataflowId + " and providerId " + dataset.getDataProviderId()).build());
            }
          }
          throw new Exception("Can not release for jobId " + jobId + " because there is an iceberg table");
        }
      }
    }
    //check Locks for Citus
    if (!Boolean.TRUE.equals(isBigDataflow)) {
      List<Long> datasetIds = datasets.stream()
              .map(ReportingDatasetVO::getId)
              .collect(Collectors.toList());

      if (datasetTableService.isAnyDatasetBeingEdited(datasetIds)) {
        LOG.info("Can not release for dataflowId {} and dataProviderId {} because a dataset is locked for editing",
                dataflowId, dataProviderId);

        if (jobId != null) {
          jobControllerZuul.updateJobInfo(jobId,
                  JobInfoEnum.ERROR_DATASET_IS_LOCKED_FOR_EDITING, null);
          jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FAILED);
          datasetSnapshotService.releaseLocksRelatedToRelease(dataflowId, dataProviderId);

          if (!silentRelease) {
            Map<String, Object> value = new HashMap<>();
            value.put(LiteralConstants.USER, user);
            value.put("release_job_id", jobId);
            kafkaSenderUtils.releaseNotificableKafkaEvent(
                    EventType.RELEASE_FAILED_DATASET_LOCKED_FOR_EDITING_EXISTS_EVENT,
                    value,
                    NotificationVO.builder()
                            .user(user)
                            .dataflowId(dataflowId)
                            .providerId(dataProviderId)
                            .error("There is a dataset locked for editing for dataflowId "
                                    + dataflowId + " and providerId " + dataProviderId)
                            .build());
          }
        }

        throw new Exception("Can not release for dataflowId " + dataflowId
                + " and dataProviderId " + dataProviderId
                + " because at least one dataset is locked for editing");
      }
    }

    if (null != dataflow && dataflow.isReleasable()) {
      try {
        datasetSnapshotService.createReleaseSnapshots(dataflowId, dataProviderId,
                restrictFromPublic, validate, jobId);
        LOG.info("Successfully created release snapshots for dataflowId {} and dataProviderId {} with jobId {}", dataflowId, dataProviderId, jobId);
      } catch (EEAException e) {
        LOG.error("Error releasing a snapshot for dataflowId {} and dataProviderId {} with jobId {}. Error Message: {}", dataflowId, dataProviderId, jobId, e.getMessage(), e);
        try {
          datasetSnapshotService.releaseLocksRelatedToRelease(dataflowId, dataProviderId);
        } catch (EEAException e1) {
          LOG.error("Error releasing snapshot locks for dataflowId {} and dataProviderId {} with jobId {} . Error Message: {}", dataflowId, dataProviderId, jobId, e1.getMessage(), e1);
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                  EEAErrorMessage.EXECUTION_ERROR);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.EXECUTION_ERROR,
                e);
      }
    } else {
      try {
        datasetSnapshotService.releaseLocksRelatedToRelease(dataflowId, dataProviderId);
      } catch (EEAException e) {
        throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
                String.format(EEAErrorMessage.DATAFLOW_NOT_RELEASABLE, dataflowId));
      }
      throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
              String.format(EEAErrorMessage.DATAFLOW_NOT_RELEASABLE, dataflowId));
    }
  }


  /**
   * Release locks from release datasets.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   */
  @Override
  @PutMapping("/private/releaseLocksRelatedToReleaseDataset/dataflow/{dataflowId}/dataProvider/{dataProviderId}")
  @ApiOperation(value = "Release locks form release datasets", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully released"),
          @ApiResponse(code = 500, message = "Error releasing locks")})
  public void releaseLocksFromReleaseDatasets(
          @ApiParam(type = "Long", value = "Dataflow Id",
                  example = "0") @PathVariable("dataflowId") Long dataflowId,
          @ApiParam(type = "Long", value = "Provider Id",
                  example = "0") @PathVariable("dataProviderId") Long dataProviderId) {
    try {
      LOG.info("Releasing locks related to release for dataflowId {} and dataProviderId {}", dataflowId, dataProviderId);
      datasetSnapshotService.releaseLocksRelatedToRelease(dataflowId, dataProviderId);
      LOG.info("Successfully released locks related to release for dataflowId {} and dataProviderId {}", dataflowId, dataProviderId);
    } catch (EEAException e) {
      LOG.error(
              "Error releasing the locks in the operation release datasets for dataflowId {} and dataProviderId {}. Error Message: {}",
              dataflowId, dataProviderId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.EXECUTION_ERROR);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error releasing locks from release datasets for dataflowId {} and dataProviderId {} Message: {}", dataflowId, dataProviderId, e.getMessage());
      throw e;
    }
  }

  /**
   * Update snapshot disabled.
   *
   * @param datasetId the dataset id
   */
  @Override
  @HystrixCommand
  @PutMapping("/private/updateSnapshotDisabled/{datasetId}")
  @ApiOperation(value = "Private operation to update snapshot, disable and move the files",
          hidden = true)
  public void updateSnapshotDisabled(@PathVariable("datasetId") Long datasetId) {
    LOG.info("Updating snapshot to disabled for datasetId {}", datasetId);
    datasetSnapshotService.updateSnapshotDisabled(datasetId);
  }

  /**
   * Delete snapshot by dataset id and date released is null.
   *
   * @param datasetId the dataset id
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/private/deleteSnapshotByDatasetIdAndDateReleasedIsNull/{datasetId}")
  @ApiOperation(value = "Private operation to delete snapshot when dcRelease equals false",
          hidden = true)
  public void deleteSnapshotByDatasetIdAndDateReleasedIsNull(
          @PathVariable("datasetId") Long datasetId) {
    LOG.info("Calling deleteSnapshotByDatasetIdAndDateReleasedIsNull for datasetId {}", datasetId);
    datasetSnapshotService.deleteSnapshotByDatasetIdAndDateReleasedIsNull(datasetId);
  }

  /**
   * Removes historic release
   * @param datasetId
   */
  @Override
  @HystrixCommand
  @ApiOperation(value = "Private operation to remove historic release",
          hidden = true)
  @PutMapping(value = "/private/removeHistoricRelease/{datasetId}")
  public void removeHistoricRelease(@PathVariable("datasetId") Long datasetId) {
    datasetSnapshotService.removeHistoricRelease(datasetId);
  }

  /**
   * Obtain the latest historic release snapshot.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/latestReleaseSnapshot", produces = MediaType.APPLICATION_JSON_VALUE)
  public SnapshotVO getLatestHistoricReleaseSnapshot(@RequestParam("datasetId") Long datasetId, @RequestParam(value = "dataflowId", required = false) Long dataflowId) {
    SnapshotVO snapshotVO;
    try {
      snapshotVO = datasetSnapshotService.getLatestReleaseSnapshot(datasetId);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving latest historic release snapshot for dataflowId {} and datasetId {} Message: {}", dataflowId, datasetId, e.getMessage());
      throw e;
    }
    return snapshotVO;
  }

  @SneakyThrows
  @Override
  @GetMapping(value = "/private/getSnapshotByDatasetId")
  public List<SnapshotVO> getSnapshotByDatasetId(Long datasetId){
    return datasetSnapshotService.getSnapshotsByIdDataset(datasetId);
  }

  /**
   * Export the historic releases for a data collection.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @return the download url
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_NATIONAL_COORDINATOR','DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT') OR checkApiKey(#dataflowId,null,#datasetId,'DATASET_STEWARD','DATASET_CUSTODIAN','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD')")
  @PostMapping(value = "/exportHistoricReleases/{datasetId}")
  public String exportHistoricReleasesCSV(@PathVariable("datasetId") Long datasetId, @RequestParam("dataflowId") Long dataflowId) throws Exception {
    LOG.info("Export historic releases from dataflowId {} and datasetId {}, with type .csv", dataflowId, datasetId);
    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDatasetId(datasetId);
    notificationControllerZuul.createUserNotificationPrivate("DOWNLOAD_HISTORIC_RELEASES_START",
            userNotificationContentVO);
    String downloadUrl = "";
    try {
      String processUUID = UUID.randomUUID().toString();
      // Sets the file name and it's root directory
      String folderName = "dataset-" + datasetId + "-HistoricReleases";
      String composedFileName = folderName + "-"
              + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss"));
      String fileNameWithExtension = composedFileName + "." + FileTypeEnum.CSV.getValue();
      datasetSnapshotService.exportHistoricReleasesCSV(datasetId, dataflowId, folderName, fileNameWithExtension, processUUID);
      downloadUrl = "/snapshot/downloadHistoricReleases/" + datasetId + "?dataflowId=" + dataflowId + "&fileName=" + fileNameWithExtension + "&processId=" + processUUID;
    } catch (EEAException | IOException e) {
      LOG.error("Error exporting historic releases from dataflowId {} and datasetId {}.  Message: {}", dataflowId, datasetId,
              e.getMessage());
    } catch (Exception e) {
      LOG.error("Unexpected error! Error historic releases from dataflowId {} and datasetId {}.  Message: {}", dataflowId, datasetId,
              e.getMessage());
      throw e;
    }
    return downloadUrl;
  }

  /**
   * Download the historic releases for a data collection.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflowId id
   * @param fileName the file name
   * @param processId the processId
   * @param response the response
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_NATIONAL_COORDINATOR','DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT') OR checkApiKey(#dataflowId,null,#datasetId,'DATASET_STEWARD','DATASET_CUSTODIAN','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD')")
  @GetMapping("/downloadHistoricReleases/{datasetId}")
  public void downloadHistoricReleasesCSV(@PathVariable Long datasetId, @RequestParam("dataflowId") Long dataflowId, @RequestParam("fileName") String fileName,
                            @RequestParam(value = "processId", required = false) String processId, HttpServletResponse response){

    try {
      LOG.info("Downloading file generated when exporting historic releases for dataflowId {} and  datasetId {} Filename {} processId {}",
              dataflowId, datasetId, fileName, processId);

      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
      OutputStream out = response.getOutputStream();

      if(StringUtils.isNotBlank(processId)) {
        ProcessVO processVO = processControllerZuul.findById(processId);
        if (processVO.getStatus().equals(ProcessStatusEnum.IN_PROGRESS.toString()) || processVO.getStatus().equals(ProcessStatusEnum.CANCELED.toString())){
          String responseMessage;
          if (processVO.getStatus().equals(ProcessStatusEnum.IN_PROGRESS.toString())){
            responseMessage = "Export historic releases file for dataset with ID " + datasetId + " is not ready. Please try again later.";
          }
          else{
            responseMessage = "Something went wrong when exporting historic releases for dataset with ID " + datasetId;
          }
          out.write(responseMessage.getBytes(StandardCharsets.UTF_8));
          out.close();
          return;
        }
      }

      File file = datasetSnapshotService.downloadHistoricReleasesCSV(datasetId, fileName);

      FileInputStream in = new FileInputStream(file);
      // copy from in to out
      IOUtils.copyLarge(in, out);
      out.close();
      in.close();
      // delete the file after downloading it
      FileUtils.forceDelete(file);
    } catch (IOException | ResponseStatusException e) {
      LOG.error("Error downloading historic releases to csv for dataflowId {} datasetId {} processId {} Message: {}", dataflowId, datasetId, processId, e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(
              "Trying to download a file generated during the export of historic releases process but the file is not found, datasetID: %s + filename: %s.",
              datasetId, fileName));
    } catch (Exception e) {
      LOG.error("Unexpected error! Error downloading historic releases to csv for dataflowId {} datasetId {} processId {} Message: {}", dataflowId, datasetId, processId, e.getMessage());
      throw e;
    }
  }

  @Override
  @PutMapping("/private/rollBackSnapshotRecord/{jobId}")
  public void rollBackSnapshotRecord(@PathVariable Long jobId, @RequestParam("dataflowId") Long dataflowId, @RequestParam("providerId") Long providerId) {
    resolveSnapshotTable.rollBackSnapshotTableValues(jobId, dataflowId, providerId);
  }

}
