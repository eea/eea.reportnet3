package org.eea.interfaces.controller.dataset;

import java.util.List;
import org.eea.interfaces.vo.dataset.TestDatasetVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * The Interface TestDatasetController.
 */
public interface TestDatasetController {


  /**
   * The Interface TestDatasetControllerZuul.
   */
  @FeignClient(value = "dataset", contextId = "testDataset", path = "/testDataset")
  interface TestDatasetControllerZuul extends TestDatasetController {

  }


  /**
   * Find Test dataset by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @GetMapping(value = "/private/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<TestDatasetVO> findTestDatasetByDataflowId(@PathVariable("id") Long idDataflow);

}
