package org.eea.interfaces.controller.validation;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Interface ValidationController.
 */
public interface ValidationController {

  /**
   * The Interface ValidationControllerZuul.
   */
  @FeignClient(value = "validation", path = "/validation")
  interface ValidationControllerZuul extends ValidationController {

  }

  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   * @return the data set VO
   */
  @RequestMapping(value = "/dataset/{id}", method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE)
  void validateDataSetData(@RequestParam("id") Long datasetId);

}
