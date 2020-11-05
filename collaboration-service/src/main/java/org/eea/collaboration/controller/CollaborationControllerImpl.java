package org.eea.collaboration.controller;

import java.util.List;
import org.eea.collaboration.service.CollaborationService;
import org.eea.exception.EEAForbiddenException;
import org.eea.exception.EEAIllegalArgumentException;
import org.eea.interfaces.controller.collaboration.CollaborationController;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class CollaborationControllerImpl.
 */
@RestController
@RequestMapping("/collaboration")
public class CollaborationControllerImpl implements CollaborationController {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The collaboration service. */
  @Autowired
  private CollaborationService collaborationService;

  /**
   * Creates the message.
   *
   * @param dataflowId the dataflow id
   * @param messageVO the message VO
   * @return the message VO
   */
  @Override
  @PostMapping("/{dataflowId}/createMessage")
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
   * Update message read status.
   *
   * @param dataflowId the dataflow id
   * @param messageVOs the message V os
   */
  @Override
  @PutMapping("/{dataflowId}/updateMessageReadStatus")
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
   * Find messages.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param read the read
   * @param page the page
   * @return the list
   */
  @Override
  @GetMapping("/{dataflowId}/findMessages")
  @PreAuthorize("secondLevelAuthorize(#dataflowId, 'DATAFLOW_STEWARD', 'DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER', 'DATAFLOW_REPORTER_READ', 'DATAFLOW_REPORTER_WRITE')")
  public List<MessageVO> findMessages(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("providerId") Long providerId,
      @RequestParam(value = "read", required = false) Boolean read,
      @RequestParam("page") int page) {
    try {
      return collaborationService.findMessages(dataflowId, providerId, read, page);
    } catch (EEAForbiddenException e) {
      LOG_ERROR.error("Error finding messages: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
    }
  }
}
