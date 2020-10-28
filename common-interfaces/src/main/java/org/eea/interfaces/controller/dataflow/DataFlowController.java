package org.eea.interfaces.controller.dataflow;

import java.util.Date;
import java.util.List;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Interface DataFlowController.
 */
public interface DataFlowController {

  /**
   * The Interface DataFlowControllerZuul.
   */
  @FeignClient(value = "dataflow", path = "/dataflow")
  interface DataFlowControllerZuul extends DataFlowController {

  }

  /**
   * Find by id.
   *
   * @param dataflowId the dataflow id
   * @return the data flow VO
   */
  @GetMapping(value = "/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  DataFlowVO findById(@PathVariable("dataflowId") Long dataflowId);

  /**
   * Find by status.
   *
   * @param status the status
   * @return the list
   */
  @GetMapping(value = "/status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataFlowVO> findByStatus(@PathVariable("status") TypeStatusEnum status);

  /**
   * Find completed.
   *
   * @param pageNum the page num
   * @param pageSize the page size
   * @return the list
   */
  @GetMapping(value = "/completed", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataFlowVO> findCompleted(
      @RequestParam(value = "pageNum", defaultValue = "0") Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize);

  /**
   * Find user dataflows by status.
   *
   * @param type the type
   * @return the list
   */
  @GetMapping(value = "/request/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataFlowVO> findUserDataflowsByStatus(@PathVariable("type") TypeRequestEnum type);

  /**
   * Find pending accepted.
   *
   * @return the list
   */
  @GetMapping(value = "/pendingaccepted", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataFlowVO> findPendingAccepted();

  /**
   * Update user request.
   *
   * @param idUserRequest the id user request
   * @param type the type
   */
  @PutMapping("/updateStatusRequest/{idUserRequest}")
  void updateUserRequest(@PathVariable("idUserRequest") Long idUserRequest,
      @RequestParam("type") TypeRequestEnum type);

  /**
   * Adds the contributor.
   *
   * @param dataflowId the dataflow id
   * @param idContributor the id contributor
   */
  @PostMapping("/{dataflowId}/contributor/add")
  void addContributor(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("idContributor") String idContributor);

  /**
   * Removes the contributor.
   *
   * @param dataflowId the dataflow id
   * @param idContributor the id contributor
   */
  @DeleteMapping("{dataflowId}/contributor/remove")
  void removeContributor(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("idContributor") String idContributor);

  /**
   * Creates the data flow.
   *
   * @param dataFlowVO the data flow VO
   * @return the response entity
   */
  @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity createDataFlow(@RequestBody DataFlowVO dataFlowVO);

  /**
   * Update data flow.
   *
   * @param dataFlowVO the data flow VO
   * @return the response entity
   */
  @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity updateDataFlow(@RequestBody DataFlowVO dataFlowVO);

  /**
   * Gets the metabase by id.
   *
   * @param dataflowId the dataflow id
   * @return the metabase by id
   */
  @GetMapping(value = "/{dataflowId}/getmetabase", produces = MediaType.APPLICATION_JSON_VALUE)
  DataFlowVO getMetabaseById(@PathVariable("dataflowId") Long dataflowId);

  /**
   * Delete data flow.
   *
   * @param dataflowId the dataflow id
   */
  @DeleteMapping("/{dataflowId}")
  void deleteDataFlow(@PathVariable("dataflowId") Long dataflowId);

  /**
   * Update data flow status.
   *
   * @param dataflowId the dataflow id
   * @param status the status
   * @param deadLineDate the dead line date
   */
  @PutMapping("/{dataflowId}/updateStatus")
  void updateDataFlowStatus(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("status") TypeStatusEnum status,
      @RequestParam(value = "deadLineDate", required = false) Date deadLineDate);

  /**
   * Creates the message.
   *
   * @param dataflowId the dataflow id
   * @param messageVO the message VO
   * @return the message VO
   */
  @PostMapping("/{dataflowId}/createMessage")
  MessageVO createMessage(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody MessageVO messageVO);

  /**
   * Find messages.
   *
   * @param dataflowId the dataflow id
   * @param read the read
   * @param page the offset
   * @return the list
   */
  @GetMapping("/{dataflowId}/findMessages")
  List<MessageVO> findMessages(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam(value = "read", required = false) Boolean read, @RequestParam("page") int page);

  /**
   * Update message read status.
   *
   * @param dataflowId the dataflow id
   * @param messageVO the message VO
   * @return true, if successful
   */
  @PutMapping("/{dataflowId}/updateMessageReadStatus")
  boolean updateMessageReadStatus(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody MessageVO messageVO);
}
