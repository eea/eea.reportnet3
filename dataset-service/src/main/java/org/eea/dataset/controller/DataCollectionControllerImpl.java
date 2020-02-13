package org.eea.dataset.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eea.dataset.service.DataCollectionService;
import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.controller.dataset.DataCollectionController;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.lock.service.LockService;
import org.eea.thread.ThreadPropertiesManager;
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

  /** The data collection service. */
  @Autowired
  private DataCollectionService dataCollectionService;

  @Autowired
  private LockService lockService;

  private static final Logger LOG = LoggerFactory.getLogger(DataCollectionControllerImpl.class);

  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  @Override
  @PutMapping("/rollback/dataflow/{dataflowId}")
  public void undoDataCollectionCreation(@RequestParam("datasetIds") List<Long> datasetIds,
      @PathVariable("dataflowId") Long dataflowId) {

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    // This method will release the lock
    dataCollectionService.undoDataCollectionCreation(datasetIds, dataflowId);
  }

  @Override
  @HystrixCommand
  @PostMapping("/create/dataflow/{dataflowId}/{dueDate}")
  @LockMethod(removeWhenFinish = false)
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public void createEmptyDataCollection(
      @PathVariable("dataflowId") @LockCriteria(name = "dataflowId") Long dataflowId,
      @PathVariable("dueDate") Long dueDate) {

    // Check if the date is after actual date
    Date date = new Date(dueDate);
    if (new Date(System.currentTimeMillis()).compareTo(date) > 0) {
      List<Object> criteria = new ArrayList<>();
      criteria.add(LockSignature.CREATE_DATA_COLLECTION.getValue());
      criteria.add(dataflowId);
      lockService.removeLockByCriteria(criteria);
      LOG_ERROR.error("Error creating DataCollection: Invalid date");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATE_AFTER_INCORRECT);
    }

    // Continue if the dataflow exists and is DESIGN
    if (!dataCollectionService.isDesignDataflow(dataflowId)) {
      List<Object> criteria = new ArrayList<>();
      criteria.add(LockSignature.CREATE_DATA_COLLECTION.getValue());
      criteria.add(dataflowId);
      lockService.removeLockByCriteria(criteria);
      LOG_ERROR.error("Error creating DataCollection: Dataflow {} is not DESIGN", dataflowId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.NOT_DESIGN_DATAFLOW);
    }

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    // This method will release the lock
    dataCollectionService.createEmptyDataCollection(dataflowId, date);
    LOG.info("DataCollection creation for Dataflow {} started", dataflowId);
  }

  /**
   * Find data collection id by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DataCollectionVO> findDataCollectionIdByDataflowId(Long idDataflow) {
    return dataCollectionService.getDataCollectionIdByDataflowId(idDataflow);
  }
}
