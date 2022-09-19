package org.eea.dataset.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.dataset.service.DataCollectionService;
import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.controller.dataset.DataCollectionController;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.lock.service.LockService;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

/**
 * The Class DataCollectionControllerImpl.
 */
@RestController
@RequestMapping("/datacollection")
@Api(tags = "Dataset: Data Collection Manager")
@ApiIgnore
public class DataCollectionControllerImpl implements DataCollectionController {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DataCollectionControllerImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The data collection service. */
  @Autowired
  private DataCollectionService dataCollectionService;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The notification controller zuul. */
  @Autowired
  private NotificationControllerZuul notificationControllerZuul;

  /**
   * Undo data collection creation.
   *
   * @param datasetIds the dataset ids
   * @param dataflowId the dataflow id
   * @param isCreation the is creation
   */
  @Override
  @PutMapping("/private/rollback/dataflow/{dataflowId}")
  @ApiOperation(value = "Rollback DataCollection creation", hidden = true)
  public void undoDataCollectionCreation(
      @ApiParam(value = "List of dataset Ids",
          example = "1,2,3") @RequestParam("datasetIds") List<Long> datasetIds,
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(value = "check if method is creation", example = "true") boolean isCreation) {

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    // This method will release the lock
    LOG.info("Undoing data collection creation for dataflowId {}", dataflowId);
    dataCollectionService.undoDataCollectionCreation(datasetIds, dataflowId, isCreation);
    LOG.info("Successfully undid data collection creation for dataflowId {}", dataflowId);
  }

