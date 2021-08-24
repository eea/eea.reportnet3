package org.eea.collaboration.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.eea.collaboration.persistence.domain.MessageAttachment;
import org.eea.exception.EEAException;
import org.eea.exception.EEAForbiddenException;
import org.eea.exception.EEAIllegalArgumentException;
import org.eea.interfaces.vo.dataflow.MessageVO;

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
   * Creates the message attachment.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param fileAttachment the file attachment
   * @return the message VO
   * @throws EEAIllegalArgumentException the EEA illegal argument exception
   * @throws EEAForbiddenException the EEA forbidden exception
   */
  MessageVO createMessageAttachment(Long dataflowId, Long providerId, InputStream is,
      String fileName, String fileSize)
      throws EEAIllegalArgumentException, EEAForbiddenException, IOException;

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
   * Delete message.
   *
   * @param messageId the message id
   * @throws EEAException the EEA exception
   */
  void deleteMessage(Long messageId) throws EEAException;

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
   * Gets the message attachment.
   *
   * @param messageAttachmentId the message attachment id
   * @return the message attachment
   * @throws EEAException the EEA exception
   */
  MessageAttachment getMessageAttachment(Long messageAttachmentId) throws EEAException;
}
