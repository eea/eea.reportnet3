package org.eea.dataflow.controller;

import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.service.ContributorService;
import org.eea.interfaces.controller.dataflow.ContributorController;
import org.eea.interfaces.vo.contributor.ContributorVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The Class ContributorControllerImpl.
 */
@RestController
@RequestMapping("/contributor")
public class ContributorControllerImpl implements ContributorController {


  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ContributorControllerImpl.class);


  /** The access right service. */
  @Autowired
  private ContributorService contributorService;

  /**
   * Delete editor.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   */
  @DeleteMapping(value = "/editor/dataflow/{dataflowId}")
  @Override
  public void deleteEditor(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody ContributorVO contributorVO) {
    // LOG.info("Didn't remove role of the user with account {} because its role is {}",
    // contributorVO.getAccount(), contributorVO.getRole());
  }


  /**
   * Delete reporter.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param contributorVO the contributor VO
   */
  @DeleteMapping(value = "/reporter/dataflow/{dataflowId}/provider/{dataProviderId}")
  @Override
  public void deleteReporter(@PathVariable("dataflowId") Long dataflowId,
      @PathVariable("dataProviderId") Long dataProviderId,
      @RequestBody ContributorVO contributorVO) {
    // LOG.info("Didn't remove role of the user with account {} because its role is {}",
    // contributorVO.getAccount(), contributorVO.getRole());
  }

  @GetMapping(value = "/editor/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Override
  public List<ContributorVO> findEditorsByGroup(@PathVariable("dataflowId") Long dataflowId) {
    ContributorVO contributorVO = new ContributorVO();
    contributorVO.setAccount("email@emali.com");
    contributorVO.setDataProviderId(null);
    contributorVO.setWritePermission(true);
    contributorVO.setRole("EDITOR");
    ContributorVO contributorVO2 = new ContributorVO();
    contributorVO2.setAccount("email2@emali.com");
    contributorVO2.setDataProviderId(null);
    contributorVO2.setWritePermission(true);
    contributorVO2.setRole("EDITOR");
    List<ContributorVO> contributorVOs = new ArrayList<>();
    contributorVOs.add(contributorVO);
    contributorVOs.add(contributorVO2);
    return contributorVOs;
  }

  /**
   * Find reporters by group.
   *
   * @param dataflowId the dataflow id
   * @param dataproviderId the dataprovider id
   * @return the list
   */
  @GetMapping(value = "/reporter/dataflow/{dataflowId}/provider/{dataproviderId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Override
  public List<ContributorVO> findReportersByGroup(@PathVariable("dataflowId") Long dataflowId,
      @PathVariable("providerId") Long dataproviderId) {
    ContributorVO contributorVO = new ContributorVO();
    contributorVO.setAccount("email@emali.com");
    contributorVO.setDataProviderId(1L);
    contributorVO.setWritePermission(true);
    contributorVO.setRole("REPORTER");
    ContributorVO contributorVO2 = new ContributorVO();
    contributorVO2.setAccount("email2@emali.com");
    contributorVO2.setDataProviderId(2L);
    contributorVO2.setWritePermission(true);
    contributorVO2.setRole("REPORTER");
    List<ContributorVO> contributorVOs = new ArrayList<>();
    contributorVOs.add(contributorVO);
    contributorVOs.add(contributorVO2);
    return contributorVOs;
  }

  /**
   * Update editor.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/editor/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity updateEditor(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody ContributorVO contributorVO) {
    return new ResponseEntity(HttpStatus.OK);
  }

  /**
   * Update reporter.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param contributorVO the contributor VO
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/reporter/dataflow/{dataflowId}/provider/{dataProviderId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity updateReporter(@PathVariable("dataflowId") Long dataflowId,
      @PathVariable("dataProviderId") Long dataProviderId,
      @RequestBody ContributorVO contributorVO) {
    return new ResponseEntity(HttpStatus.OK);
  }

}
