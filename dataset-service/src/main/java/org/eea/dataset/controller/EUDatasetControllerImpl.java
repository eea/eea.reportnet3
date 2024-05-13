package org.eea.dataset.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.eea.dataset.service.EUDatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.controller.dataset.EUDatasetController;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The Class EUDatasetControllerImpl.
 */
@RestController
@RequestMapping("/euDataset")
@Api(tags = "EU datasets : EU Dataset Manager")
public class EUDatasetControllerImpl implements EUDatasetController {


  /** The eu dataset service. */
  @Autowired
  private EUDatasetService euDatasetService;

  /** The notification controller zuul. */
  @Autowired
  private NotificationControllerZuul notificationControllerZuul;

  /** The job controller zuul. */
  @Autowired
  private JobControllerZuul jobControllerZuul;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(EUDatasetControllerImpl.class);

  /**
   * Find EU dataset by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Populate data from Datacollection", hidden = true)
  public List<EUDatasetVO> findEUDatasetByDataflowId(@ApiParam(type = "Long", value = "Dataflow Id",
      example = "0") @PathVariable("id") Long idDataflow) {

    return euDatasetService.getEUDatasetByDataflowId(idDataflow);
  }

  /**
   * Populate data from data collection.
   *
   * @param dataflowId the dataflow id
   */
  @Override
  @HystrixCommand
  @PostMapping("/v1/populateData/dataflow/{dataflowId}")
  @LockMethod(removeWhenFinish = false)
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN')  OR (checkApiKey(#dataflowId,null,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN')) OR hasAnyRole('ADMIN')")
  @ApiOperation(value = "Copy data collections data to EU datasets by dataflow id",
      notes = "Allowed roles: CUSTODIAN, STEWARD")
  public void populateDataFromDataCollection(
      @ApiParam(type = "Long", value = "Dataflow id", example = "0") @LockCriteria(
          name = "dataflowId") @PathVariable("dataflowId") Long dataflowId, @RequestParam(name = "jobId", required = false) Long jobId) {

    JobVO jobVO = null;
    if (jobId!=null) {
      jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.IN_PROGRESS);
      jobVO = jobControllerZuul.findJobById(jobId);
    }
    String user = jobVO!=null ? jobVO.getCreatorUsername() : SecurityContextHolder.getContext().getAuthentication().getName();
    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDataflowId(dataflowId);
    userNotificationContentVO.setUserId(user);
    notificationControllerZuul.createUserNotificationPrivate("COPY_TO_EU_DATASET_INIT", userNotificationContentVO);

    try {
      // Set the user name on the thread
      ThreadPropertiesManager.setVariable("user", user);
      LOG.info("Populating data for dataflowId {}", dataflowId);
      euDatasetService.populateEUDatasetWithDataCollection(dataflowId, jobId);
    } catch (EEAException e) {
      LOG.error("Error populating the EU Dataset for dataflowId {} because: {}", dataflowId, e.getMessage());
    } catch (Exception e) {
      LOG.error("Unexpected error! Error populating data from data collection for dataflowId {} Message: {}", dataflowId, e.getMessage());
      throw e;
    }
  }

  /**
   * Populate data from data collection legacy.
   *
   * @param dataflowId the dataflow id
   */
  @Override
  @HystrixCommand
  @PostMapping("/populateData/dataflow/{dataflowId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN')  OR (checkApiKey(#dataflowId,null,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN'))")
  @ApiOperation(value = "Populate data from Datacollection", hidden = true,
      notes = "Allowed roles: CUSTODIAN, STEWARD")
  public void populateDataFromDataCollectionLegacy(
      @ApiParam(type = "Long", value = "Dataflow Id", example = "0") @LockCriteria(
          name = "dataflowId") @PathVariable("dataflowId") Long dataflowId) {
    this.populateDataFromDataCollection(dataflowId, null);
  }

  /**
   * Remove locks related to populate eu
   * @param dataflowId
   */
  @Override
  @HystrixCommand
  @PutMapping("/private/removeLocksRelatedToPopulateEU/{dataflowId}")
  @ApiOperation(value = "Remove locks related to populate eu", hidden = true)
  public void removeLocksRelatedToPopulateEU(@PathVariable("dataflowId") Long dataflowId) {
    euDatasetService.removeLocksRelatedToPopulateEU(dataflowId);
  }
}









