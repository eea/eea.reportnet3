package org.eea.interfaces.controller.dataset;

import java.util.List;
import org.eea.interfaces.vo.dataset.schemas.WebformVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * The Interface WebformController.
 */
public interface WebformController {


  /**
   * The Interface WebformControllerZuul.
   */
  @FeignClient(value = "dataset", contextId = "webform", path = "/webform")
  interface WebformControllerZuul extends WebformController {

  }


  /**
   * Gets the webforms list by dataset id.
   *
   * @param datasetId the dataset id
   * @return the webforms list
   */
  @GetMapping("/{datasetId}/webforms")
  List<WebformVO> getListWebformsByDatasetId(@PathVariable("datasetId") Long datasetId);

}