  /**
   * Creates the empty data collection.
   *
   * @param stopAndNotifySQLErrors the stop and notify SQL errors
   * @param manualCheck enable the manual check for the custodian approval
   * @param showPublicInfo the show public info
   * @param dataCollectionVO the dataflow collection vo
   * @param stopAndNotifyPKError the stop and notify PK error
   */
  @Override
  @HystrixCommand
  @PostMapping("/create")
  @LockMethod(removeWhenFinish = false)
  @PreAuthorize("secondLevelAuthorize(#dataCollectionVO.idDataflow,'DATAFLOW_CUSTODIAN', 'DATAFLOW_STEWARD')")
  @ApiOperation(value = "Create a Data Collection", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully create data collection"),
      @ApiResponse(code = 400, message = EEAErrorMessage.NOT_DESIGN_DATAFLOW)})
  public void createEmptyDataCollection(@ApiParam(
      value = "Stop And Notify SQL Errors: If an error is found in the SQL rules, it stops the creation process.",
      example = "true") @RequestParam(defaultValue = "true",
          name = "stopAndNotifySQLErrors") boolean stopAndNotifySQLErrors,
      @ApiParam(value = "Manual Check: Enable the manual check for the custodian approval.",
          example = "false") @RequestParam(value = "manualCheck",
              required = false) boolean manualCheck,
      @ApiParam(
          value = "Show Public Info: If the schema has been marked as public, and this option is checked, the Dataflow will appear as public.",
          example = "true") @RequestParam(value = "showPublicInfo",
              defaultValue = "true") boolean showPublicInfo,
      @ApiParam(value = "Dataflow Id", example = "0") @RequestBody @LockCriteria(
          name = "dataflowId", path = "idDataflow") DataCollectionVO dataCollectionVO,
      @ApiParam(
          value = "Stop And Notify PK Errors: If all tables in all schemas have PKs the process works.",
          example = "true") @RequestParam(defaultValue = "true",
              name = "stopAndNotifyPKError") boolean stopAndNotifyPKError) {

    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDataflowId(dataCollectionVO.getIdDataflow());
    userNotificationContentVO.setDatasetName(dataCollectionVO.getDataSetName());

    LocalDateTime date = (dataCollectionVO.getDueDate() != null) ? LocalDateTime.ofInstant(
        Instant.ofEpochMilli(dataCollectionVO.getDueDate().getTime()), ZoneOffset.UTC) : null;

    Long dataflowId = dataCollectionVO.getIdDataflow();
    // new check: dataflow is Reference dataset?
    DataFlowVO dataflow = dataCollectionService.getDataflowMetabase(dataflowId);
    boolean referenceDataflow = false;
    if (null != dataflow && TypeDataflowEnum.REFERENCE.equals(dataflow.getType())) {
      referenceDataflow = true;
      showPublicInfo = false;
      manualCheck = false;
      stopAndNotifySQLErrors = false;

      notificationControllerZuul.createUserNotificationPrivate("CREATE_REFERENCE_DATASETS_INIT",
          userNotificationContentVO);
    } else {
      notificationControllerZuul.createUserNotificationPrivate("CREATE_DATA_COLLECTION_INIT",
          userNotificationContentVO);
    }

    // Continue if the dataflow exists and is DESIGN
    if (dataflow == null || (date == null && !referenceDataflow) || dataflowId == null
        || !TypeStatusEnum.DESIGN.equals(dataflow.getStatus())) {
      Map<String, Object> createDataCollection = new HashMap<>();
      createDataCollection.put(LiteralConstants.SIGNATURE,
          LockSignature.CREATE_DATA_COLLECTION.getValue());
      createDataCollection.put(LiteralConstants.DATAFLOWID, dataflowId);
      lockService.removeLockByCriteria(createDataCollection);
      LOG_ERROR.error("Error creating DataCollection: Dataflow {} is not DESIGN", dataflowId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.NOT_DESIGN_DATAFLOW);
    }

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    // This method will release the lock
    LOG.info("Creating empty data collection for dataflowId {}", dataflowId);
    dataCollectionService.createEmptyDataCollection(dataflowId, date, stopAndNotifySQLErrors,
        manualCheck, showPublicInfo, referenceDataflow, stopAndNotifyPKError);
    LOG.info("Successfully created empty data collection for dataflowId {}", dataflowId);
  }

  /**
   * Update data collection.
   *
   * @param dataflowId the dataflow id
   */
  @Override
  @HystrixCommand
  @PutMapping("/update/{dataflowId}")
  @LockMethod(removeWhenFinish = false)
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD')")
  @ApiOperation(value = "Update a Data Collection", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully update data collection"),
      @ApiResponse(code = 400, message = EEAErrorMessage.NOT_DRAFT_DATAFLOW)})
  public void updateDataCollection(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") @LockCriteria(
          name = "dataflowId") Long dataflowId) {

    // new check: dataflow is Reference dataset?
    DataFlowVO dataflow = dataCollectionService.getDataflowMetabase(dataflowId);
    boolean referenceDataflow = false;
    if (null != dataflow && TypeDataflowEnum.REFERENCE.equals(dataflow.getType())) {
      referenceDataflow = true;
    }

    if (dataflow == null || !TypeStatusEnum.DRAFT.equals(dataflow.getStatus())) {

      Map<String, Object> updateDataCollection = new HashMap<>();
      updateDataCollection.put(LiteralConstants.SIGNATURE,
          LockSignature.UPDATE_DATA_COLLECTION.getValue());
      updateDataCollection.put(LiteralConstants.DATAFLOWID, dataflowId);
      lockService.removeLockByCriteria(updateDataCollection);
      LOG_ERROR.error("Error updating DataCollection: Dataflow {} is not DRAFT", dataflowId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.NOT_DRAFT_DATAFLOW);
    }

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    // This method will release the lock
    LOG.info("Updating data collection for dataflowId {}", dataflowId);
    dataCollectionService.updateDataCollection(dataflowId, referenceDataflow);
    LOG.info("Successfully updated data collection for dataflowId {}", dataflowId);
  }

  /**
   * Find data collection id by dataflow id.
   *
   * @param idDataflow the id dataflow
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find a Data Collection by Dataflow id",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DataCollectionVO.class,
      responseContainer = "List", hidden = true)
  public List<DataCollectionVO> findDataCollectionIdByDataflowId(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("id") Long idDataflow) {
    return dataCollectionService.getDataCollectionIdByDataflowId(idDataflow);
  }

  /**
   * Find providers pending in eu dataset.
   *
   * @param id the id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/pendingProviders/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find Providers pending to copy into EU Dataset",
      produces = MediaType.APPLICATION_JSON_VALUE, response = String.class,
      responseContainer = "List", hidden = true)
  public List<String> findProvidersPendingInEuDataset(
      @ApiParam(value = "Datacollection Id", example = "0") @PathVariable("id") Long id) {
    return dataCollectionService.getProvidersPendingToCopyIntoEU(id);
  }

}
