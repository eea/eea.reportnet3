package org.eea.dataflow.controller;

import java.util.List;
import org.eea.dataflow.service.ContributorService;
import org.eea.exception.EEAException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
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


  /** The contributor service. */
  @Autowired
  private ContributorService contributorService;

  /**
   * Delete.
   *
   * @param dataflowId the dataflow id
   * @param account the account
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN', 'DATAFLOW_LEAD_REPORTER')")
  @DeleteMapping(value = "/dataflow/{dataflowId}/user")
  public void delete(@PathVariable("dataflowId") Long dataflowId, @RequestParam String account) {
    // we can only remove role of editor, reporter or reporter partition type
    try {
      contributorService.deleteContributor(dataflowId, account);
    } catch (EEAException e) {
      LOG_ERROR.error("Error deleting the contributor {}.in the dataflow: {}", account, dataflowId);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }


  /**
   * Find contributors by group.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN', 'DATAFLOW_LEAD_REPORTER')")
  @GetMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<ContributorVO> findContributorsByGroup(@PathVariable("dataflowId") Long dataflowId) {
    // we can find editors, reporters or reporter partition roles based on the dataflow state
    // mock
    return contributorService.findContributorsByIdDataflow(dataflowId);
  }

  /**
   * Update role user.
   *
   * @param dataflowId the dataflow id
   * @param roleUserVO the role user VO
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN', 'DATAFLOW_LEAD_REPORTER')")
  @PutMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity update(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody ContributorVO contributorVO) {
    // we can only update an editor, reporter or reporter partition role
    // mock
    String message = "";
    HttpStatus status = HttpStatus.OK;
    try {
      contributorService.updateContributor(contributorVO, dataflowId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error update the contributor {}.in the dataflow: {}",
          contributorVO.getAccount(), dataflowId);
      message = e.getMessage();
      status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    return new ResponseEntity<>(message, status);
  }


  /**
   * Creates the contributor.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @return the long
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN', 'DATAFLOW_LEAD_REPORTER')")
  @PostMapping("/dataflow/{dataflowId}")
  public void createContributor(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody ContributorVO contributorVO) {
    switch (contributorVO.getRole()) {
      case "EDITOR":
      case "REPORTER":
        try {
          contributorService.createContributor(contributorVO, dataflowId);
        } catch (EEAException e) {
          LOG_ERROR.error("Error creating  the contributor {}.in the dataflow: {} ",
              contributorVO.getAccount(), dataflowId);
          throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        break;
      default:
        LOG.info("Didn't remove role of the user with account {} because its role is {}",
            contributorVO.getAccount(), contributorVO.getRole());
        break;
    }
  }
}
