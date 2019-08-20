package org.eea.dataflow.controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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


  /** The dataflow service. */
  @Autowired
  private DataflowService dataflowService;


  /**
   * Find by id.
   *
   * @param id the id
   * @return the data flow VO
   */
  @Override
  @HystrixCommand(fallbackMethod = "errorHandler")
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
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
   * Error handler data flow vo.
   *
   * @param id the id
   *
   * @return the data flow vo
   */
  public static DataFlowVO errorHandler(@PathVariable("id") final Long id) {
    final String errorMessage = String.format("Dataflow with id: %d has a problem", id);
    final DataFlowVO result = new DataFlowVO();
    result.setId(-1L);
    LOG_ERROR.error(errorMessage);
    return result;
  }


  /**
   * Error handler list.
   *
   * @param userId the user id
   * @return the list
   */
  public static List<DataFlowVO> errorHandlerList() {
    final String errorMessage = String.format("User has problems to retrieve dataflows");
    final List<DataFlowVO> results = new ArrayList<>();
    LOG_ERROR.error(errorMessage);
    return results;
  }


  /**
   * Error handler list completed.
   *
   * @param userId the user id
   * @param pageNum the page num
   * @param pageSize the page size
   * @return the list
   */
  public static List<DataFlowVO> errorHandlerListCompleted(final Integer pageNum,
      final Integer pageSize) {
    final String errorMessage = String.format(
        "User has problems to retrieve dataflows completed, form page %d with pageSize of %d",
        pageNum, pageSize);
    final List<DataFlowVO> results = new ArrayList<>();
    LOG_ERROR.error(errorMessage);
    return results;
  }

  /**
   * Find by status.
   *
   * @param status the status
   * @return the list
   */
  @Override
  @GetMapping(value = "/status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
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
  @HystrixCommand(fallbackMethod = "errorHandlerList")
  @GetMapping(value = "/pendingaccepted", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DataFlowVO> findPendingAccepted() {

    List<DataFlowVO> dataflows = new ArrayList<>();
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get("userId");
    try {
      dataflows = dataflowService.getPendingAccepted(Long.parseLong(userId));
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
  @HystrixCommand(fallbackMethod = "errorHandlerListCompleted")
  @GetMapping(value = "/completed", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DataFlowVO> findCompleted(Integer pageNum, Integer pageSize) {

    List<DataFlowVO> dataflows = new ArrayList<>();
    Pageable pageable = PageRequest.of(pageNum, pageSize);
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get("userId");
    try {
      dataflows = dataflowService.getCompleted(Long.parseLong(userId), pageable);
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
  @GetMapping(value = "/request/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DataFlowVO> findUserDataflowsByStatus(TypeRequestEnum type) {

    List<DataFlowVO> dataflows = new ArrayList<>();
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get("userId");
    try {
      dataflows = dataflowService.getPendingByUser(Long.parseLong(userId), type);
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
  @PutMapping(value = "/updateStatusRequest/{idUserRequest}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void updateUserRequest(Long idUserRequest, TypeRequestEnum type) {

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
  @PostMapping(value = "/{idDataflow}/contributor/add", produces = MediaType.APPLICATION_JSON_VALUE)
  public void addContributor(@PathVariable("idDataflow") Long idDataflow, Long userId) {

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
  @DeleteMapping(value = "{idDataflow}/contributor/remove")
  public void removeContributor(@PathVariable("idDataflow") Long idDataflow, Long userId) {
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
   */
  @Override
  @HystrixCommand(fallbackMethod = "createDataFlow")
  @PostMapping(value = "/createDataFlow", produces = MediaType.APPLICATION_JSON_VALUE)
  public void createDataFlow(@RequestBody DataFlowVO dataFlowVO) {

    final Timestamp dateToday = java.sql.Timestamp.valueOf(LocalDateTime.now());
    if (null != dataFlowVO.getDeadlineDate() && (dataFlowVO.getDeadlineDate().before(dateToday)
        || dataFlowVO.getDeadlineDate().equals(dateToday))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATE_AFTER_INCORRECT);
    }
    dataflowService.createDataFlow(dataFlowVO);
  }

}
