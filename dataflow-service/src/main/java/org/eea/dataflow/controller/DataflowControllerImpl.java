package org.eea.dataflow.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eea.dataflow.service.DataflowService;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.dataflow.service.file.DataflowHelper;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataflowCountVO;
import org.eea.interfaces.vo.dataflow.DataflowPrivateVO;
import org.eea.interfaces.vo.dataflow.DataflowPublicVO;
import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataflow.PaginatedDataflowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.enums.EntityClassEnum;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.ums.DataflowUserRoleVO;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.lock.service.LockService;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * The Class DataflowControllerImpl.
 */
@RestController
@RequestMapping(value = "/dataflow")
@Api(tags = "Dataflows : Dataflows Manager")
public class DataflowControllerImpl implements DataFlowController {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DataflowControllerImpl.class);

  /** The dataflow service. */
  @Autowired
  @Lazy
  private DataflowService dataflowService;

  /** The representative service. */
  @Autowired
  private RepresentativeService representativeService;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The dataflow helper. */
  @Autowired
  private DataflowHelper dataflowHelper;

  /** The notification controller zuul. */
  @Autowired
  private NotificationControllerZuul notificationControllerZuul;

  /**
   * Find by id.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @return the data flow VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_STEWARD_SUPPORT','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_REQUESTER','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_STEWARD_SUPPORT','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR hasAnyRole('ADMIN')")
  @GetMapping(value = "/v1/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get dataflow by dataflow id", produces = MediaType.APPLICATION_JSON_VALUE,
      response = DataFlowVO.class,
      notes = "Allowed roles: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT, LEAD REPORTER, REPORTER WRITE, REPORTER READ, EDITOR READ, EDITOR WRITE, NATIONAL COORDINATOR, ADMIN.\nReporters must include providerId to get dataflow data.")
  @ApiResponse(code = 400, message = EEAErrorMessage.DATAFLOW_INCORRECT_ID)
  public DataFlowVO findById(
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(value = "Provider id", example = "0", required = false) @RequestParam(value = "providerId", required = false) Long providerId) {

    if (dataflowId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATAFLOW_INCORRECT_ID);
    }
    DataFlowVO result = null;
    try {
      if (isUserRequester(dataflowId)) {
        result = dataflowService.getById(dataflowId, true);
      } else {
        result =
            dataflowService.getByIdWithRepresentativesFilteredByUserEmail(dataflowId, providerId);
      }
    } catch (EEAException e) {
      LOG.error("Could not retrieve dataflow with id {} and providerId {} ", dataflowId, providerId, e);
    } catch (Exception e){
      LOG.error("Unexpected error! Could not retrieve dataflow with id {} and providerId {} ", dataflowId, providerId, e);
      throw e;
    }
    return result;
  }

  /**
   * Find dataflow name by id.
   *
   * @param dataflowId the dataflow id
   * @return the data flow name
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping(value = "/v1/dataflowName/{dataflowId}")
  public String findDataflowNameById(@PathVariable("dataflowId") Long dataflowId){
    try{
      return dataflowService.getDataflowNameById(dataflowId);
    }
    catch(Exception e){
      LOG.error("Unexpected error! Could not retrieve dataflow name for dataflow with id {} ", dataflowId, e);
      throw e;
    }
  }

  /**
   * Find by id legacy.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @return the data flow VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_STEWARD_SUPPORT','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_REQUESTER','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_STEWARD_SUPPORT','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR hasAnyRole('ADMIN')")
  @GetMapping(value = "/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get Dataflow by Id", produces = MediaType.APPLICATION_JSON_VALUE,
      response = DataFlowVO.class, hidden = true)
  @ApiResponse(code = 400, message = EEAErrorMessage.DATAFLOW_INCORRECT_ID)
  public DataFlowVO findByIdLegacy(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(value = "Provider Id", example = "0", required = false) @RequestParam(value = "providerId", required = false) Long providerId) {
    return this.findById(dataflowId, providerId);
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
      responseContainer = "List", hidden = true)
  public List<DataFlowVO> findByStatus(@ApiParam(type = "Object", value = "Dataflow status",
      example = "DESIGN") @PathVariable("status") TypeStatusEnum status) {
    List<DataFlowVO> dataflows = new ArrayList<>();
    try {
      dataflows = dataflowService.getByStatus(status);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    } catch (Exception e){
      LOG_ERROR.error("Unexpected error! Could not retrieve dataflow by status. Message: {}", e.getMessage());
      throw e;
    }
    return dataflows;
  }


  /**
   * Find dataflows.
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @PostMapping(value = "/getDataflows", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find Dataflows for the logged User",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DataFlowVO.class,
      responseContainer = "List", hidden = true)
  public PaginatedDataflowVO findDataflows(
      @RequestBody(required = false) Map<String, String> filters,
      @RequestParam(required = false) String orderHeader,
      @RequestParam(required = false) boolean asc, 
      @RequestParam(required = false) Integer pageSize,
      @RequestParam(required = false) Integer pageNum) {
    PaginatedDataflowVO dataflows = new PaginatedDataflowVO();
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID);
    try {
      dataflows = dataflowService.getDataflows(userId, TypeDataflowEnum.REPORTING, filters,
          orderHeader, asc, pageSize, pageNum);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    } catch (Exception e){
      LOG_ERROR.error("Unexpected error! Could not retrieve dataflows for userId {}. Message: {}", userId, e.getMessage());
      throw e;
    }
    return dataflows;
  }


  /**
   * Find reference dataflows.
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @PostMapping(value = "/referenceDataflows", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find Reference Dataflows for the logged User",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DataFlowVO.class,
      responseContainer = "List", hidden = true)
  public PaginatedDataflowVO findReferenceDataflows(
      @RequestBody(required = false) Map<String, String> filters,
      @RequestParam(required = false) String orderHeader,
      @RequestParam(required = false) boolean asc, 
      @RequestParam(required = false) Integer pageSize,
      @RequestParam(required = false) Integer pageNum) {
    PaginatedDataflowVO dataflows = new PaginatedDataflowVO();
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID);
    try {
      dataflows = dataflowService.getDataflows(userId, TypeDataflowEnum.REFERENCE, filters,
          orderHeader, asc, pageSize, pageNum);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    } catch (Exception e){
      LOG_ERROR.error("Unexpected error! Could not retrieve reference dataflows for userId {}. Message: {}", userId, e.getMessage());
      throw e;
    }
    return dataflows;
  }

  /**
   * Find business dataflows.
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @PostMapping(value = "/businessDataflows", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find Business Dataflows for the logged User",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DataFlowVO.class,
      responseContainer = "List", hidden = true)
  public PaginatedDataflowVO findBusinessDataflows(
      @RequestBody(required = false) Map<String, String> filters,
      @RequestParam(required = false) String orderHeader,
      @RequestParam(required = false) boolean asc, 
      @RequestParam(required = false) Integer pageSize,
      @RequestParam(required = false) Integer pageNum)

  {
    PaginatedDataflowVO dataflows = new PaginatedDataflowVO();
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID);
    try {
      dataflows = dataflowService.getDataflows(userId, TypeDataflowEnum.BUSINESS, filters,
          orderHeader, asc, pageSize, pageNum);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    } catch (Exception e){
      LOG_ERROR.error("Unexpected error! Could not retrieve business dataflows for userId {}. Message: {}", userId, e.getMessage());
      throw e;
    }
    return dataflows;
  }



  /**
   * Find citizen science dataflows.
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @PostMapping(value = "/citizenScienceDataflows", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find Citizen Science Dataflows for the logged User",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DataFlowVO.class,
      responseContainer = "List", hidden = true)
  public PaginatedDataflowVO findCitizenScienceDataflows(
      @RequestBody(required = false) Map<String, String> filters,
      @RequestParam(required = false) String orderHeader,
      @RequestParam(required = false) boolean asc, @RequestParam(required = false) Integer pageSize,
      @RequestParam(required = false) Integer pageNum) {
    PaginatedDataflowVO dataflows = new PaginatedDataflowVO();
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID);
    try {
      dataflows = dataflowService.getDataflows(userId, TypeDataflowEnum.CITIZEN_SCIENCE, filters,
          orderHeader, asc, pageSize, pageNum);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    } catch (Exception e){
      LOG_ERROR.error("Unexpected error! Could not retrieve citizen science dataflows for userId {}. Message: {}", userId, e.getMessage());
      throw e;
    }
    return dataflows;
  }

  /**
   * Find cloneable dataflows.
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping(value = "/cloneableDataflows", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find Dataflows for clone for the logged User",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DataFlowVO.class,
      responseContainer = "List", hidden = true)
  public List<DataFlowVO> findCloneableDataflows() {
    List<DataFlowVO> dataflows = new ArrayList<>();
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID);
    try {
      // All dataflows except REFERENCE type dataflow
      dataflows = dataflowService.getCloneableDataflows(userId);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    } catch (Exception e){
      LOG_ERROR.error("Unexpected error! Could not retrieve clonable dataflows for userId {}. Message: {}", userId, e.getMessage());
      throw e;
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
      responseContainer = "List", hidden = true)
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
    } catch (Exception e){
      LOG_ERROR.error("Unexpected error! Could not retrieve completed dataflows for userId {}. Message: {}", userId, e.getMessage());
      throw e;
    }
    return dataflows;
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
  @PreAuthorize("hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD','DATA_REQUESTER','ADMIN')")
  @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Create one Dataflow", produces = MediaType.APPLICATION_JSON_VALUE,
      response = ResponseEntity.class, hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully created Dataflow "),
      @ApiResponse(code = 400,
          message = "1-The date has to be later than today's date \n2-Dataflow Description or Name empty \n3-Dataflow Obligation empty"),
      @ApiResponse(code = 500, message = " Internal Server Error")})
  public ResponseEntity createDataFlow(
      @ApiParam(value = "Dataflow Object") @RequestBody @LockCriteria(name = "name",
          path = "name") DataFlowVO dataFlowVO) {

    String message = "";
    HttpStatus status = HttpStatus.OK;
    if (TypeDataflowEnum.BUSINESS.equals(dataFlowVO.getType()) && !dataflowService.isAdmin()) {
      message = EEAErrorMessage.UNAUTHORIZED;
      status = HttpStatus.UNAUTHORIZED;
    }
    final Timestamp dateToday = java.sql.Timestamp.valueOf(LocalDateTime.now());
    if (status == HttpStatus.OK && !TypeDataflowEnum.REFERENCE.equals(dataFlowVO.getType())
        && null != dataFlowVO.getDeadlineDate() && (dataFlowVO.getDeadlineDate().before(dateToday)
            || dataFlowVO.getDeadlineDate().equals(dateToday))) {

      message = EEAErrorMessage.DATE_AFTER_INCORRECT;
      status = HttpStatus.BAD_REQUEST;
    }

    if (status == HttpStatus.OK && (StringUtils.isBlank(dataFlowVO.getName())
        || StringUtils.isBlank(dataFlowVO.getDescription()))) {

      message = EEAErrorMessage.DATAFLOW_DESCRIPTION_NAME;
      status = HttpStatus.BAD_REQUEST;
    }
    if (!TypeDataflowEnum.REFERENCE.equals(dataFlowVO.getType()) && status == HttpStatus.OK
        && (null == dataFlowVO.getObligation()
            || null == dataFlowVO.getObligation().getObligationId())) {
      message = EEAErrorMessage.DATAFLOW_OBLIGATION;
      status = HttpStatus.BAD_REQUEST;
    }

    if (status == HttpStatus.OK) {
      try {
        Long dataflowId = dataflowService.createDataFlow(dataFlowVO);
        LOG.info("Successfully created dataflow with id {}", dataflowId);
        message = dataflowId.toString();
      } catch (EEAException e) {
        if(dataFlowVO != null){
          LOG_ERROR.error("Creating dataflow with name {} failed. Message: {} ", dataFlowVO.getName(), e.getMessage());
        }
        else{
          LOG_ERROR.error("Creating dataflow failed because object is null. Message: {} ", e.getMessage());
        }
        message = "There was an unknown error creating the dataflow.";
        status = HttpStatus.INTERNAL_SERVER_ERROR;
      } catch (Exception e){
        Long dataflowId = (dataFlowVO != null) ? dataFlowVO.getId() : null;
        String dataflowName = (dataFlowVO != null) ? dataFlowVO.getName() : null;
        LOG_ERROR.error("Unexpected error! Could not create dataflow with id {} and name {}. Message: {}", dataflowId, dataflowName, e.getMessage());
        throw e;
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
  @PreAuthorize("secondLevelAuthorize(#dataFlowVO.id,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR hasAnyRole('ADMIN')")
  @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Update a Dataflow", produces = MediaType.APPLICATION_JSON_VALUE,
      response = ResponseEntity.class, hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully Updated Dataflow "),
      @ApiResponse(code = 400,
          message = "1-The date has to be later than today's date\n2-Dataflow Description or Name empty\n3-Dataflow Obligation empty"),
      @ApiResponse(code = 500, message = "Internal Server Error ")})
  public ResponseEntity updateDataFlow(
      @ApiParam(value = "Dataflow Object") @RequestBody DataFlowVO dataFlowVO) {
    final Timestamp dateToday = java.sql.Timestamp.valueOf(LocalDateTime.now());

    String message = "";
    HttpStatus status = HttpStatus.OK;
    boolean isAdmin = dataflowService.isAdmin();

    if (!isAdmin && !TypeDataflowEnum.REFERENCE.equals(dataFlowVO.getType())
        && null != dataFlowVO.getDeadlineDate() && (dataFlowVO.getDeadlineDate().before(dateToday)
            || dataFlowVO.getDeadlineDate().equals(dateToday))) {
      message = EEAErrorMessage.DATE_AFTER_INCORRECT;
      status = HttpStatus.BAD_REQUEST;
    }

    if (!isAdmin && status == HttpStatus.OK && (StringUtils.isBlank(dataFlowVO.getName())
        || StringUtils.isBlank(dataFlowVO.getDescription()))) {
      message = EEAErrorMessage.DATAFLOW_DESCRIPTION_NAME;
      status = HttpStatus.BAD_REQUEST;
    } else {
      if (isAdmin && status == HttpStatus.OK && StringUtils.isBlank(dataFlowVO.getName())) {
        message = EEAErrorMessage.DATAFLOW_NAME_EMPTY;
        status = HttpStatus.BAD_REQUEST;
      }
    }

    if (status == HttpStatus.OK && !isAdmin) {
      try {
        DataFlowVO dataflow = dataflowService.getMetabaseById(dataFlowVO.getId());
        if (!TypeStatusEnum.DESIGN.equals(dataflow.getStatus())
            && ((TypeDataflowEnum.CITIZEN_SCIENCE.equals(dataflow.getType())
                || TypeDataflowEnum.REPORTING.equals(dataflow.getType()))
                && (dataflow.isReleasable() == dataFlowVO.isReleasable()
                    && dataflow.isShowPublicInfo() == dataFlowVO.isShowPublicInfo()))) {
          message = EEAErrorMessage.DATAFLOW_UPDATE_ERROR;
          status = HttpStatus.BAD_REQUEST;
        }
      } catch (EEAException e) {
        LOG_ERROR.error("Error finding dataflow metabase from dataflow id {}", dataFlowVO.getId());
      } catch (Exception e){
        Long dataflowId = (dataFlowVO != null) ? dataFlowVO.getId() : null;
        LOG_ERROR.error("Unexpected error! Could not find dataflow with id {} in metabase. Message: {}", dataflowId, e.getMessage());
        throw e;
      }
    }

    if (!isAdmin && !TypeDataflowEnum.REFERENCE.equals(dataFlowVO.getType())
        && status == HttpStatus.OK && (null == dataFlowVO.getObligation()
            || null == dataFlowVO.getObligation().getObligationId())) {
      message = EEAErrorMessage.DATAFLOW_OBLIGATION;
      status = HttpStatus.BAD_REQUEST;
    }
    // If it's a Business Dataflow, check if there are representatives selected. If so, then deny
    // the update
    if (TypeDataflowEnum.BUSINESS.equals(dataFlowVO.getType()) && status == HttpStatus.OK) {
      try {
        DataFlowVO dataflow = dataflowService.getMetabaseById(dataFlowVO.getId());
        if ((!isAdmin && !TypeStatusEnum.DESIGN.equals(dataflow.getStatus()))) {
          message = EEAErrorMessage.DATAFLOW_UPDATE_ERROR;
          status = HttpStatus.BAD_REQUEST;
        }
        if (!dataflow.getDataProviderGroupId().equals(dataFlowVO.getDataProviderGroupId())
            && !representativeService.getRepresetativesByIdDataFlow(dataFlowVO.getId()).isEmpty()) {
          message = EEAErrorMessage.EXISTING_REPRESENTATIVES;
          status = HttpStatus.BAD_REQUEST;
        }
      } catch (EEAException e) {
        LOG_ERROR.error("Error finding the representatives from the dataflowId {}",
            dataFlowVO.getId());
      } catch (Exception e){
        Long dataflowId = (dataFlowVO != null) ? dataFlowVO.getId() : null;
        LOG_ERROR.error("Unexpected error! Could not find the representatives for dataflow with id {}. Message: {}", dataflowId, e.getMessage());
        throw e;
      }
    }

    if (status == HttpStatus.OK) {
      try {
        dataflowService.updateDataFlow(dataFlowVO);
        LOG.info("Successfully updated dataflow with id {}", dataFlowVO.getId());
      } catch (EEAException e) {
        LOG_ERROR.error("Update dataflow failed. ", e.getCause());
        message = "There was an unknown error updating the dataflow.";
        status = HttpStatus.INTERNAL_SERVER_ERROR;
      } catch (Exception e){
        Long dataflowId = (dataFlowVO != null) ? dataFlowVO.getId() : null;
        LOG_ERROR.error("Unexpected error! Could not update dataflow with id {} Message: {}", dataflowId, e.getMessage());
        throw e;
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
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_STEWARD_SUPPORT','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_REQUESTER','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD','ADMIN')")
  @GetMapping(value = "/v1/{dataflowId}/getmetabase", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get dataflow metadata by dataflow id",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DataFlowVO.class,
      notes = "Allowed roles: CUSTODIAN, STEWARD, OBSERVER, LEAD REPORTER, REPORTER WRITE, REPORTER READ, EDITOR READ, EDITOR WRITE, NATIONAL COORDINATOR, ADMIN, STEWARD SUPPORT")
  @ApiResponse(code = 400, message = EEAErrorMessage.DATAFLOW_INCORRECT_ID)
  public DataFlowVO getMetabaseById(
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    if (dataflowId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATAFLOW_INCORRECT_ID);
    }
    DataFlowVO result = null;
    try {
      result = dataflowService.getMetabaseById(dataflowId);

    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    } catch (Exception e){
      LOG_ERROR.error("Unexpected error! Could not retrieve dataflow metadata for dataflowId {} Message: {}", dataflowId, e.getMessage());
      throw e;
    }
    return result;
  }

  /**
   * Gets the metabase by id legacy.
   *
   * @param dataflowId the dataflow id
   * @return the metabase by id legacy
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_STEWARD_SUPPORT','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_REQUESTER','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD','ADMIN')")
  @GetMapping(value = "/{dataflowId}/getmetabase", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get meta information from a Dataflow based on its Id",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DataFlowVO.class, hidden = true)
  @ApiResponse(code = 400, message = EEAErrorMessage.DATAFLOW_INCORRECT_ID)
  public DataFlowVO getMetabaseByIdLegacy(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    return this.getMetabaseById(dataflowId);
  }

  /**
   * Delete data flow.
   *
   * @param dataflowId the dataflow id
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN') OR (hasRole('ADMIN') AND checkAccessEntity('BUSINESS','DATAFLOW',#dataflowId))")
  @DeleteMapping("/{dataflowId}")
  @ApiOperation(value = "Delete a Dataflow by its Id", hidden = true)
  @ApiResponse(code = 500, message = "Internal Server Error")
  @HystrixCommand
  public void deleteDataFlow(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId) {

    DataFlowVO dataflowData = null;
    try {
      dataflowData = dataflowService.getMetabaseById(dataflowId);
    } catch (EEAException e) {
      LOG.error(String.format(
          "Couldn't retrieve the dataflow information with the provided dataflowId %s",
          dataflowId));
    } catch (Exception e){
      LOG_ERROR.error("Unexpected error! Could not retrieve dataflow information for dataflowId {} Message: {}", dataflowId, e.getMessage());
      throw e;
    }
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    Map<String, Object> importDatasetData = new HashMap<>();
    importDatasetData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_SCHEMAS.getValue());
    importDatasetData.put(LiteralConstants.DATAFLOWID, dataflowId);
    LockVO importLockVO = lockService.findByCriteria(importDatasetData);

    Map<String, Object> copyDatasetSchema = new HashMap<>();
    copyDatasetSchema.put(LiteralConstants.SIGNATURE, LockSignature.COPY_DATASET_SCHEMA.getValue());
    copyDatasetSchema.put(LiteralConstants.DATAFLOWIDDESTINATION, dataflowId);
    LockVO cloneLockVO = lockService.findByCriteria(copyDatasetSchema);

    if (importLockVO != null) {
      throw new ResponseStatusException(HttpStatus.LOCKED,
          "Dataflow is locked because import is in progress.");
    } else if (cloneLockVO != null) {
      throw new ResponseStatusException(HttpStatus.LOCKED,
          "Dataflow is locked because clone is in progress.");
    } else if (!dataflowService.isAdmin() && dataflowData != null
        && dataflowData.getType() == TypeDataflowEnum.BUSINESS) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "Can't delete a Business Dataflow without being an admin user.");
    } else {
      try{
        LOG.info("Deleting dataflow with id {}", dataflowId);
        dataflowService.deleteDataFlow(dataflowId);
        LOG.info("Successfully deleted dataflow with id {}", dataflowId);
      } catch (Exception e){
        LOG_ERROR.error("Unexpected error! Could not delete dataflow with id {} Message: {}", dataflowId, e.getMessage());
        throw e;
      }
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
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN')")
  @PutMapping("/{dataflowId}/updateStatus")
  @ApiOperation(value = "Update one Dataflow Status", hidden = true)
  @ApiResponse(code = 500, message = "Internal Server Error")
  public void updateDataFlowStatus(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(value = "Status", example = "DESIGN") @RequestParam("status") TypeStatusEnum status,
      @ApiParam(value = "Date ending of Dataflow") @RequestParam(value = "deadLineDate",
          required = false) Date deadlineDate) {
    try {
      dataflowService.updateDataFlowStatus(dataflowId, status, deadlineDate);
    } catch (Exception e) {
      LOG.error(String.format(
          "Unexpected error! Could not update the dataflow status with the provided dataflowId %s.", dataflowId), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Couldn't update the dataflow status. An unknown error happenned.");
    }
  }


  /**
   * Gets the public dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the public dataflow
   */
  @Override
  @GetMapping("/getPublicDataflow/{dataflowId}")
  @ApiOperation(value = "Gets the public dataflow by Id", hidden = true)
  @ApiResponses(value = {
      @ApiResponse(code = 404, message = "Dataflow not found using the dataflowId provided."),
      @ApiResponse(code = 500, message = "Internal server error")})
  public DataflowPublicVO getPublicDataflow(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    try {
      return dataflowService.getPublicDataflowById(dataflowId);
    } catch (EEAException e) {
      if (EEAErrorMessage.DATAFLOW_NOTFOUND.equals(e.getMessage())) {
        LOG.error(String.format(
            "Couldn't find the public dataflow with the provided dataflowId %s.", dataflowId), e);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            "An error happenned trying to retrieve the dataflow");
      }
      LOG.error(String.format(
          "Couldn't retrieve the public dataflow with the provided dataflowId %s.", dataflowId), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "An error happenned trying to retrieve the dataflow");
    } catch (Exception e){
      LOG_ERROR.error("Unexpected error! Could not retrieve public dataflow with id {} Message: {}", dataflowId, e.getMessage());
      throw e;
    }
  }

  /**
   * Gets the public dataflows.
   *
   * @param filters the filters
   * @param orderHeader the order header
   * @param asc the asc
   * @param pageSize the page size
   * @param pageNum the page num
   * @return the public dataflows
   */
  @Override
  @PostMapping("/getPublicDataflows")

  @ApiOperation(value = "Gets all the public dataflows", hidden = true)
  public PaginatedDataflowVO getPublicDataflows(
      @RequestBody(required = false) Map<String, String> filters,
      @RequestParam(required = false) String orderHeader,
      @RequestParam(required = false) boolean asc, @RequestParam(required = false) Integer pageSize,
      @RequestParam(required = false) Integer pageNum) {
    try {
      return dataflowService.getPublicDataflows(filters, orderHeader, asc, pageSize, pageNum);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "An error happenned trying to retrieve the dataflows");
    } catch (Exception e){
      LOG_ERROR.error("Unexpected error! Could not retrieve public dataflows. Message: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Gets the public dataflows by country.
   *
   * @param countryCode the country code
   * @param pageNum the page num
   * @param pageSize the page size
   * @param sortField the sort field
   * @param asc the asc
   * @param filters the filters
   * @return the public dataflows by country
   */
  @Override
  @PostMapping("/public/country/{countryCode}")
  @ApiOperation(value = "Gets all the public dataflow that use a specific Country Code",
      hidden = false)
  public PaginatedDataflowVO getPublicDataflowsByCountry(
      @ApiParam(value = "Country Code",
          example = "AL") @PathVariable("countryCode") String countryCode,
      @ApiParam(value = "pageNum: page number to show", example = "0",
          defaultValue = "0") @RequestParam(value = "pageNum", defaultValue = "0",
              required = false) Integer pageNum,
      @ApiParam(value = "pageSize: specifies the maximum number of records per page",
          example = "10", defaultValue = "10") @RequestParam(value = "pageSize",
              defaultValue = "10", required = false) Integer pageSize,
      @ApiParam(
          value = "sortField: specifies the field which should be used to sort the data retrieved",
          example = "name") @RequestParam(value = "sortField", required = false) String sortField,
      @ApiParam(value = "asc: is the sorting order ascending or descending?", example = "false",
          defaultValue = "true") @RequestParam(value = "asc", defaultValue = "true") boolean asc,
      @RequestBody(required = false) Map<String, String> filters) {
    try {
      return dataflowService.getPublicDataflowsByCountry(countryCode, sortField, asc, pageNum,
          pageSize, filters);
    } catch (EEAException e) {
      LOG_ERROR.info(
          String.format("There was an error retrieving the public dataflows for the country: %s",
              countryCode),
          e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DATAFLOW_GET_ERROR);
    } catch (Exception e){
      LOG_ERROR.error("Unexpected error! Could not retrieve public dataflows for countryCode {} Message: {}", countryCode, e.getMessage());
      throw e;
    }
  }

  /**
   * Update data flow public status.
   *
   * @param dataflowId the dataflow id
   * @param showPublicInfo the show public info
   */
  @Override
  @PutMapping("private/updatePublicStatus")
  @ApiOperation(value = "Updates a public dataflow status", hidden = true)
  public void updateDataFlowPublicStatus(
      @ApiParam(value = "Dataflow Id", example = "1") @RequestParam("dataflowId") Long dataflowId,
      @ApiParam(value = "Show Public Info",
          example = "true") @RequestParam("showPublicInfo") boolean showPublicInfo) {
    dataflowService.updateDataFlowPublicStatus(dataflowId, showPublicInfo);
  }


  /**
   * Gets the user roles all dataflows.
   *
   * @return the user roles all dataflows
   */
  @Override
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/getUserRolesAllDataflows")
  @ApiOperation(value = "Gets the user roles for all dataflows", hidden = true)
  @ApiResponse(code = 401, message = EEAErrorMessage.UNAUTHORIZED)
  public List<DataflowUserRoleVO> getUserRolesAllDataflows() {
    List<Long> dataProviderIds = new ArrayList<>();
    List<DataflowUserRoleVO> result = new ArrayList<>();
    List<DataFlowVO> dataflows;
    try {
      // get providerId and check if user is National coordinator
      dataProviderIds = representativeService.getProviderIds();
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, EEAErrorMessage.UNAUTHORIZED);
    } catch (Exception e){
      LOG_ERROR.error("Unexpected error! Could not retrieve provider ids. Message: {}", e.getMessage());
      throw e;
    }

    try {
      dataflows = dataflowService.getDataflowsByDataProviderIds(dataProviderIds);
      LOG.info("getDataflowsByDataProviderIds finished");
      dataProviderIds.stream().forEach(
              dataProvider -> result.addAll(dataflowService.getUserRoles(dataProvider, dataflows)));
      LOG.info("streaming getDataflowsByDataProviderIds finished");
    } catch (Exception e){
      LOG_ERROR.error("Unexpected error! Could not retrieve dataflows by providerIds. Message: {}", e.getMessage());
      throw e;
    }

    return result;
  }

  /**
   * Access reference entity.
   *
   * @param entity the entity
   * @param entityId the entity id
   * @return true, if successful
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/private/isReferenceDataflowDraft/entity/{entity}/{entityId}")
  @ApiOperation(value = "Checks if a reference dataflow/dataset has draft status.", hidden = true)
  public boolean accessReferenceEntity(
      @ApiParam(value = "Entity type",
          example = "DATAFLOW") @PathVariable("entity") EntityClassEnum entity,
      @ApiParam(value = "Entity id", example = "124") @PathVariable("entityId") Long entityId) {
    return dataflowService.isReferenceDataflowDraft(entity, entityId);
  }


  /**
   * Access entity.
   *
   * @param dataflowType the dataflow type
   * @param entity the entity
   * @param entityId the entity id
   * @return true, if successful
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/private/isDataflowType/{type}/entity/{entity}/{entityId}")
  @ApiOperation(value = "Checks if the type of the entity passed as parameter is of the type given",
      hidden = true)
  public boolean accessEntity(
      @ApiParam(value = "Dataflow type",
          example = "BUSINESS") @PathVariable("type") TypeDataflowEnum dataflowType,
      @ApiParam(value = "Entity type",
          example = "DATAFLOW") @PathVariable("entity") EntityClassEnum entity,
      @ApiParam(value = "Entity id", example = "120") @PathVariable("entityId") Long entityId) {
    return dataflowService.isDataflowType(dataflowType, entity, entityId);
  }


  /**
   * Gets the private dataflow by id.
   *
   * @param dataflowId the dataflow id
   * @return the private dataflow by id
   */
  @Override
  @HystrixCommand
  @GetMapping("/getPrivateDataflow/{dataflowId}")
  @ApiOperation(value = "Gets a private dataflow based on given Id", hidden = true)
  public DataflowPrivateVO getPrivateDataflowById(@ApiParam(value = "Dataflow Id",
      example = "125") @PathVariable("dataflowId") Long dataflowId) {
    DataflowPrivateVO dataflowPrivateVO = null;
    try {
      return dataflowService.getPrivateDataflowById(dataflowId);
    } catch (EEAException e) {
      LOG_ERROR.info(String.format("Couldn't find a dataflow with id %s", dataflowId));
    } catch (Exception e) {
      LOG_ERROR.error("Unexpected error! Could not retrieve private dataflowId {}. Message {}", dataflowId, e.getMessage());
      throw e;
    }
    return dataflowPrivateVO;
  }

  /**
   * Gets the dataflows count.
   *
   * @return the dataflows count
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping(value = "/countByType", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Counts Dataflows by type for the logged User",
      response = DataflowCountVO.class, responseContainer = "List", hidden = true)
  public List<DataflowCountVO> getDataflowsCount() {
    List<DataflowCountVO> dataflowTypesCount = null;

    try {
      dataflowTypesCount = dataflowService.getDataflowsCount();
    } catch (EEAException e) {
      LOG_ERROR.error(String.format(
          "There was an error while retrieving the amount of dataflows of each dataflow type: %s",
          e.getMessage()));
    } catch (Exception e) {
      LOG_ERROR.error("Unexpected error! Could not retrieve number of dataflows. Message {}", e.getMessage());
      throw e;
    }
    return dataflowTypesCount;
  }


  /**
   * Gets the dataset summary by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the dataset summary by dataflow id
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasAnyRole('ADMIN','DATA_CUSTODIAN')")
  @GetMapping("/{dataflowId}/datasetsSummary")
  @ApiOperation(value = "Get a summary of the information of all the dataset types of a dataflow",
      hidden = true)
  @ApiResponse(code = 400, message = EEAErrorMessage.DATAFLOW_INCORRECT_ID)
  public List<DatasetsSummaryVO> getDatasetSummaryByDataflowId(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    List<DatasetsSummaryVO> datasetsSummary = null;
    try {
      datasetsSummary = dataflowService.getDatasetSummary(dataflowId);
    } catch (EEAException e) {
      LOG_ERROR.info(String.format("Error obtaining the dataset types for the dataflow with id %s.",
          dataflowId));
    } catch (Exception e) {
      LOG_ERROR.error("Unexpected error! Could not retrieve dataset types for dataflowId {}. Message {}", dataflowId, e.getMessage());
      throw e;
    }
    return datasetsSummary;
  }

  /**
   * Export schema information.
   *
   * @param dataflowId the dataflow id
   */
  @Override
  @PostMapping("/exportSchemaInformation/{dataflowId}")
  @ApiOperation(value = "Export a file with all Schema Information", hidden = true)
  public void exportSchemaInformation(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    LOG.info("Export schema information from dataflowId {}", dataflowId);
    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDataflowId(dataflowId);
    notificationControllerZuul.createUserNotificationPrivate("DOWNLOAD_SCHEMAS_INFO_START",
        userNotificationContentVO);
    try {
      LOG.info("Exporting schema information for dataflow with id {}", dataflowId);
      dataflowHelper.exportSchemaInformation(dataflowId);
      LOG.info("Successfully exported schema information for dataflow with id {}", dataflowId);
    } catch (IOException | EEAException e) {
      LOG_ERROR.error(
          "Error downloading file generated from export from the dataflowId {}. Message: {}",
          dataflowId, e.getMessage());
    } catch (Exception e) {
      LOG_ERROR.error("Unexpected error! Could not export schema information for dataflowId {}. Message {}", dataflowId, e.getMessage());
      throw e;
    }
  }

  /**
   * Download schema information.
   *
   * @param dataflowId the dataflow id
   * @param fileName the file name
   * @param response the response
   */
  @Override
  @GetMapping("/downloadSchemaInformation/{dataflowId}")
  @ApiOperation(value = "Download a file with all Schema Information from a dataflow",
      hidden = true)
  public void downloadSchemaInformation(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @RequestParam String fileName, HttpServletResponse response) {
    try {
      LOG.info(
          "Downloading file generated when exporting Schema Information. DataflowId {}. Filename {}",
          dataflowId, fileName);
      File file = dataflowHelper.downloadSchemaInformation(dataflowId, fileName);
      LOG.info("Successfully downloaded file generated when exporting Schema Information. DataflowId {}. Filename {}", dataflowId, fileName);
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

      OutputStream out = response.getOutputStream();
      FileInputStream in = new FileInputStream(file);
      // copy from in to out
      IOUtils.copyLarge(in, out);
      out.close();
      in.close();
      // delete the file after downloading it
      FileUtils.forceDelete(file);
    } catch (IOException | ResponseStatusException e) {
      LOG_ERROR.error(
          "Downloading file generated when exporting Schema Information. DataflowId {}. Filename {}. Error message: {}",
          dataflowId, fileName, e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(
          "Trying to download a file generated during the export Schema Information process but the file is not found: dataflowId: %s, filename: %s",
          dataflowId, fileName));
    } catch (Exception e) {
      LOG_ERROR.error("Unexpected error! Could not download schema information for dataflowId {}. Message {}", dataflowId, e.getMessage());
      throw e;
    }
  }

  /**
   * Download public schema information.
   *
   * @param dataflowId the dataflow id
   * @return the response entity
   */
  @Override
  @GetMapping("/downloadPublicSchemaInformation/{dataflowId}")
  @ApiOperation(value = "Download a file with all Schema Information from a public dataflow",
      hidden = true)
  public ResponseEntity<byte[]> downloadPublicSchemaInformation(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId) {

    try {
      dataflowService.getPublicDataflowById(dataflowId);
      LOG.info("Downloading file Schema Information from dataflowId {}", dataflowId);
      String composedFileName = "dataflow-" + dataflowId + "-Schema_Information";
      String fileNameWithExtension = composedFileName + "_"
          + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss")) + "."
          + FileTypeEnum.XLSX.getValue();

      byte[] file = dataflowHelper.downloadPublicSchemaInformation(dataflowId);
      LOG.info("Successfully downloaded file Schema Information from dataflowId {}", dataflowId);
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
          "attachment; filename=" + fileNameWithExtension);
      return new ResponseEntity<>(file, httpHeaders, HttpStatus.OK);

    } catch (EEAException e) {
      LOG_ERROR.error("DataflowId {} is not public. Error message: {}", dataflowId, e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          "Couldn't find a public dataflow with provided Id");
    } catch (ResponseStatusException | IOException e) {
      LOG_ERROR.error(
          "Error downloading file schema information from the dataflowId {}, with message: {}",
          dataflowId, e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          "There was an error downloading the dataflow schema information.");
    } catch (Exception e) {
      LOG_ERROR.error("Unexpected error! Could not download public schema information for dataflowId {}. Message {}", dataflowId, e.getMessage());
      throw e;
    }
  }

  /**
   * Validate all reporters.
   *
   * @return the response entity
   */
  @Override
  @PutMapping("/validateAllReporters")
  @PreAuthorize("hasAnyRole('ADMIN')")
  @ApiOperation(
      value = "Validates lead reporters and reporters from all the dataflows in the system.",
      hidden = true)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Reporters and Lead Reporters validated successfully."),
      @ApiResponse(code = 400,
          message = "There was an error validating Reporters and Lead Reporters.")})
  public ResponseEntity validateAllReporters() {
    String message = "";
    HttpStatus status = HttpStatus.OK;
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID);

    try {
      LOG.info("Validating all reporters with userId {}", userId);
      dataflowService.validateAllReporters(userId);
    } catch (Exception e) {
      message =
          "Couldn't validate all reporters and lead reporters, an error was produced during the process.";
      LOG.error("Unexpected error! Could not validate all reporters and lead reporters, an error was produced during the process.");
      status = HttpStatus.BAD_REQUEST;
    }

    return new ResponseEntity<>(message, status);
  }

  /**
   * Update data flow automatic reporting deletion.
   *
   * @param dataflowId the dataflow id
   * @param automaticReportingDelete the automatic reporting delete
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN')")
  @PutMapping("/{dataflowId}/updateAutomaticDelete")
  @ApiOperation(value = "Update one Dataflow Automatic Delete Data and Snapshot", hidden = true)
  @ApiResponse(code = 500, message = "Internal Server Error")
  public void updateDataFlowAutomaticReportingDeletion(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("automaticDelete") boolean automaticReportingDelete) {
    try {
      dataflowService.updateDataFlowAutomaticReportingDeletion(dataflowId,
          automaticReportingDelete);
    } catch (Exception e) {
      LOG.error(String.format(
          "Unexpected error! Could not update the dataflow automatic delete data and snapshots with the provided dataflowId %s.",
          dataflowId), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Couldn't update the dataflow automatic delete data and snapshots. An unknown error happenned.");
    }
  }

  /**
   * Gets the dataflows metabase by id.
   *
   * @param dataflowIds the dataflow ids
   * @return the dataflows metabase by id
   */
  @Override
  @PostMapping(value = "/private/dataflows/getmetabase",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get dataflows metadata by dataflow ids", hidden = true)
  public List<DataFlowVO> getDataflowsMetabaseById(@RequestBody List<Long> dataflowIds) {
    if (dataflowIds == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATAFLOW_INCORRECT_ID);
    }
    return dataflowService.getDataflowsMetabaseById(dataflowIds);
  }

  /**
   * Checks if is user requester.
   *
   * @param dataflowId the dataflow id
   * @return true, if is user requester
   */
  private boolean isUserRequester(Long dataflowId) {
    String roleAdmin = "ROLE_" + SecurityRoleEnum.ADMIN;
    for (GrantedAuthority role : SecurityContextHolder.getContext().getAuthentication()
        .getAuthorities()) {
      if (ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(dataflowId)
          .equals(role.getAuthority())
          || ObjectAccessRoleEnum.DATAFLOW_OBSERVER.getAccessRole(dataflowId)
              .equals(role.getAuthority())
          || ObjectAccessRoleEnum.DATAFLOW_STEWARD.getAccessRole(dataflowId)
              .equals(role.getAuthority())
          || ObjectAccessRoleEnum.DATAFLOW_STEWARD_SUPPORT.getAccessRole(dataflowId)
              .equals(role.getAuthority())
          || roleAdmin.equals(role.getAuthority())) {
        return true;
      }
    }
    return false;
  }
}
