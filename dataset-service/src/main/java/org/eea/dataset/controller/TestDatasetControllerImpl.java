package org.eea.dataset.controller;

import java.util.List;
import org.eea.dataset.service.TestDatasetService;
import org.eea.interfaces.controller.dataset.TestDatasetController;
import org.eea.interfaces.vo.dataset.TestDatasetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

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
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TestDatasetControllerImpl.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");



  /**
   * Find Test dataset by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<TestDatasetVO> findTestDatasetByDataflowId(@PathVariable("id") Long idDataflow) {

    return testDatasetService.getTestDatasetByDataflowId(idDataflow);
  }


}
