package org.eea.interfaces.controller.dataset;

import java.util.List;
import org.eea.interfaces.vo.pams.SinglePaMVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * The Interface PaMsController.
 */
public interface PamController {

  /**
   * The Interface DataCollectionControllerZuul.
   */
  @FeignClient(value = "dataset", contextId = "pams", path = "/pams")
  interface PaMsControllerZuul extends PamController {

  }

  /**
   * Update data collection.
   *
   * @param datasetId the dataset id
   * @param paMsId the pa ms id
   * @return the singles pa ms
   */
  @GetMapping("/{datasetId}/getSinglesPaMs/{paMsId}")
  List<SinglePaMVO> getSinglesPaMs(@PathVariable("datasetId") Long datasetId,
      @PathVariable("paMsId") String paMsId);

}
