package org.eea.dataflow.controller;

import java.util.ArrayList;
import java.util.List;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
  @RequestMapping(value = "/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
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
   * Find by status.
   *
   * @param status the status
   * @return the list
   */
  @Override
  @RequestMapping(value = "/status/{status}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
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
   * @param userId the user id
   * @return the list
   */
  @Override
  @RequestMapping(value = "/pendingaccepted/{userId}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DataFlowVO> findPendingAccepted(Long userId) {

    List<DataFlowVO> dataflows = new ArrayList<>();
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
  @RequestMapping(value = "/completed", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DataFlowVO> findCompleted(Integer pageNum, Integer pageSize) {

    List<DataFlowVO> dataflows = new ArrayList<>();
    Pageable pageable = PageRequest.of(pageNum, pageSize);
    try {
      dataflows = dataflowService.getCompleted(pageable);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }
    return dataflows;


  }

  /**
   * Find user dataflows by status.
   *
   * @param userId the user id
   * @param type the type
   * @return the list
   */
  @Override
  @RequestMapping(value = "/{userId}/request/{type}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DataFlowVO> findUserDataflowsByStatus(Long userId, TypeRequestEnum type) {

    List<DataFlowVO> dataflows = new ArrayList<>();
    try {
      dataflows = dataflowService.getPendingByUser(userId, type);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }
    return dataflows;

  }

}
