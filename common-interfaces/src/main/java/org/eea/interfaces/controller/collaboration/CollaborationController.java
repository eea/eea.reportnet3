package org.eea.interfaces.controller.collaboration;

import java.util.List;
import org.eea.interfaces.vo.dataflow.MessagePaginatedVO;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

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
   * @param user the user
   * @param jobId the jobId
   * @return the message VO
   */
  @PostMapping("/createMessage/dataflow/{dataflowId}")
  MessageVO createMessage(@PathVariable("dataflowId") Long dataflowId, @RequestBody MessageVO messageVO,
                          @RequestParam(required = false) String user, @RequestParam(required = false) Long jobId);

  /**
   * Creates the message attachment.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param fileAttachment the file attachment
   * @return the message VO
   */
  @PostMapping("/createMessage/dataflow/{dataflowId}/attachment")
  MessageVO createMessageAttachment(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("providerId") Long providerId,
      @RequestPart("fileAttachment") MultipartFile fileAttachment) throws Exception;

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
   * Delete message.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param messageId the message id
   */
  @DeleteMapping("/deleteMessage/dataflow/{dataflowId}")
  void deleteMessage(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("providerId") Long providerId, @RequestParam("messageId") Long messageId);

  /**
   * Find messages.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param read the read
   * @param page the page
   * @return the message paginated VO
   */
  @GetMapping("/findMessages/dataflow/{dataflowId}")
  MessagePaginatedVO findMessages(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("providerId") Long providerId,
      @RequestParam(value = "read", required = false) Boolean read, @RequestParam("page") int page);

  /**
   * Gets the message attachment.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param messageAttachmentId the message attachment id
   * @return the message attachment
   */
  @GetMapping("/findMessages/dataflow/{dataflowId}/getMessageAttachment")
  ResponseEntity<byte[]> getMessageAttachment(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("providerId") Long providerId,
      @RequestParam("messageAttachmentId") Long messageAttachmentId) throws Exception;

  /**
   * Notify new messages.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param custodianUserName the custodian's userName
   * @param modifiedDatasetId the modified dataset id
   * @param datasetStatus the dataset status
   * @param datasetName the dataset name
   * @param eventType the event type
   */
  @GetMapping("/private/notifyNewMessages")
  void notifyNewMessages(@RequestParam("dataflowId") Long dataflowId,
      @RequestParam("providerId") Long providerId,
      @RequestParam(value = "custodianUserName", required = false) String custodianUserName,
      @RequestParam("modifiedDatasetId") Long modifiedDatasetId,
      @RequestParam("datasetStatus") DatasetStatusEnum datasetStatus,
      @RequestParam("datasetName") String datasetName, @RequestParam("eventType") String eventType);

  @GetMapping("/private/getMessageById/{messageId}")
  MessageVO getMessage(@PathVariable("messageId") Long messageId) throws Exception;

  @PutMapping("/private/setBigData/{messageId}")
  void setBigData(@PathVariable("messageId") Long messageId, @RequestParam("bigData") Boolean bigData) throws Exception;
}
