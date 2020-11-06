package org.eea.collaboration.service;

import java.util.List;
import org.eea.exception.EEAForbiddenException;
import org.eea.exception.EEAIllegalArgumentException;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.eea.kafka.domain.EventType;

/**
 * The Interface CollaborationService.
 */
public interface CollaborationService {

  /**
   * Creates the message.
   *
   * @param dataflowId the dataflow id
   * @param messageVO the message VO
   * @return the message VO
   * @throws EEAIllegalArgumentException the EEA illegal argument exception
   * @throws EEAForbiddenException the EEA forbidden exception
   */
  MessageVO createMessage(Long dataflowId, MessageVO messageVO)
      throws EEAIllegalArgumentException, EEAForbiddenException;

  /**
   * Update message read status.
   *
   * @param dataflowId the dataflow id
   * @param messageVOs the message V os
   * @throws EEAIllegalArgumentException the EEA illegal argument exception
   * @throws EEAForbiddenException the EEA forbidden exception
   */
  void updateMessageReadStatus(Long dataflowId, List<MessageVO> messageVOs)
      throws EEAIllegalArgumentException, EEAForbiddenException;

  /**
   * Find messages.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param read the read
   * @param page the page
   * @return the list
   * @throws EEAForbiddenException the EEA forbidden exception
   */
  List<MessageVO> findMessages(Long dataflowId, Long providerId, Boolean read, int page)
      throws EEAForbiddenException;

  /**
   * Notify new messages.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param eventType the event type
   */
  void notifyNewMessages(Long dataflowId, Long providerId, EventType eventType);
}
