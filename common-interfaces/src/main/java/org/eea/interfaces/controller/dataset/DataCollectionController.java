package org.eea.interfaces.controller.dataset;

import java.util.List;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/** The Interface DataCollectionController. */
public interface DataCollectionController {

  /** The Interface DataCollectionControllerZuul. */
  @FeignClient(value = "dataset", contextId = "datacollection", path = "/datacollection")
  interface DataCollectionControllerZuul extends DataCollectionController {

  }

  /**
   * Undo data collection creation.
   *
   * @param datasetIds the dataset ids
   * @param dataflowId the dataflow id
   * @param isCreation the is creation
   */
  @PutMapping("/private/rollback/dataflow/{dataflowId}")
  void undoDataCollectionCreation(@RequestParam("datasetIds") List<Long> datasetIds,
      @PathVariable("dataflowId") Long dataflowId, @RequestParam("isCreation") boolean isCreation);

  /**
   * Creates the empty data collection.
   *
   * @param stopAndNotifySQLErrors the stop and notify SQL errors
   * @param manualCheck the manual check
   * @param showPublicInfo the show public info
   * @param dataCollectionVO the data collection VO
   * @param stopAndNotifyPKError the stop and notify PK error
   */
  @PostMapping("/create")
  void createEmptyDataCollection(
      @RequestParam(defaultValue = "true",
          name = "stopAndNotifySQLErrors") boolean stopAndNotifySQLErrors,
      @RequestParam(value = "manualCheck", required = false) boolean manualCheck,
      @RequestParam(value = "showPublicInfo", defaultValue = "true") boolean showPublicInfo,
      @RequestBody DataCollectionVO dataCollectionVO, @RequestParam(defaultValue = "true",
          name = "stopAndNotifyPKError") boolean stopAndNotifyPKError);


  /**
   * Find data collection id by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataCollectionVO> findDataCollectionIdByDataflowId(@PathVariable("id") Long idDataflow);

  /**
   * Update data collection.
   *
   * @param dataflowId the dataflow id
   */
  @PutMapping("/update/{dataflowId}")
  void updateDataCollection(@PathVariable("dataflowId") Long dataflowId);

}
