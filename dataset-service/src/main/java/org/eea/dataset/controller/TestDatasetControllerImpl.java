package org.eea.dataset.controller;

import java.util.List;
import org.eea.dataset.service.TestDatasetService;
import org.eea.interfaces.controller.dataset.TestDatasetController;
import org.eea.interfaces.vo.dataset.TestDatasetVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * The Class TestDatasetControllerImpl.
 */
@RestController
@RequestMapping("/testDataset")
public class TestDatasetControllerImpl implements TestDatasetController {


  /** The Test dataset service. */
  @Autowired
  private TestDatasetService testDatasetService;

  /**
   * Find Test dataset by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find test dataset by dataflow Id", hidden = true)
  public List<TestDatasetVO> findTestDatasetByDataflowId(@ApiParam(type = "Long",
      value = "Dataflow Id", example = "0") @PathVariable("id") Long idDataflow) {
    return testDatasetService.getTestDatasetByDataflowId(idDataflow);
  }

}
