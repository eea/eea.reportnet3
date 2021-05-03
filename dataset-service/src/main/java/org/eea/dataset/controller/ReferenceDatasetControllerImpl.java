package org.eea.dataset.controller;

import java.util.List;
import org.eea.dataset.service.ReferenceDatasetService;
import org.eea.interfaces.controller.dataset.ReferenceDatasetController;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


/**
 * The Class ReferenceDatasetControllerImpl.
 */
@RestController
@RequestMapping("/referenceDataset")
public class ReferenceDatasetControllerImpl implements ReferenceDatasetController {


  /** The reference dataset service. */
  @Autowired
  private ReferenceDatasetService referenceDatasetService;

  /**
   * Find Test dataset by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<ReferenceDatasetVO> findReferenceDatasetByDataflowId(
      @PathVariable("id") Long dataflowId) {

    return referenceDatasetService.getReferenceDatasetByDataflowId(dataflowId);
  }


}
