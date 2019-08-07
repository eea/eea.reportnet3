package org.eea.interfaces.controller.dataflow;


import java.util.List;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  DataFlowVO findById(@PathVariable("id") Long id);


  /**
   * Find by status.
   *
   * @param status the status
   * @return the list
   */
  @GetMapping(value = "/status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataFlowVO> findByStatus(TypeStatusEnum status);


  /**
   * Find completed.
   *
   * @param userId the user id
   * @param pageNum the page num
   * @param pageSize the page size
   * @return the list
   */
  @GetMapping(value = "/{userId}/completed", produces = MediaType.APPLICATION_JSON_VALUE)
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
  @GetMapping(value = "/{userId}/request/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataFlowVO> findUserDataflowsByStatus(@PathVariable(value = "userId") Long userId,
      @PathVariable(value = "type") TypeRequestEnum type);



  /**
   * Find pending accepted.
   *
   * @param userId the user id
   * @return the list
   */
  @GetMapping(value = "/pendingaccepted/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataFlowVO> findPendingAccepted(@PathVariable(value = "userId") Long userId);



  /**
   * Update user request.
   *
   * @param idUserRequest the id user request
   * @param type the type
   */
  @PutMapping(value = "/updateStatusRequest/{idUserRequest}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  void updateUserRequest(@PathVariable("idUserRequest") Long idUserRequest, TypeRequestEnum type);

  /**
   * Adds the contributor.
   *
   * @param idDataflow the id dataflow
   * @param userId the user id
   */
  @PostMapping(value = "/{idDataflow}/contributor/add", produces = MediaType.APPLICATION_JSON_VALUE)
  void addContributor(@PathVariable("idDataflow") Long idDataflow,
      @RequestParam(value = "idContributor") Long userId);


  /**
   * Removes the contributor.
   *
   * @param idDataflow the id dataflow
   * @param userId the user id
   */
  @DeleteMapping(value = "{idDataflow}/contributor/remove")
  void removeContributor(@PathVariable("idDataflow") Long idDataflow,
      @RequestParam(value = "idContributor") Long userId);



  /**
   * Creates the data flow.
   *
   * @param description the description
   * @param nameDataFlow the name data flow
   * @param deadDateToSend the dead date to send
   */
  @PostMapping(value = "/createDataFlow", produces = MediaType.APPLICATION_JSON_VALUE)
  void createDataFlow(@RequestBody DataFlowVO dataFlowVO);

}
