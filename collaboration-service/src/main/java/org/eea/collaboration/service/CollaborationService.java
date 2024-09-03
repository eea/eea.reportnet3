package org.eea.collaboration.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.eea.collaboration.persistence.domain.Message;
import org.eea.exception.EEAException;
import org.eea.exception.EEAForbiddenException;
import org.eea.exception.EEAIllegalArgumentException;
import org.eea.interfaces.vo.dataflow.MessagePaginatedVO;
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
   * @param user the user
   * @param jobId the jobId
   * @return the message VO
   * @throws EEAIllegalArgumentException the EEA illegal argument exception
   * @throws EEAForbiddenException the EEA forbidden exception
   */
  MessageVO createMessage(Long dataflowId, MessageVO messageVO, String user, Long jobId)
      throws EEAIllegalArgumentException, EEAForbiddenException;

  /**
   * Creates the message attachment.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param is the is
   * @param fileName the file name
   * @param fileSize the file size
   * @param contentType the content type
   * @return the message VO
   * @throws EEAIllegalArgumentException the EEA illegal argument exception
   * @throws EEAForbiddenException the EEA forbidden exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  MessageVO createMessageAttachment(Long dataflowId, Long providerId, InputStream is,
      String fileName, String fileSize, String contentType)
          throws Exception;

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
   * @return the message paginated VO
   * @throws EEAForbiddenException the EEA forbidden exception
   */
  MessagePaginatedVO findMessages(Long dataflowId, Long providerId, Boolean read, int page)
      throws EEAForbiddenException;

  /**
   * Gets the message.
   *
   * @param message the message
   * @return the message
   * @throws EEAException the EEA exception
   */
  Message getMessage(Long message) throws EEAException;


  /**
   * Gets the message attachment.
   *
   * @param messageId the message id
   * @param dataflowId the dataflow id
   * @param fileName the file name
   * @return the message attachment
   */
  byte[] getMessageAttachment(Long messageId, Long dataflowId, String fileName) throws Exception;

  /**
   * Sets the big data param
   *
   * @param messageId the message id
   * @param bigData the bigData
   * @return
   */
  void setBigData(Long messageId, Boolean bigData);
}
