package org.eea.interfaces.controller.collaboration;

import java.util.List;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Interface CollaborationController.
 */
public interface CollaborationController {

  /**
   * The Interface CollaborationControllerZuul.
   */
  @FeignClient(value = "collaboration", path = "/collaboration")
  interface CollaborationControllerZuul extends CollaborationController {

  }

  /**
   * Creates the message.
   *
   * @param dataflowId the dataflow id
   * @param messageVO the message VO
   * @return the message VO
   */
  @PostMapping("/createMessage/dataflow/{dataflowId}")
  MessageVO createMessage(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody MessageVO messageVO);

  /**
   * Update message read status.
   *
   * @param dataflowId the dataflow id
   * @param messageVOs the message V os
   */
  @PutMapping("/updateMessageReadStatus/dataflow/{dataflowId}")
  void updateMessageReadStatus(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody List<MessageVO> messageVOs);

  /**
   * Find messages.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param read the read
   * @param page the page
   * @return the list
   */
  @GetMapping("/findMessages/dataflow/{dataflowId}")
  List<MessageVO> findMessages(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("providerId") Long providerId,
      @RequestParam(value = "read", required = false) Boolean read, @RequestParam("page") int page);

  /**
   * Notify new messages.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param eventType the event type
   */
  @GetMapping("/private/notifyNewMessages")
  void notifyNewMessages(@RequestParam("dataflowId") Long dataflowId,
      @RequestParam("providerId") Long providerId, @RequestParam("eventType") String eventType);
}
