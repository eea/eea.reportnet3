package org.eea.dataflow.controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.eea.dataflow.service.DataflowService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The type Data flow controller.
 */
@RestController
@RequestMapping(value = "/dataflow")
public class DataFlowControllerImpl implements DataFlowController {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The dataflow service.
   */
  @Autowired
  private DataflowService dataflowService;


  /**
   * Find by id.
   *
   * @param id the id
   *
   * @return the data flow VO
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#id,'DATAFLOW_PROVIDER','DATAFLOW_CUSTODIAN','DATAFLOW_REQUESTER')")
  public DataFlowVO findById(@PathVariable("id") final Long id) {

    if (id == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATAFLOW_INCORRECT_ID);
    }
    DataFlowVO result = null;
    try {
      result = dataflowService.getById(id);

    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }
    return result;
  }


  /**
   * Find by status.
   *
   * @param status the status
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR hasRole('DATA_PROVIDER') OR hasRole('DATA_REQUESTER')")
  public List<DataFlowVO> findByStatus(TypeStatusEnum status) {

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
  @GetMapping(value = "/pendingaccepted", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR hasRole('DATA_PROVIDER')")
  public List<DataFlowVO> findPendingAccepted() {

    List<DataFlowVO> dataflows = new ArrayList<>();
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get("userId");
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
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/completed", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR hasRole('DATA_PROVIDER') OR hasRole('DATA_REQUESTER')")
  public List<DataFlowVO> findCompleted(Integer pageNum, Integer pageSize) {

    List<DataFlowVO> dataflows = new ArrayList<>();
    Pageable pageable = PageRequest.of(pageNum, pageSize);
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get("userId");
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
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/request/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR hasRole('DATA_PROVIDER')")
  public List<DataFlowVO> findUserDataflowsByStatus(TypeRequestEnum type) {

    List<DataFlowVO> dataflows = new ArrayList<>();
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get("userId");
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
  @PutMapping(value = "/updateStatusRequest/{idUserRequest}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public void updateUserRequest(@PathVariable("idUserRequest") Long idUserRequest,
      TypeRequestEnum type) {

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
   * @param idDataflow the id dataflow
   * @param userId the user id
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/{idDataflow}/contributor/add", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public void addContributor(@PathVariable("idDataflow") Long idDataflow, String userId) {

    try {
      dataflowService.addContributorToDataflow(idDataflow, userId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }


  }

  /**
   * Removes the contributor.
   *
   * @param idDataflow the id dataflow
   * @param userId the user id
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "{idDataflow}/contributor/remove")
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public void removeContributor(@PathVariable("idDataflow") Long idDataflow, String userId) {
    try {
      dataflowService.removeContributorFromDataflow(idDataflow, userId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }
  }



  /**
   * Creates the data flow.
   *
   * @param dataFlowVO the data flow VO
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR hasRole('DATA_REQUESTER')")
  public ResponseEntity<?> createDataFlow(@RequestBody DataFlowVO dataFlowVO) {

    String message = "";
    HttpStatus status = HttpStatus.OK;

    final Timestamp dateToday = java.sql.Timestamp.valueOf(LocalDateTime.now());
    if (null != dataFlowVO.getDeadlineDate() && (dataFlowVO.getDeadlineDate().before(dateToday)
        || dataFlowVO.getDeadlineDate().equals(dateToday))) {

      message = EEAErrorMessage.DATE_AFTER_INCORRECT;
      status = HttpStatus.BAD_REQUEST;
    }

    if (StringUtils.isBlank(dataFlowVO.getName())
        || StringUtils.isBlank(dataFlowVO.getDescription())) {

      message = EEAErrorMessage.DATAFLOW_DESCRIPTION_NAME;
      status = HttpStatus.BAD_REQUEST;
    }

    try {
      dataflowService.createDataFlow(dataFlowVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Create dataflow failed. ", e.getCause());
      message = e.getMessage();
      status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    return new ResponseEntity<>(message, status);
  }


  /**
   * Update data flow.
   *
   * @param dataFlowVO the data flow VO
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#dataFlowVO.id,'DATAFLOW_CUSTODIAN')")
  public ResponseEntity<?> updateDataFlow(@RequestBody DataFlowVO dataFlowVO) {
    final Timestamp dateToday = java.sql.Timestamp.valueOf(LocalDateTime.now());

    String message = "";
    HttpStatus status = HttpStatus.OK;

    if (null != dataFlowVO.getDeadlineDate() && (dataFlowVO.getDeadlineDate().before(dateToday)
        || dataFlowVO.getDeadlineDate().equals(dateToday))) {
      message = EEAErrorMessage.DATE_AFTER_INCORRECT;
      status = HttpStatus.BAD_REQUEST;
    }

    if (StringUtils.isBlank(dataFlowVO.getName())
        || StringUtils.isBlank(dataFlowVO.getDescription())) {
      message = EEAErrorMessage.DATAFLOW_DESCRIPTION_NAME;
      status = HttpStatus.BAD_REQUEST;
    }

    try {
      dataflowService.updateDataFlow(dataFlowVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Update dataflow failed. ", e.getCause());
      message = e.getMessage();
      status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    return new ResponseEntity<>(message, status);
  }

  /**
   * Gets the metabase by id.
   *
   * @param id the id
   *
   * @return the metabase by id
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{id}/getmetabase", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#id,'DATAFLOW_PROVIDER','DATAFLOW_CUSTODIAN','DATAFLOW_REQUESTER')")
  public DataFlowVO getMetabaseById(@PathVariable("id") final Long id) {

    if (id == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATAFLOW_INCORRECT_ID);
    }
    DataFlowVO result = null;
    try {
      result = dataflowService.getMetabaseById(id);

    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }
    return result;
  }


  /**
   * Delete data flow.
   *
   * @param idDataflow the id dataflow
   */
  @Override
  @DeleteMapping(value = "/{idDataflow}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#idDataflow,'DATAFLOW_CUSTODIAN')")
  public void deleteDataFlow(@PathVariable("idDataflow") Long idDataflow) {
    try {
      dataflowService.deleteDataFlow(idDataflow);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Update data flow status.
   *
   * @param idDataflow the id dataflow
   * @param status the status
   */
  @Override
  @PutMapping(value = "/{id}/updateStatus", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR hasRole('DATA_PROVIDER')")
  public void updateDataFlowStatus(@PathVariable("id") Long idDataflow,
      @RequestParam(value = "status", required = true) TypeStatusEnum status) {
    try {
      dataflowService.updateDataFlowStatus(idDataflow, status);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }


}
