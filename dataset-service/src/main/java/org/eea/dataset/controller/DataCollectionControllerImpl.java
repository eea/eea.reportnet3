package org.eea.dataset.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.dataset.service.DataCollectionService;
import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.controller.dataset.DataCollectionController;
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

/**
 * The Class DataCollectionControllerImpl.
 */
@RestController
@RequestMapping("/datacollection")
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

  /**
   * Undo data collection creation.
   *
   * @param datasetIds the dataset ids
   * @param dataflowId the dataflow id
   * @param isCreation the is creation
   */
  @Override
  @PutMapping("/private/rollback/dataflow/{dataflowId}")
  public void undoDataCollectionCreation(@RequestParam("datasetIds") List<Long> datasetIds,
      @PathVariable("dataflowId") Long dataflowId, boolean isCreation) {

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    // This method will release the lock
    dataCollectionService.undoDataCollectionCreation(datasetIds, dataflowId, isCreation);
  }

  /**
   * Creates the empty data collection.
   *
   * @param stopAndNotifySQLErrors the stop and notify SQL errors
   * @param manualCheck the manual check
   * @param showPublicInfo the show public info
   * @param dataCollectionVO the dataflow collection vo
   */
  @Override
  @HystrixCommand
  @PostMapping("/create")
  @LockMethod(removeWhenFinish = false)
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN', 'DATAFLOW_STEWARD')")
  public void createEmptyDataCollection(
      @RequestParam(defaultValue = "true",
          name = "stopAndNotifySQLErrors") boolean stopAndNotifySQLErrors,
      @RequestParam(value = "manualCheck", required = false) boolean manualCheck,
      @RequestParam(value = "showPublicInfo", defaultValue = "true") boolean showPublicInfo,
      @RequestBody @LockCriteria(name = "dataflowId",
          path = "idDataflow") DataCollectionVO dataCollectionVO) {

    Date date = dataCollectionVO.getDueDate();
    Long dataflowId = dataCollectionVO.getIdDataflow();
    TypeStatusEnum status = dataCollectionService.getDataflowStatus(dataflowId);

    // Continue if the dataflow exists and is DESIGN
    if (date == null || dataflowId == null || !TypeStatusEnum.DESIGN.equals(status)) {
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
    dataCollectionService.createEmptyDataCollection(dataflowId, date, stopAndNotifySQLErrors,
        manualCheck, showPublicInfo);
    LOG.info("DataCollection creation for Dataflow {} started", dataflowId);
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
  public void updateDataCollection(
      @PathVariable("dataflowId") @LockCriteria(name = "dataflowId") Long dataflowId) {

    TypeStatusEnum status = dataCollectionService.getDataflowStatus(dataflowId);

    if (!TypeStatusEnum.DRAFT.equals(status)) {

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
    dataCollectionService.updateDataCollection(dataflowId);
    LOG.info("DataCollection update for Dataflow {} started", dataflowId);
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
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DataCollectionVO> findDataCollectionIdByDataflowId(Long idDataflow) {
    return dataCollectionService.getDataCollectionIdByDataflowId(idDataflow);
  }
}
