package org.eea.collaboration.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.eea.collaboration.persistence.domain.MessageAttachment;
import org.eea.collaboration.service.CollaborationService;
import org.eea.collaboration.service.helper.CollaborationServiceHelper;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.exception.EEAForbiddenException;
import org.eea.exception.EEAIllegalArgumentException;
import org.eea.interfaces.controller.collaboration.CollaborationController;
import org.eea.interfaces.vo.dataflow.MessageAttachmentVO;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import org.eea.interfaces.vo.dataset.enums.MessageTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The Class CollaborationControllerImpl.
 */
@RestController
@RequestMapping("/collaboration")
public class CollaborationControllerImpl implements CollaborationController {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(CollaborationControllerImpl.class);

  /** The collaboration service. */
  @Autowired
  private CollaborationService collaborationService;

  /** The collaboration service helper. */
  @Autowired
  private CollaborationServiceHelper collaborationServiceHelper;

  /**
   * Creates the message.
   *
   * @param dataflowId the dataflow id
   * @param messageVO the message VO
   * @return the message VO
   */
  @Override
  @PostMapping("/createMessage/dataflow/{dataflowId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId, 'DATAFLOW_STEWARD', 'DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER', 'DATAFLOW_REPORTER_READ', 'DATAFLOW_REPORTER_WRITE')")
  public MessageVO createMessage(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody MessageVO messageVO) {
    try {
      return collaborationService.createMessage(dataflowId, messageVO);
    } catch (EEAIllegalArgumentException e) {
      LOG_ERROR.error("Error creating message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    } catch (EEAForbiddenException e) {
      LOG_ERROR.error("Error creating message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
    }
  }

  /**
   * Creates the message attachment.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param fileAttachment the file attachment
   * @return the message VO
   */
  @Override
  @HystrixCommand
  @PostMapping("/createMessage/dataflow/{dataflowId}/attachment")
  @PreAuthorize("secondLevelAuthorize(#dataflowId, 'DATAFLOW_STEWARD', 'DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER', 'DATAFLOW_REPORTER_READ', 'DATAFLOW_REPORTER_WRITE')")
  public MessageVO createMessageAttachment(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("providerId") Long providerId,
      @RequestPart("fileAttachment") MultipartFile fileAttachment) {
    try {
      if (providerId == null || dataflowId == null || fileAttachment == null
          || fileAttachment.getOriginalFilename() == null) {
        throw new EEAIllegalArgumentException(EEAErrorMessage.MESSAGING_BAD_REQUEST);
      }
      if (fileAttachment.getSize() > 20971520) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FILE_FORMAT);
      }
      InputStream is = fileAttachment.getInputStream();
      // Remove comma "," character to avoid error with special characters
      String fileName = fileAttachment.getOriginalFilename().replace(",", "");
      String fileSize = String.valueOf(fileAttachment.getSize());
      return collaborationService.createMessageAttachment(dataflowId, providerId, is, fileName,
          fileSize);
    } catch (EEAIllegalArgumentException e) {
      LOG_ERROR.error("Error creating message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    } catch (EEAForbiddenException e) {
      LOG_ERROR.error("Error creating message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
    } catch (IOException e) {
      LOG_ERROR.error("Error saving message attachment from the dataflowId {}, with message: {}",
          dataflowId, e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Update message read status.
   *
   * @param dataflowId the dataflow id
   * @param messageVOs the message V os
   */
  @Override
  @PutMapping("/updateMessageReadStatus/dataflow/{dataflowId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId, 'DATAFLOW_STEWARD', 'DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER', 'DATAFLOW_REPORTER_READ', 'DATAFLOW_REPORTER_WRITE')")
  public void updateMessageReadStatus(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody List<MessageVO> messageVOs) {
    try {
      collaborationService.updateMessageReadStatus(dataflowId, messageVOs);
    } catch (EEAIllegalArgumentException e) {
      LOG_ERROR.error("Error updating messages: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    } catch (EEAForbiddenException e) {
      LOG_ERROR.error("Error updating messages: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
    }
  }

  /**
   * Delete message.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param messageId the message id
   */
  @Override
  @DeleteMapping("/deleteMessage/dataflow/{dataflowId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId, 'DATAFLOW_STEWARD', 'DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER', 'DATAFLOW_REPORTER_READ', 'DATAFLOW_REPORTER_WRITE')")
  public void deleteMessage(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("providerId") Long providerId, @RequestParam("messageId") Long messageId) {
    try {
      if (providerId == null || dataflowId == null || messageId == null) {
        throw new EEAIllegalArgumentException(EEAErrorMessage.MESSAGING_BAD_REQUEST);
      }
      collaborationService.deleteMessage(messageId);

    } catch (EEAIllegalArgumentException e) {
      LOG_ERROR.error("Error deleting message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    } catch (EEAException e) {
      LOG_ERROR.error("Error deleting message, messageId {}, with message: {}", messageId,
          e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }
  }

  /**
   * Find messages.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param read the read
   * @param page the page
   * @return the list
   */
  @Override
  @GetMapping("/findMessages/dataflow/{dataflowId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId, 'DATAFLOW_STEWARD', 'DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER', 'DATAFLOW_REPORTER_READ', 'DATAFLOW_REPORTER_WRITE')")
  public List<MessageVO> findMessages(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("providerId") Long providerId,
      @RequestParam(value = "read", required = false) Boolean read,
      @RequestParam("page") int page) {
    try {
      List<MessageVO> listMessageVO =
          collaborationService.findMessages(dataflowId, providerId, read, page);
      for (MessageVO messageVO : listMessageVO) {
        if (messageVO.getType() == MessageTypeEnum.ATTACHMENT) {
          try {
            MessageAttachment messageAttachment =
                collaborationService.getMessageAttachment(messageVO.getId());
            MessageAttachmentVO messageAttachmentVO = new MessageAttachmentVO();
            messageAttachmentVO.setName(messageAttachment.getFileName());
            String fileName = messageAttachment.getFileName();
            int indexExtension = fileName.lastIndexOf(".");
            String extension = fileName.substring(indexExtension + 1, fileName.length());
            messageAttachmentVO.setExtension(extension);
            messageAttachmentVO.setSize(messageAttachment.getFileSize());
            messageVO.setMessageAttachmentVO(messageAttachmentVO);
          } catch (EEAException e) {
            LOG_ERROR.error("Error retrieving message info from the messageId {}, with message: {}",
                messageVO.getId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
          }
        }
      }
      return listMessageVO;
    } catch (EEAForbiddenException e) {
      LOG_ERROR.error("Error finding messages: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
    }
  }

  /**
   * Gets the message attachment.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param messageId the message id
   * @return the message attachment
   */
  @Override
  @GetMapping("/findMessages/dataflow/{dataflowId}/getMessageAttachment")
  @PreAuthorize("secondLevelAuthorize(#dataflowId, 'DATAFLOW_STEWARD', 'DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER', 'DATAFLOW_REPORTER_READ', 'DATAFLOW_REPORTER_WRITE')")
  public ResponseEntity<byte[]> getMessageAttachment(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("providerId") Long providerId, @RequestParam("messageId") Long messageId) {


    LOG.info("Downloading message attachment from the dataflowId {}", dataflowId);
    try {
      MessageAttachment messageAttachment = collaborationService.getMessageAttachment(messageId);
      byte[] file = messageAttachment.getContent();
      String filename = messageAttachment.getFileName();
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
      return new ResponseEntity<>(file, httpHeaders, HttpStatus.OK);
    } catch (EEAException e) {
      LOG_ERROR.error(
          "Error downloading message attachment from the dataflowId {}, with message: {}",
          dataflowId, e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }

  }


  /**
   * Notify new messages.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param modifiedDatasetId the modified dataset id
   * @param datasetStatus the dataset status
   * @param datasetName the dataset name
   * @param eventType the event type
   */
  @Override
  @GetMapping("/private/notifyNewMessages")
  public void notifyNewMessages(@RequestParam("dataflowId") Long dataflowId,
      @RequestParam("providerId") Long providerId,
      @RequestParam("modifiedDatasetId") Long modifiedDatasetId,
      @RequestParam("datasetStatus") DatasetStatusEnum datasetStatus,
      @RequestParam("datasetName") String datasetName,
      @RequestParam("eventType") String eventType) {
    collaborationServiceHelper.notifyNewMessages(dataflowId, providerId, modifiedDatasetId,
        datasetStatus, datasetName, eventType);
  }
}
