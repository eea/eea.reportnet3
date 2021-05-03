package org.eea.interfaces.controller.dataset;

import java.util.List;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



/**
 * The Interface ReferenceDatasetController.
 */
public interface ReferenceDatasetController {


  /**
   * The Interface TestDatasetControllerZuul.
   */
  @FeignClient(value = "dataset", contextId = "referenceDataset", path = "/referenceDataset")
  interface ReferenceDatasetControllerZuul extends ReferenceDatasetController {

  }



  /**
   * Find reference dataset by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<ReferenceDatasetVO> findReferenceDatasetByDataflowId(@PathVariable("id") Long dataflowId);

}
