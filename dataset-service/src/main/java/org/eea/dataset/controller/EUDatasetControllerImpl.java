package org.eea.dataset.controller;

import java.util.List;
import org.eea.dataset.service.EUDatasetService;
import org.eea.interfaces.controller.dataset.EUDatasetController;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The Class EUDatasetControllerImpl.
 */
@RestController
@RequestMapping("/euDataset")
public class EUDatasetControllerImpl implements EUDatasetController {


  /** The eu dataset service. */
  @Autowired
  private EUDatasetService euDatasetService;


  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(EUDatasetControllerImpl.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");



  /**
   * Find EU dataset by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<EUDatasetVO> findEUDatasetByDataflowId(Long idDataflow) {

    return euDatasetService.getEUDatasetByDataflowId(idDataflow);
  }
}
