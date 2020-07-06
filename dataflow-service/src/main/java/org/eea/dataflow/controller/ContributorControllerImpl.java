package org.eea.dataflow.controller;

import java.util.List;
import org.eea.dataflow.service.ContributorService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.ContributorController;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.contributor.ContributorVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
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

  /** The Constant EDITOR: {@value}. */
  private static final String EDITOR = "EDITOR";

  /** The Constant REPORTER: {@value}. */
  private static final String REPORTER = "REPORTER";

  /** The contributor service. */
  @Autowired
  private ContributorService contributorService;

  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /**
   * Delete editor.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN')")
  @DeleteMapping(value = "/editor/dataflow/{dataflowId}")
  public void deleteEditor(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody ContributorVO contributorVO) {
    // we can only remove role of editor, reporter or reporter partition type
    try {
      checkAccount(dataflowId, contributorVO.getAccount());
      contributorService.deleteContributor(dataflowId, contributorVO.getAccount(), EDITOR, null);
    } catch (EEAException e) {
      LOG_ERROR.error("Error deleting the contributor {}.in the dataflow: {}",
          contributorVO.getAccount(), dataflowId);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Delete reporter.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param contributorVO the contributor VO
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN', 'DATAFLOW_LEAD_REPORTER')")
  @DeleteMapping(value = "/reporter/dataflow/{dataflowId}/provider/{dataProviderId}")
  public void deleteReporter(@PathVariable("dataflowId") Long dataflowId,
      @PathVariable("dataProviderId") Long dataProviderId,
      @RequestBody ContributorVO contributorVO) {
    // we can only remove role of editor, reporter or reporter partition type
    try {
      checkAccount(dataflowId, contributorVO.getAccount());
      contributorService.deleteContributor(dataflowId, contributorVO.getAccount(), REPORTER,
          dataProviderId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error deleting the contributor {}.in the dataflow: {}",
          contributorVO.getAccount(), dataflowId);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Find editors by group.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN')")
  @GetMapping(value = "/editor/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<ContributorVO> findEditorsByGroup(@PathVariable("dataflowId") Long dataflowId) {
    // we can find editors,
    return contributorService.findContributorsByResourceId(dataflowId, null, EDITOR);
  }

  /**
   * Find reporters by group.
   *
   * @param dataflowId the dataflow id
   * @param dataproviderId the dataprovider id
   * @return the list
   */
  @Override
  @GetMapping(value = "/reporter/dataflow/{dataflowId}/provider/{dataproviderId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN', 'DATAFLOW_LEAD_REPORTER')")
  public List<ContributorVO> findReportersByGroup(@PathVariable("dataflowId") Long dataflowId,
      @PathVariable("dataproviderId") Long dataproviderId) {
    // find reporters or reporter partition roles based on the dataflow state
    return contributorService.findContributorsByResourceId(dataflowId, dataproviderId, REPORTER);
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
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN')")
  @PutMapping(value = "/editor/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> updateEditor(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody ContributorVO contributorVO) {
    // we can only update an editor, reporter or reporter partition role
    // mock
    String message = "";
    HttpStatus status = HttpStatus.OK;
    if (!EDITOR.equals(contributorVO.getRole())) {
      LOG_ERROR.error(
          "Error creating contributor with the account: {} in the dataflow {}  because the role not avaliable {}",
          contributorVO.getAccount(), dataflowId, contributorVO.getRole());
      message = new StringBuilder("Role ").append(contributorVO.getRole()).append(" doesn't exist")
          .toString();
      status = HttpStatus.BAD_REQUEST;

    } else {
      try {
        checkAccount(dataflowId, contributorVO.getAccount());
        contributorService.updateContributor(dataflowId, contributorVO, EDITOR, null);
      } catch (EEAException e) {
        LOG_ERROR.error("Error update the contributor {}.in the dataflow: {}",
            contributorVO.getAccount(), dataflowId);
        message = e.getMessage();
        status = HttpStatus.INTERNAL_SERVER_ERROR;
      }
    }
    return new ResponseEntity<>(message, status);
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
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN', 'DATAFLOW_LEAD_REPORTER')")
  @PutMapping(value = "/reporter/dataflow/{dataflowId}/provider/{dataProviderId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> updateReporter(@PathVariable("dataflowId") Long dataflowId,
      @PathVariable("dataProviderId") Long dataProviderId,
      @RequestBody ContributorVO contributorVO) {
    // we can only update an editor, reporter or reporter partition role
    String message = "";
    HttpStatus status = HttpStatus.OK;
    if (!REPORTER.equals(contributorVO.getRole())) {
      LOG_ERROR.error(
          "Error creating contributor with the account: {} in the dataflow {}  because the role not avaliable {}",
          contributorVO.getAccount(), dataflowId, contributorVO.getRole());
      message = new StringBuilder("Role ").append(contributorVO.getRole()).append(" doesn't exist")
          .toString();
      status = HttpStatus.BAD_REQUEST;

    } else {
      try {
        checkAccount(dataflowId, contributorVO.getAccount());
        contributorService.updateContributor(dataflowId, contributorVO, REPORTER, dataProviderId);
      } catch (EEAException e) {
        LOG_ERROR.error("Error update the contributor {}.in the dataflow: {}",
            contributorVO.getAccount(), dataflowId);
        message = e.getMessage();
        status = HttpStatus.INTERNAL_SERVER_ERROR;
      }
    }
    return new ResponseEntity<>(message, status);
  }

  /**
   * Creates the associated permissions.
   *
   * @param dataflowId the dataflow id
   * @param datasetId the dataset id
   */
  @Override
  @PostMapping("/private/dataflow/{dataflowId}/createAssociatedPermissions/{datasetId}")
  public void createAssociatedPermissions(@PathVariable("dataflowId") Long dataflowId,
      @PathVariable("datasetId") Long datasetId) {

    try {
      contributorService.createAssociatedPermissions(dataflowId, datasetId);
    } catch (EEAException e) {
      LOG_ERROR.error(
          "Error creating  the associated permissions for editor role in datasetschema {}.in the dataflow: {} ",
          datasetId, dataflowId);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Check account.
   *
   * @param dataflowId the dataflow id
   * @param account the account
   */
  private void checkAccount(Long dataflowId, String account) {
    // check if the email is correct
    UserRepresentationVO emailUser = userManagementControllerZull.getUserByEmail(account);
    if (null == emailUser || null == emailUser.getEmail()) {
      LOG_ERROR.error(
          "Error creating contributor with the account: {} in the dataflow {} because the email doesn't exist in the system",
          account, dataflowId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, new StringBuilder("The email ")
          .append(account).append(" doesn't exist in repornet").toString());
    }
  }

}
