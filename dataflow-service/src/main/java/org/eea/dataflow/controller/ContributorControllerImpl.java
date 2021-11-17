package org.eea.dataflow.controller;

import java.util.List;
import org.eea.dataflow.service.ContributorService;
import org.eea.dataflow.service.impl.DataflowServiceImpl;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.ContributorController;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.contributor.ContributorVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.enums.EntityClassEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
import springfox.documentation.annotations.ApiIgnore;

/**
 * The Class ContributorControllerImpl.
 */
@RestController
@ApiIgnore
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
   * The contributor service.
   */
  @Autowired
  private ContributorService contributorService;

  /**
   * The user management controller zull.
   */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /** The dataflow service. */
  @Autowired
  private DataflowServiceImpl dataflowService;

  /**
   * Delete requester.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN') || hasRole('ADMIN')")
  @DeleteMapping(value = "/requester/dataflow/{dataflowId}")
  @ApiOperation(value = "Delete one requester in a Dataflow", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully deleted requester"),
      @ApiResponse(code = 204, message = "Successfully deleted requester"),
      @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
      @ApiResponse(code = 404, message = "The email doesn't exist in Repornet"),
      @ApiResponse(code = 500, message = "Internal Server Error")})
  public void deleteRequester(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(type = "Object",
          value = "Contributors Properties") @RequestBody ContributorVO contributorVO) {
    // check permission not allowed for custodian in bdr dataflow
    checkIsBussinesCustodian(dataflowId);
    // we can only remove role of editor, reporter or reporter partition type
    try {
      if (checkAccount(dataflowId, contributorVO.getAccount())) {
        contributorService.deleteContributor(dataflowId, contributorVO.getAccount(),
            contributorVO.getRole(), null);
        LOG.info("requester {} Deleted", contributorVO.getAccount());
      } else
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format(THE_EMAIL_NOT_EXISTS, contributorVO.getAccount()));
    } catch (EEAException e) {
      if (HttpStatus.NOT_FOUND.toString().equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format(THE_EMAIL_NOT_EXISTS, contributorVO.getAccount()));
      }
      LOG_ERROR.error("Error deleting the requester {}.in the dataflow: {}",
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
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN', 'DATAFLOW_LEAD_REPORTER')")
  @DeleteMapping(value = "/reporter/dataflow/{dataflowId}/provider/{dataProviderId}")
  @ApiOperation(value = "Delete one Reporter in a Dataflow", hidden = true)
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
      if (checkAccount(dataflowId, contributorVO.getAccount())) {
        contributorService.deleteContributor(dataflowId, contributorVO.getAccount(),
            contributorVO.getRole(), dataProviderId);
        LOG.info("Reporter {} Deleted", contributorVO.getAccount());
      } else if (contributorService.findTempUserByAccountAndDataflow(contributorVO.getAccount(),
          dataflowId) != null) {
        contributorService.deleteTemporaryUser(dataflowId, contributorVO.getAccount(),
            contributorVO.getRole(), dataProviderId);
        LOG.info("Temporary Reporter {} Deleted", contributorVO.getAccount());
      } else
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format(THE_EMAIL_NOT_EXISTS, contributorVO.getAccount()));
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
   * Find requesters by group.
   *
   * @param dataflowId the dataflow id
   *
   * @return the list
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN') || hasRole('ADMIN')")
  @GetMapping(value = "/requester/dataflow/{dataflowId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find all Requesters in a Dataflow",
      produces = MediaType.APPLICATION_JSON_VALUE, response = ContributorVO.class,
      responseContainer = "List", hidden = true)
  public List<ContributorVO> findRequestersByGroup(@ApiParam(type = "Long", value = "Dataflow Id",
      example = "0") @PathVariable("dataflowId") Long dataflowId) {
    // we can find requesters,
    return contributorService.findContributorsByResourceId(dataflowId, null,
        LiteralConstants.REQUESTER);
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
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN', 'DATAFLOW_LEAD_REPORTER')")
  @ApiOperation(value = "Find all Reporters in a Dataflow",
      produces = MediaType.APPLICATION_JSON_VALUE, response = ContributorVO.class,
      responseContainer = "List", hidden = true)
  public List<ContributorVO> findReportersByGroup(
      @ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(type = "Long", value = "Dataprovider Id",
          example = "0") @PathVariable("dataproviderId") Long dataproviderId) {
    // find reporters or reporter partition roles based on the dataflow state
    List<ContributorVO> tempReporterWrite = contributorService
        .findTempUserByRoleAndDataflow(SecurityRoleEnum.REPORTER_WRITE.toString(), dataflowId);
    List<ContributorVO> tempReporterRead = contributorService
        .findTempUserByRoleAndDataflow(SecurityRoleEnum.REPORTER_READ.toString(), dataflowId);
    List<ContributorVO> reportersList = contributorService.findContributorsByResourceId(dataflowId,
        dataproviderId, LiteralConstants.REPORTER);
    reportersList.addAll(tempReporterWrite);
    reportersList.addAll(tempReporterRead);

    return reportersList;
  }

  /**
   * Update requester.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   *
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN') || hasRole('ADMIN')")
  @PutMapping(value = "/requester/dataflow/{dataflowId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Update one Requester in a Dataflow",
      produces = MediaType.APPLICATION_JSON_VALUE, response = ResponseEntity.class, hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully update requester"),
      @ApiResponse(code = 204, message = "Successfully updated requester"),
      @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
      @ApiResponse(code = 404, message = "The email doesn't exist in Repornet"),
      @ApiResponse(code = 500, message = "Internal Server Error")})
  public ResponseEntity updateRequester(
      @ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(type = "Object",
          value = "Contributors Properties") @RequestBody ContributorVO contributorVO) {
    // check permission not allowed for custodian in bdr dataflow
    checkIsBussinesCustodian(dataflowId);
    // we can only update an editor, reporter or reporter partition role
    // mock
    String message = "";
    HttpStatus status = HttpStatus.OK;
    try {
      if (checkAccount(dataflowId, contributorVO.getAccount())) {
        contributorService.updateContributor(dataflowId, contributorVO, null);
        LOG.info("requester {} Updated", contributorVO.getAccount());
      } else
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format(THE_EMAIL_NOT_EXISTS, contributorVO.getAccount()));
    } catch (EEAException e) {
      if (HttpStatus.NOT_FOUND.toString().equals(e.getMessage())) {
        message = String.format(THE_EMAIL_NOT_EXISTS, contributorVO.getAccount());
        status = HttpStatus.NOT_FOUND;
      } else {
        message = e.getMessage();
        status = HttpStatus.INTERNAL_SERVER_ERROR;
      }
      LOG_ERROR.error("Error update the requester {}.in the dataflow: {}",
          contributorVO.getAccount(), dataflowId);

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
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN', 'DATAFLOW_LEAD_REPORTER')")
  @PutMapping(value = "/reporter/dataflow/{dataflowId}/provider/{dataProviderId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Update one Reporter in a Dataflow",
      produces = MediaType.APPLICATION_JSON_VALUE, response = ResponseEntity.class, hidden = true)
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
      if (checkAccount(dataflowId, contributorVO.getAccount())) {
        contributorService.updateContributor(dataflowId, contributorVO, dataProviderId);
        LOG.info("Reporter {} Updated", contributorVO.getAccount());
      } else if (contributorService.findTempUserByAccountAndDataflow(contributorVO.getAccount(),
          dataflowId) != null) {
        contributorService.updateTemporaryUser(dataflowId, contributorVO, dataProviderId);
      } else if (contributorService.findTempUserByAccountAndDataflow(contributorVO.getAccount(),
          dataflowId) == null) {
        contributorService.createTempUser(dataflowId, contributorVO, dataProviderId);
        LOG.info("Inserting user {} into temp user table", contributorVO.getAccount());
      } else {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "There's already a user assigned with the same e-mail to this dataflow");
      }
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
          code = 500, message = "Error creating  the associated permissions for requester role")})
  public void createAssociatedPermissions(
      @ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @PathVariable("datasetId") Long datasetId) {

    try {
      contributorService.createAssociatedPermissions(dataflowId, datasetId);
    } catch (EEAException e) {
      LOG_ERROR.error(
          "Error creating  the associated permissions for requester role in datasetschema {}.in the dataflow: {} ",
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
  private boolean checkAccount(Long dataflowId, String account) {

    return userManagementControllerZull.getUserByEmail(account) != null;
  }


  /**
   * Check is bussines custodian.
   *
   * @param dataflowId the dataflow id
   */
  private void checkIsBussinesCustodian(Long dataflowId) {
    if (dataflowService.isDataflowType(TypeDataflowEnum.BUSINESS, EntityClassEnum.DATAFLOW,
        dataflowId) && !isAdminOrSteward(dataflowId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, EEAErrorMessage.FORBIDDEN);
    }
  }

  /**
   * Checks if is admin or steward.
   *
   * @param dataflowId the dataflow id
   * @return true, if is admin or steward
   */
  private boolean isAdminOrSteward(Long dataflowId) {
    String roleSteward = ObjectAccessRoleEnum.DATAFLOW_STEWARD.getAccessRole(dataflowId);
    String roleAdmin = "ROLE_" + SecurityRoleEnum.ADMIN;
    return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .anyMatch(role -> roleSteward.equals(role.getAuthority())
            || roleAdmin.equals(role.getAuthority()));
  }

}
