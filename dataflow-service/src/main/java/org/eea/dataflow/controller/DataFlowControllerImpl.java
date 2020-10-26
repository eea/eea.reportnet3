package org.eea.dataflow.controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.eea.dataflow.service.DataflowService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * The Class DataFlowControllerImpl.
 */
@RestController
@RequestMapping(value = "/dataflow")
@Api(tags = "Dataflows : Dataflows Manager")
public class DataFlowControllerImpl implements DataFlowController {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The dataflow service. */
  @Autowired
  private DataflowService dataflowService;

  /**
   * Find by id.
   *
   * @param dataflowId the dataflow id
   * @return the data flow VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_REQUESTER','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ')")
  @GetMapping(value = "/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find a Dataflow by its Id", produces = MediaType.APPLICATION_JSON_VALUE,
      response = DataFlowVO.class)
  @ApiResponse(code = 400, message = EEAErrorMessage.DATAFLOW_INCORRECT_ID)
  public DataFlowVO findById(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId) {

    if (dataflowId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATAFLOW_INCORRECT_ID);
    }
    DataFlowVO result = null;
    try {
      if (isUserDataCustodian()) {
        result = dataflowService.getById(dataflowId);
      } else {
        result = dataflowService.getByIdWithRepresentativesFilteredByUserEmail(dataflowId);
      }
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }
    return result;
  }

  /**
   * Find by status.
   *
   * @param status the status
   * @return the list
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping(value = "/status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find Dataflows based on the Status",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DataFlowVO.class,
      responseContainer = "List")
  public List<DataFlowVO> findByStatus(@ApiParam(type = "Object",
      value = "Dataflow status") @PathVariable("status") TypeStatusEnum status) {
    List<DataFlowVO> dataflows = new ArrayList<>();
    try {
      dataflows = dataflowService.getByStatus(status);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }
    return dataflows;
  }

  /**
   * Find pending accepted.
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping(value = "/pendingaccepted", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find Dataflows in Pending or Accepted Status for the logged User",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DataFlowVO.class,
      responseContainer = "List")
  public List<DataFlowVO> findPendingAccepted() {
    List<DataFlowVO> dataflows = new ArrayList<>();
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID);
    try {
      dataflows = dataflowService.getPendingAccepted(userId);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }
    return dataflows;
  }

  /**
   * Find completed.
   *
   * @param pageNum the page num
   * @param pageSize the page size
   * @return the list
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping(value = "/completed", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find list of completed Dataflows",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DataFlowVO.class,
      responseContainer = "List")
  public List<DataFlowVO> findCompleted(
      @ApiParam(value = "PageNum: page number to show",
          example = "0") @RequestParam(value = "pageNum", defaultValue = "0") Integer pageNum,
      @ApiParam(value = "PageSize: specifies the maximum number of records per page",
          example = "20") @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
    List<DataFlowVO> dataflows = new ArrayList<>();
    Pageable pageable = PageRequest.of(pageNum, pageSize);
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID);
    try {
      dataflows = dataflowService.getCompleted(userId, pageable);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }
    return dataflows;
  }

  /**
   * Find user dataflows by status.
   *
   * @param type the type
   * @return the list
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping(value = "/request/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find list of Dataflows based on the Status for the logged User",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DataFlowVO.class,
      responseContainer = "List")
  public List<DataFlowVO> findUserDataflowsByStatus(@ApiParam(type = "Object",
      value = "Dataflow status") @PathVariable("type") TypeRequestEnum type) {
    List<DataFlowVO> dataflows = new ArrayList<>();
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID);
    try {
      dataflows = dataflowService.getPendingByUser(userId, type);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }
    return dataflows;
  }

  /**
   * Update user request.
   *
   * @param idUserRequest the id user request
   * @param type the type
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  @PutMapping("/updateStatusRequest/{idUserRequest}")
  @ApiOperation(value = "Update a Join Request's Status for a User")
  @ApiResponse(code = 400, message = EEAErrorMessage.USER_REQUEST_NOTFOUND)
  public void updateUserRequest(
      @ApiParam(value = "User request Id",
          example = "0") @PathVariable("idUserRequest") Long idUserRequest,
      @ApiParam(type = "Object",
          value = "Join Request Status") @RequestParam("type") TypeRequestEnum type) {
    try {
      dataflowService.updateUserRequestStatus(idUserRequest, type);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }
  }

  /**
   * Adds the contributor.
   *
   * @param dataflowId the dataflow id
   * @param idContributor the id contributor
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  @PostMapping("/{dataflowId}/contributor/add")
  @ApiOperation(value = "Add one Contributor to a Dataflow")
  @ApiResponse(code = 400, message = EEAErrorMessage.USER_REQUEST_NOTFOUND)
  public void addContributor(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(value = "Contributor Id",
          example = "0") @RequestParam("idContributor") String idContributor) {
    try {
      dataflowService.addContributorToDataflow(dataflowId, idContributor);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }
  }

  /**
   * Removes the contributor.
   *
   * @param dataflowId the dataflow id
   * @param idContributor the id contributor
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  @DeleteMapping("{dataflowId}/contributor/remove")
  @ApiOperation(value = "Remove one Contributor from a Dataflow")
  @ApiResponse(code = 400, message = EEAErrorMessage.USER_REQUEST_NOTFOUND)
  public void removeContributor(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(value = "Contributor Id",
          example = "0") @RequestParam("idContributor") String idContributor) {
    try {
      dataflowService.removeContributorFromDataflow(dataflowId, idContributor);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }
  }

  /**
   * Creates the data flow.
   *
   * @param dataFlowVO the data flow VO
   *
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @LockMethod
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR hasRole('DATA_REQUESTER')")
  @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Create one Dataflow", produces = MediaType.APPLICATION_JSON_VALUE,
      response = ResponseEntity.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully created Dataflow "),
      @ApiResponse(code = 400,
          message = "1-The date has to be later than today's date \n2-Dataflow Description or Name empty \n3-Dataflow Obligation empty"),
      @ApiResponse(code = 500, message = " Internal Server Error")})
  public ResponseEntity createDataFlow(
      @ApiParam(value = "Dataflow Object") @RequestBody @LockCriteria(name = "name",
          path = "name") DataFlowVO dataFlowVO) {

    String message = "";
    HttpStatus status = HttpStatus.OK;

    final Timestamp dateToday = java.sql.Timestamp.valueOf(LocalDateTime.now());
    if (null != dataFlowVO.getDeadlineDate() && (dataFlowVO.getDeadlineDate().before(dateToday)
        || dataFlowVO.getDeadlineDate().equals(dateToday))) {

      message = EEAErrorMessage.DATE_AFTER_INCORRECT;
      status = HttpStatus.BAD_REQUEST;
    }

    if (status == HttpStatus.OK && (StringUtils.isBlank(dataFlowVO.getName())
        || StringUtils.isBlank(dataFlowVO.getDescription()))) {

      message = EEAErrorMessage.DATAFLOW_DESCRIPTION_NAME;
      status = HttpStatus.BAD_REQUEST;
    }
    if (status == HttpStatus.OK && (null == dataFlowVO.getObligation()
        || null == dataFlowVO.getObligation().getObligationId())) {
      message = EEAErrorMessage.DATAFLOW_OBLIGATION;
      status = HttpStatus.BAD_REQUEST;
    }

    if (status == HttpStatus.OK) {
      try {
        dataflowService.createDataFlow(dataFlowVO);
      } catch (EEAException e) {
        LOG_ERROR.error("Create dataflow failed. ", e.getCause());
        message = e.getMessage();
        status = HttpStatus.INTERNAL_SERVER_ERROR;
      }
    }

    return new ResponseEntity<>(message, status);
  }

  /**
   * Update data flow.
   *
   * @param dataFlowVO the data flow VO
   *
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataFlowVO.id,'DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE')")
  @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Update a Dataflow", produces = MediaType.APPLICATION_JSON_VALUE,
      response = ResponseEntity.class)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully Updated Dataflow "),
      @ApiResponse(code = 400,
          message = "1-The date has to be later than today's date\n2-Dataflow Description or Name empty\n3-Dataflow Obligation empty"),
      @ApiResponse(code = 500, message = "Internal Server Error ")})
  public ResponseEntity updateDataFlow(
      @ApiParam(value = "Dataflow Object") @RequestBody DataFlowVO dataFlowVO) {
    final Timestamp dateToday = java.sql.Timestamp.valueOf(LocalDateTime.now());

    String message = "";
    HttpStatus status = HttpStatus.OK;

    if (null != dataFlowVO.getDeadlineDate() && (dataFlowVO.getDeadlineDate().before(dateToday)
        || dataFlowVO.getDeadlineDate().equals(dateToday))) {
      message = EEAErrorMessage.DATE_AFTER_INCORRECT;
      status = HttpStatus.BAD_REQUEST;
    }
    if (status == HttpStatus.OK && (StringUtils.isBlank(dataFlowVO.getName())
        || StringUtils.isBlank(dataFlowVO.getDescription()))) {

      message = EEAErrorMessage.DATAFLOW_DESCRIPTION_NAME;
      status = HttpStatus.BAD_REQUEST;
    }
    if (status == HttpStatus.OK && (null == dataFlowVO.getObligation()
        || null == dataFlowVO.getObligation().getObligationId())) {
      message = EEAErrorMessage.DATAFLOW_OBLIGATION;
      status = HttpStatus.BAD_REQUEST;
    }

    if (status == HttpStatus.OK) {
      try {
        dataflowService.updateDataFlow(dataFlowVO);
      } catch (EEAException e) {
        LOG_ERROR.error("Update dataflow failed. ", e.getCause());
        message = e.getMessage();
        status = HttpStatus.INTERNAL_SERVER_ERROR;
      }
    }

    return new ResponseEntity<>(message, status);
  }

  /**
   * Gets the metabase by id.
   *
   * @param dataflowId the dataflow id
   *
   * @return the metabase by id
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_REQUESTER','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ')")
  @GetMapping(value = "/{dataflowId}/getmetabase", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get meta information from a Dataflow based on its Id",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DataFlowVO.class)
  @ApiResponse(code = 400, message = EEAErrorMessage.DATAFLOW_INCORRECT_ID)
  public DataFlowVO getMetabaseById(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    if (dataflowId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATAFLOW_INCORRECT_ID);
    }
    DataFlowVO result = null;
    try {
      result = dataflowService.getMetabaseById(dataflowId);

    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }
    return result;
  }

  /**
   * Delete data flow.
   *
   * @param dataflowId the dataflow id
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN')")
  @DeleteMapping("/{dataflowId}")
  @ApiOperation(value = "Delete a Dataflow by its Id")
  @ApiResponse(code = 500, message = "Internal Server Error")
  public void deleteDataFlow(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    try {
      dataflowService.deleteDataFlow(dataflowId);
    } catch (Exception e) {
      LOG_ERROR.error("Error deleting the dataflow {}. Error message: {}", dataflowId,
          e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Update data flow status.
   *
   * @param dataflowId the dataflow id
   * @param status the status
   * @param deadlineDate the deadline date
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN')")
  @PutMapping("/{dataflowId}/updateStatus")
  @ApiOperation(value = "Update one Dataflow Status")
  @ApiResponse(code = 500, message = "Internal Server Error")
  public void updateDataFlowStatus(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(value = "Status") @RequestParam("status") TypeStatusEnum status,
      @ApiParam(value = "Date ending of Dataflow") @RequestParam(value = "deadLineDate",
          required = false) Date deadlineDate) {
    try {
      dataflowService.updateDataFlowStatus(dataflowId, status, deadlineDate);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Creates the message.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param content the content
   * @return the message VO
   */
  @Override
  @PostMapping("/{dataflowId}/createMessage")
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER')")
  public MessageVO createMessage(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("providerId") Long providerId, @RequestBody String content) {
    try {
      return dataflowService.createMessage(dataflowId, providerId, content);
    } catch (EEAException e) {
      LOG_ERROR.error("Error creating message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  /**
   * Find messages.
   *
   * @param dataflowId the dataflow id
   * @param page the offset
   * @return the list
   */
  @Override
  @GetMapping("/{dataflowId}/findMessages")
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER')")
  public List<MessageVO> findMessages(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam(value = "read", required = false) boolean read,
      @RequestParam("page") int page) {
    return dataflowService.findMessages(dataflowId, read, page);
  }

  /**
   * Update message read status.
   *
   * @param dataflowId the dataflow id
   * @param messageId the message id
   * @param read the read
   * @return true, if successful
   */
  @Override
  @PutMapping("/{dataflowId}/updateMessageReadStatus")
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER')")
  public boolean updateMessageReadStatus(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("messageId") Long messageId, @RequestParam("read") boolean read) {
    return dataflowService.updateMessageReadStatus(dataflowId, messageId, read);
  }

  /**
   * Checks if is user data custodian.
   *
   * @return true, if is user data custodian
   */
  private boolean isUserDataCustodian() {
    String dataCustodianRole = "ROLE_" + SecurityRoleEnum.DATA_CUSTODIAN;
    for (GrantedAuthority role : SecurityContextHolder.getContext().getAuthentication()
        .getAuthorities()) {
      if (dataCustodianRole.equals(role.getAuthority())) {
        return true;
      }
    }
    return false;
  }
}
