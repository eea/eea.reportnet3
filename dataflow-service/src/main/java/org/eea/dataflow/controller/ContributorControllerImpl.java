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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
   * Delete resource.
   *
   * @param contributorVO the role user VO
   * @param dataflowId the dataflow id
   */
  @DeleteMapping(value = "/dataflow/{dataflowId}")
  @Override
  public void delete(@RequestBody ContributorVO contributorVO,
      @PathVariable("dataflowId") Long dataflowId) {
    // we can only remove role of editor, reporter or reporter partition type
    switch (contributorVO.getRole()) {
      case "EDITOR":
      case "REPORTER_PARTITIONED":
      case "REPORTER":
        contributorService.deleteContributor(contributorVO, dataflowId);
        break;
      default:
        LOG.info("Didn't remove role of the user with account {} because its role is {}",
            contributorVO.getAccount(), contributorVO.getRole());
        break;
    }
  }

  /**
   * Find role users by group.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @GetMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Override
  public List<ContributorVO> findContributorsByGroup(@PathVariable("dataflowId") Long dataflowId) {
    // we can find editors, reporters or reporter partition roles based on the dataflow state
    // mock
    ContributorVO contributorVO = new ContributorVO();
    contributorVO.setAccount("email@emali.com");
    contributorVO.setDataProviderId(1L);
    contributorVO.setWritePermission(true);
    contributorVO.setRole("EDITOR");
    List<ContributorVO> contributorVOs = new ArrayList<>();
    contributorVOs.add(contributorVO);
    contributorVOs.add(contributorVO);
    return contributorVOs;
  }

  /**
   * Update role user.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the role user VO
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity update(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody ContributorVO contributorVO) {
    // we can only update an editor, reporter or reporter partition role
    // mock
    return new ResponseEntity(HttpStatus.OK);
  }

  /**
   * Creates the representative.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the role user VO
   * @return the long
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN')")
  @PostMapping("/dataflow/{dataflowId}")
  public Long createContributor(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody ContributorVO contributorVO) {

    switch (contributorVO.getRole()) {
      case "EDITOR":
      case "REPORTER_PARTITIONED":
      case "REPORTER":
        contributorService.createContributor(contributorVO, dataflowId);
        break;
      default:
        LOG.info("Didn't remove role of the user with account {} because its role is {}",
            contributorVO.getAccount(), contributorVO.getRole());
        break;
    }
    return 1L;
  }
}
