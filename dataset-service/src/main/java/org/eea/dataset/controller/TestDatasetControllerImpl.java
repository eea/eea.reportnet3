package org.eea.dataset.controller;

import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.service.TestDatasetService;
import org.eea.interfaces.controller.dataset.TestDatasetController;
import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataset.TestDatasetVO;
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

  /**
   * Find test datasets summary list.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @GetMapping(value = "/private/testDatasetsSummary/dataflow/{id}")
  public List<DatasetsSummaryVO> findTestDatasetsSummaryList(@PathVariable("id") Long dataflowId) {
    List<DatasetsSummaryVO> testDatasetsSummaryList = new ArrayList<>();
    if (null != dataflowId) {
      testDatasetsSummaryList = testDatasetService.findTestDatasetsSummaryList(dataflowId);
    }
    return testDatasetsSummaryList;
  }



}
