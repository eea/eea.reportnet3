package org.eea.interfaces.controller.dataset;

import java.util.List;
import org.eea.interfaces.vo.pams.SinglePaMsVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * The Interface PaMsController.
 */
public interface PamsController {

  /**
   * The Interface DataCollectionControllerZuul.
   */
  @FeignClient(value = "dataset", contextId = "pams", path = "/pams")
  interface PaMsControllerZuul extends PamsController {

  }

  /**
   * Update data collection.
   *
   * @param datasetId the dataset id
   * @param paMsId the pa ms id
   * @return the singles pa ms
   */
  @GetMapping("/{datasetId}/getSinglePaMs/{paMsId}")
  List<SinglePaMsVO> getSinglesPaMs(@PathVariable("datasetId") Long datasetId,
      @PathVariable("paMsId") String paMsId);

}
