package org.eea.dataset.controller;

import java.util.List;
import org.eea.dataset.service.EUDatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.EUDatasetController;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * The Class EUDatasetControllerImpl.
 */
@RestController
@RequestMapping("/euDataset")
public class EUDatasetControllerImpl implements EUDatasetController {


  /** The eu dataset service. */
  @Autowired
  private EUDatasetService euDatasetService;


  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(EUDatasetControllerImpl.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");



  /**
   * Find EU dataset by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
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
  @PostMapping("/populateData/dataflow/{dataflowId}")
  @LockMethod(removeWhenFinish = false)
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN')  OR (checkApiKey(#dataflowId,null,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN'))")
  @ApiOperation(value = "Populate data from Datacollection")
  public void populateDataFromDataCollection(
      @ApiParam(type = "Long", value = "Dataflow Id", example = "0") @LockCriteria(
          name = "dataflowId") @PathVariable("dataflowId") Long dataflowId) {
    try {
      // Set the user name on the thread
      ThreadPropertiesManager.setVariable("user",
          SecurityContextHolder.getContext().getAuthentication().getName());
      LOG.info("Populating data in dataflow {}", dataflowId);
      euDatasetService.populateEUDatasetWithDataCollection(dataflowId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error populating the EU Dataset because: {}", e.getMessage());
    }
  }

}
