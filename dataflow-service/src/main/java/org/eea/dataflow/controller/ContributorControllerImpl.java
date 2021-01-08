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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * The Class ContributorControllerImpl.
 */
@RestController
@Api(tags = "Contributors : Contributors Manager")
@RequestMapping("/contributor")
public class ContributorControllerImpl implements ContributorController {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ContributorControllerImpl.class);

  /**
   * The Constant THE_EMAIL: {@value}.
   */
  private static final String THE_EMAIL_NOT_EXISTS = "The email %s doesn't exist in repornet";

  /**
   * The Constant EDITOR: {@value}.
   */
  private static final String EDITOR = "EDITOR";

  /**
   * The Constant REPORTER: {@value}.
   */
  private static final String REPORTER = "REPORTER";

  /**
   * The contributor service.
   */
  @Autowired
  private ContributorService contributorService;

  /**
   * The user management controller zull.
   */
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
  @ApiOperation(value = "Delete one Editor in a Dataflow")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully deleted editor"),
      @ApiResponse(code = 204, message = "Successfully deleted editor"),
      @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
      @ApiResponse(code = 404, message = "The email doesn't exist in Repornet"),
      @ApiResponse(code = 500, message = "Internal Server Error")})
  @ApiParam()
  public void deleteEditor(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(type = "Object",
          value = "Contributors Properties") @RequestBody ContributorVO contributorVO) {
    // we can only remove role of editor, reporter or reporter partition type
    try {
      checkAccount(dataflowId, contributorVO.getAccount());
      contributorService.deleteContributor(dataflowId, contributorVO.getAccount(), EDITOR, null);
      LOG.info("Editor {} Deleted", contributorVO.getAccount());
    } catch (EEAException e) {
      if (HttpStatus.NOT_FOUND.toString().equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format(THE_EMAIL_NOT_EXISTS, contributorVO.getAccount()));
      }
      LOG_ERROR.error("Error deleting the editor {}.in the dataflow: {}",
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
  @ApiOperation(value = "Delete one Reporter in a Dataflow")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully deleted reporter"),
      @ApiResponse(code = 204, message = "Successfully deleted reporter"),
      @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
      @ApiResponse(code = 404, message = "The email doesn't exist in Repornet"),
      @ApiResponse(code = 500, message = "Internal Server Error")})
  public void deleteReporter(
      @ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(type = "Long", value = "Dataprovider Id",
          example = "0") @PathVariable("dataProviderId") Long dataProviderId,
      @ApiParam(type = "Object",
          value = "Contributors Properties") @RequestBody ContributorVO contributorVO) {
    // we can only remove role of editor, reporter or reporter partition type
    try {
      checkAccount(dataflowId, contributorVO.getAccount());
      contributorService.deleteContributor(dataflowId, contributorVO.getAccount(), REPORTER,
          dataProviderId);
      LOG.info("Reporter {} Deleted", contributorVO.getAccount());
    } catch (EEAException e) {
      if (HttpStatus.NOT_FOUND.toString().equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format(THE_EMAIL_NOT_EXISTS, contributorVO.getAccount()));
      }
      LOG_ERROR.error("Error deleting the reporter {}.in the dataflow: {}",
          contributorVO.getAccount(), dataflowId);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Find editors by group.
   *
   * @param dataflowId the dataflow id
   *
   * @return the list
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN')")
  @GetMapping(value = "/editor/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find all Editors in a Dataflow",
      produces = MediaType.APPLICATION_JSON_VALUE, response = ContributorVO.class,
      responseContainer = "List")
  public List<ContributorVO> findEditorsByGroup(@ApiParam(type = "Long", value = "Dataflow Id",
      example = "0") @PathVariable("dataflowId") Long dataflowId) {
    // we can find editors,
    return contributorService.findContributorsByResourceId(dataflowId, null, EDITOR);
  }

  /**
   * Find reporters by group.
   *
   * @param dataflowId the dataflow id
   * @param dataproviderId the dataprovider id
   *
   * @return the list
   */
  @Override
  @GetMapping(value = "/reporter/dataflow/{dataflowId}/provider/{dataproviderId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN', 'DATAFLOW_LEAD_REPORTER')")
  @ApiOperation(value = "Find all Reporters in a Dataflow",
      produces = MediaType.APPLICATION_JSON_VALUE, response = ContributorVO.class,
      responseContainer = "List")
  public List<ContributorVO> findReportersByGroup(
      @ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(type = "Long", value = "Dataprovider Id",
          example = "0") @PathVariable("dataproviderId") Long dataproviderId) {
    // find reporters or reporter partition roles based on the dataflow state
    return contributorService.findContributorsByResourceId(dataflowId, dataproviderId, REPORTER);
  }

  /**
   * Update editor.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   *
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN')")
  @PutMapping(value = "/editor/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Update one Editor in a Dataflow",
      produces = MediaType.APPLICATION_JSON_VALUE, response = ResponseEntity.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully update editor"),
      @ApiResponse(code = 204, message = "Successfully updated editor"),
      @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
      @ApiResponse(code = 404, message = "The email doesn't exist in Repornet"),
      @ApiResponse(code = 500, message = "Internal Server Error")})
  public ResponseEntity updateEditor(
      @ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(type = "Object",
          value = "Contributors Properties") @RequestBody ContributorVO contributorVO) {
    // we can only update an editor, reporter or reporter partition role
    // mock
    String message = "";
    HttpStatus status = HttpStatus.OK;
    try {
      checkAccount(dataflowId, contributorVO.getAccount());
      contributorService.updateContributor(dataflowId, contributorVO, EDITOR, null);
      LOG.info("Editor {} Updated", contributorVO.getAccount());
    } catch (EEAException e) {
      if (HttpStatus.NOT_FOUND.toString().equals(e.getMessage())) {
        message = String.format(THE_EMAIL_NOT_EXISTS, contributorVO.getAccount());
        status = HttpStatus.NOT_FOUND;
      } else {
        message = e.getMessage();
        status = HttpStatus.INTERNAL_SERVER_ERROR;
      }
      LOG_ERROR.error("Error update the editor {}.in the dataflow: {}", contributorVO.getAccount(),
          dataflowId);

    }
    return new ResponseEntity<>(message, status);
  }

  /**
   * Update reporter.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param contributorVO the contributor VO
   *
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN', 'DATAFLOW_LEAD_REPORTER')")
  @PutMapping(value = "/reporter/dataflow/{dataflowId}/provider/{dataProviderId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Update one Reporter in a Dataflow",
      produces = MediaType.APPLICATION_JSON_VALUE, response = ResponseEntity.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully update reporter"),
      @ApiResponse(code = 204, message = "Successfully updated reporter"),
      @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
      @ApiResponse(code = 404, message = "The email doesn't exist in Repornet"),
      @ApiResponse(code = 500, message = "Internal Server Error")})
  public ResponseEntity updateReporter(
      @ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(type = "Long", value = "Dataprovider Id",
          example = "0") @PathVariable("dataProviderId") Long dataProviderId,
      @ApiParam(type = "Object",
          value = "Contributors Properties") @RequestBody ContributorVO contributorVO) {
    // we can only update an editor, reporter or reporter partition role
    String message = "";
    HttpStatus status = HttpStatus.OK;
    try {
      checkAccount(dataflowId, contributorVO.getAccount());
      contributorService.updateContributor(dataflowId, contributorVO, REPORTER, dataProviderId);
      LOG.info("Reporter {} Updated", contributorVO.getAccount());
    } catch (EEAException e) {
      if (HttpStatus.NOT_FOUND.toString().equals(e.getMessage())) {
        message = String.format(THE_EMAIL_NOT_EXISTS, contributorVO.getAccount());
        status = HttpStatus.NOT_FOUND;
      } else {
        message = e.getMessage();
        status = HttpStatus.INTERNAL_SERVER_ERROR;
      }
      LOG_ERROR.error("Error update the reporter {}.in the dataflow: {}",
          contributorVO.getAccount(), dataflowId);
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
  @ApiOperation(value = "Create permissions for all Datasetschemas in a Dataflow", hidden = true)
  @ApiResponses(
      value = {@ApiResponse(code = 200, message = "Successfully updated reporter"), @ApiResponse(
          code = 500, message = "Error creating  the associated permissions for editor role")})
  public void createAssociatedPermissions(
      @ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @PathVariable("datasetId") Long datasetId) {

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
   *
   * @throws EEAException
   */
  private void checkAccount(Long dataflowId, String account) throws EEAException {
    // check if the email is correct
    UserRepresentationVO emailUser = userManagementControllerZull.getUserByEmail(account);
    if (null == emailUser || null == emailUser.getEmail()
        || !emailUser.getEmail().equalsIgnoreCase(account)) {
      LOG_ERROR.error(
          "Error creating contributor with the account: {} in the dataflow {} because the email doesn't exist in the system",
          account, dataflowId);
      throw new EEAException(HttpStatus.NOT_FOUND.toString());
    }
  }

}
