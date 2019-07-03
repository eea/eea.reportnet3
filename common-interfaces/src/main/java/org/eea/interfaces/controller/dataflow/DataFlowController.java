package org.eea.interfaces.controller.dataflow;


import java.util.List;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The interface Data flow controller.
 */
public interface DataFlowController {

  /**
   * The Interface DataFlowControllerZuul.
   */
  @FeignClient(value = "dataflow", path = "/dataflow")
  interface DataFlowControllerZuul extends DataFlowController {

  }

  /**
   * Find by id data flow vo.
   *
   * @param id the id
   *
   * @return the data flow vo
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  DataFlowVO findById(@PathVariable("id") Long id);


  /**
   * Find by status.
   *
   * @param status the status
   * @return the list
   */
  @RequestMapping(value = "/status/{status}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataFlowVO> findByStatus(TypeStatusEnum status);


  /**
   * Find completed.
   *
   * @param pageNum the page num
   * @param pageSize the page size
   * @return the list
   */
  @RequestMapping(value = "/{userId}/completed", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataFlowVO> findCompleted(@PathVariable(value = "userId") Long userId,
      @RequestParam(value = "pageNum", defaultValue = "0") Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize);


  /**
   * Find user dataflows by status.
   *
   * @param userId the user id
   * @param type the type
   * @return the list
   */
  @RequestMapping(value = "/{userId}/request/{type}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataFlowVO> findUserDataflowsByStatus(@PathVariable(value = "userId") Long userId,
      @PathVariable(value = "type") TypeRequestEnum type);



  /**
   * Find pending accepted.
   *
   * @param userId the user id
   * @return the list
   */
  @RequestMapping(value = "/pendingaccepted/{userId}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataFlowVO> findPendingAccepted(@PathVariable(value = "userId") Long userId);

}
