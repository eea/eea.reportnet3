package org.eea.interfaces.controller.validation;

import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
   *
   * @return the data set VO
   */
  @RequestMapping(value = "/dataset/{id}", method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE)
  void validateDataSetData(@PathVariable("id") Long datasetId);

  /**
   * Gets the failed validations by id dataset.
   *
   * @param datasetId the dataset id
   * @param pageNum the page num
   * @param pageSize the page size
   * @param fields the fields
   * @param asc the asc
   * @return the failed validations by id dataset
   */
  @GetMapping(value = "/listValidations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  FailedValidationsDatasetVO getFailedValidationsByIdDataset(@PathVariable("id") Long datasetId,
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
      @RequestParam(value = "fields", required = false) String fields,
      @RequestParam(value = "asc", defaultValue = "true") Boolean asc);
}
