package org.eea.collaboration.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.eea.collaboration.persistence.domain.Message;
import org.eea.collaboration.service.CollaborationService;
import org.eea.collaboration.service.helper.CollaborationServiceHelper;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.exception.EEAForbiddenException;
import org.eea.exception.EEAIllegalArgumentException;
import org.eea.interfaces.controller.collaboration.CollaborationController;
import org.eea.interfaces.vo.dataflow.MessageAttachmentVO;
import org.eea.interfaces.vo.dataflow.MessagePaginatedVO;
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
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

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
  @HystrixCommand
  @PostMapping("/createMessage/dataflow/{dataflowId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId, 'DATAFLOW_STEWARD', 'DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER', 'DATAFLOW_REPORTER_READ', 'DATAFLOW_REPORTER_WRITE')")
  @ApiOperation(value = "Creates a new message assigned to a Dataflow", hidden = true,
      response = MessageVO.class)
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error creating message"),
      @ApiResponse(code = 403, message = "Error creating message")})
  public MessageVO createMessage(
      @ApiParam(value = "Dataflow Id you're assigning the message to",
          example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(value = "Message Object") @RequestBody MessageVO messageVO) {
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
  @HystrixCommand(commandProperties = {
      @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "65000")})
  @PostMapping("/createMessage/dataflow/{dataflowId}/attachment")
  @PreAuthorize("secondLevelAuthorize(#dataflowId, 'DATAFLOW_STEWARD', 'DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER', 'DATAFLOW_REPORTER_READ', 'DATAFLOW_REPORTER_WRITE')")
  @ApiOperation(value = "Creates a new message attachment assigned to a Dataflow and a provider Id",
      response = MessageVO.class, hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 400, message = EEAErrorMessage.FILE_FORMAT),
      @ApiResponse(code = 403, message = "Error creating message attachment"),
      @ApiResponse(code = 500, message = "Internal server error creating message attachment")})
  public MessageVO createMessageAttachment(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(value = "Provider Id", example = "0") @RequestParam("providerId") Long providerId,
      @ApiParam(
          value = "The file which is going to be attached") @RequestPart("fileAttachment") MultipartFile fileAttachment) {
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
          fileSize, fileAttachment.getContentType());
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
  @HystrixCommand
  @PutMapping("/updateMessageReadStatus/dataflow/{dataflowId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId, 'DATAFLOW_STEWARD', 'DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER', 'DATAFLOW_REPORTER_READ', 'DATAFLOW_REPORTER_WRITE')")
  @ApiOperation(value = "Updates the message read status", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error updating the message"),
      @ApiResponse(code = 500, message = "Error updating the message")})
  public void updateMessageReadStatus(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(value = "Message Object to be updated") @RequestBody List<MessageVO> messageVOs) {
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
  @HystrixCommand
  @DeleteMapping("/deleteMessage/dataflow/{dataflowId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId, 'DATAFLOW_STEWARD', 'DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER', 'DATAFLOW_REPORTER_READ', 'DATAFLOW_REPORTER_WRITE')")
  @ApiOperation(value = "Deletes the message", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error deleting the message"),
      @ApiResponse(code = 404, message = "Error deleting the message")})
  public void deleteMessage(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(value = "Provider Id", example = "0") @RequestParam("providerId") Long providerId,
      @ApiParam(value = "Message Id", example = "0") @RequestParam("messageId") Long messageId) {
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
   * @return the message paginated VO
   */
  @Override
  @HystrixCommand
  @GetMapping("/findMessages/dataflow/{dataflowId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId, 'DATAFLOW_STEWARD', 'DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER', 'DATAFLOW_REPORTER_READ', 'DATAFLOW_REPORTER_WRITE')")
  @ApiOperation(value = "Gets all the messages assigned to a Dataflow and a Provider",
      hidden = true)
  @ApiResponse(code = 403, message = "Error finding the messages.")
  public MessagePaginatedVO findMessages(@PathVariable("dataflowId") Long dataflowId,
      @ApiParam(value = "Provider Id", example = "0") @RequestParam("providerId") Long providerId,
      @ApiParam(value = "Searching for read messages?", example = "true",
          required = false) @RequestParam(value = "read", required = false) Boolean read,
      @ApiParam(value = "Page number where the messaged are located",
          example = "0") @RequestParam("page") int page) {
    try {
      MessagePaginatedVO messagePaginatedVO =
          collaborationService.findMessages(dataflowId, providerId, read, page);
      List<MessageVO> listMessageVO = messagePaginatedVO.getListMessage();

      for (MessageVO messageVO : listMessageVO) {
        if (messageVO.getType() == MessageTypeEnum.ATTACHMENT) {
          MessageAttachmentVO messageAttachmentVO = new MessageAttachmentVO();
          messageAttachmentVO.setName(messageVO.getContent());
          String fileName = messageVO.getContent();
          int indexExtension = fileName.lastIndexOf(".");
          String extension = fileName.substring(indexExtension + 1, fileName.length());
          messageAttachmentVO.setExtension(extension);
          messageAttachmentVO.setSize(messageVO.getFileSize());
          messageVO.setMessageAttachment(messageAttachmentVO);
        }
      }
      return messagePaginatedVO;
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
  @HystrixCommand(commandProperties = {
      @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "65000")})
  @GetMapping("/findMessages/dataflow/{dataflowId}/getMessageAttachment")
  @PreAuthorize("secondLevelAuthorize(#dataflowId, 'DATAFLOW_STEWARD', 'DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER', 'DATAFLOW_REPORTER_READ', 'DATAFLOW_REPORTER_WRITE')")
  @ApiOperation(value = "Gets the attachment assigned to a message", hidden = true)
  @ApiResponse(code = 404, message = "Error getting the message attachment")
  public ResponseEntity<byte[]> getMessageAttachment(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(value = "Provider Id", example = "0") @RequestParam("providerId") Long providerId,
      @ApiParam(value = "Message Id", example = "0") @RequestParam("messageId") Long messageId) {


    LOG.info("Downloading message attachment from the dataflowId {}", dataflowId);
    try {
      Message messageAttachment = collaborationService.getMessage(messageId);
      String filename = messageAttachment.getContent();
      byte[] file = collaborationService.getMessageAttachment(messageId, dataflowId, filename);
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
  @HystrixCommand
  @GetMapping("/private/notifyNewMessages")
  @ApiOperation(value = "Notifies about a new message", hidden = true)
  public void notifyNewMessages(@RequestParam("dataflowId") Long dataflowId,
      @ApiParam(value = "Provider Id", example = "0") @RequestParam("providerId") Long providerId,
      @ApiParam(value = "Modified Dataset Id",
          example = "0") @RequestParam("modifiedDatasetId") Long modifiedDatasetId,
      @ApiParam(value = "The Dataset Status",
          example = "RELEASED") @RequestParam("datasetStatus") DatasetStatusEnum datasetStatus,
      @ApiParam(value = "Dataset name",
          example = "Im A Dataset") @RequestParam("datasetName") String datasetName,
      @ApiParam(value = "Event type") @RequestParam("eventType") String eventType) {
    collaborationServiceHelper.notifyNewMessages(dataflowId, providerId, modifiedDatasetId,
        datasetStatus, datasetName, eventType);
  }
}
