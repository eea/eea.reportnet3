package org.eea.interfaces.controller.dataset;

import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * The Interface EUDatasetController.
 */
public interface EUDatasetController {


  /**
   * The Interface EUDatasetControllerZuul.
   */
  @FeignClient(value = "dataset", contextId = "euDataset", path = "/euDataset")
  interface EUDatasetControllerZuul extends EUDatasetController {

  }


  /**
   * Find EU dataset by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @GetMapping(value = "/private/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<EUDatasetVO> findEUDatasetByDataflowId(@PathVariable("id") Long idDataflow);

  /**
   * Populate data from data collection.
   *
   * @param idDataflow the id dataflow
   */
  @PostMapping("/v1/populateData/dataflow/{dataflowId}")
  void populateDataFromDataCollection(@PathVariable("dataflowId") Long idDataflow, @RequestParam(name = "jobId", required = false) Long jobId);

  /**
   * Populate data from data collection legacy.
   *
   * @param idDataflow the id dataflow
   */
  @PostMapping("/populateData/dataflow/{dataflowId}")
  void populateDataFromDataCollectionLegacy(@PathVariable("dataflowId") Long idDataflow);

  /**
   * Remove locks related to populate eu
   * @param dataflowId
   */
  @PutMapping("/private/removeLocksRelatedToPopulateEU/{dataflowId}")
  void removeLocksRelatedToPopulateEU(@PathVariable("dataflowId") Long dataflowId);
}








