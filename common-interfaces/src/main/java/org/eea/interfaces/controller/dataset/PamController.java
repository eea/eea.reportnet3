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
  @FeignClient(value = "dataset", contextId = "pam", path = "/pam")
  interface PaMControllerZuul extends PamController {

  }

  /**
   * Gets the list single paM.
   *
   * @param datasetId the dataset id
   * @param groupPaMId the group paM id
   * @return the list single paM
   */
  @GetMapping("/{datasetId}/getListSinglePaM/{groupPaMId}")
  List<SinglePaMVO> getListSinglePaM(@PathVariable("datasetId") Long datasetId,
      @PathVariable("groupPaMId") String groupPaMId);

}
