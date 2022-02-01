package org.eea.collaboration.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eea.collaboration.mapper.MessageMapper;
import org.eea.collaboration.persistence.domain.Message;
import org.eea.collaboration.persistence.repository.MessageRepository;
import org.eea.collaboration.service.CollaborationService;
import org.eea.collaboration.service.helper.CollaborationServiceHelper;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.exception.EEAForbiddenException;
import org.eea.exception.EEAIllegalArgumentException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.document.DocumentController.DocumentControllerZuul;
import org.eea.interfaces.vo.dataflow.MessageAttachmentVO;
import org.eea.interfaces.vo.dataflow.MessagePaginatedVO;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.eea.interfaces.vo.dataset.enums.MessageTypeEnum;
import org.eea.kafka.domain.EventType;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * The Class CollaborationServiceImpl.
 */
@Service
public class CollaborationServiceImpl implements CollaborationService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(CollaborationServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The max message length. */
  @Value("${spring.health.db.check.frequency}")
  private int maxMessageLength;

  /** The dataset metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /** The kafka sender utils. */
  @Autowired
  private CollaborationServiceHelper collaborationServiceHelper;

  /** The message repository. */
  @Autowired
  private MessageRepository messageRepository;


  /** The message mapper. */
  @Autowired
  private MessageMapper messageMapper;


  /** The document controller zuul. */
  @Autowired
  private DocumentControllerZuul documentControllerZuul;

  /**
   * Creates the message.
   *
   * @param dataflowId the dataflow id
   * @param messageVO the message VO
   * @return the message VO
   * @throws EEAForbiddenException the EEA forbidden exception
   * @throws EEAIllegalArgumentException the EEA illegal argument exception
   */
  @Override
  public MessageVO createMessage(Long dataflowId, MessageVO messageVO)
      throws EEAForbiddenException, EEAIllegalArgumentException {

    Long providerId = messageVO.getProviderId();
    String content = messageVO.getContent();

    if (providerId == null || content == null || content.isEmpty()) {
      throw new EEAIllegalArgumentException(EEAErrorMessage.MESSAGING_BAD_REQUEST);
    }

    String userName = SecurityContextHolder.getContext().getAuthentication().getName();
    boolean direction = authorizeAndGetDirection(dataflowId, providerId);

    if (content.length() > maxMessageLength) {
      content = content.substring(0, maxMessageLength);
    }

    Message message = new Message();
    message.setContent(content);
    message.setDataflowId(dataflowId);
    message.setProviderId(providerId);
    message.setDate(new Date());
    message.setRead(false);
    message.setUserName(userName);
    message.setDirection(direction);
    message.setType(MessageTypeEnum.TEXT);
    message.setAutomatic(messageVO.isAutomatic());
    message.setFileSize("0");
    message = messageRepository.save(message);

    String eventType = EventType.RECEIVED_MESSAGE.toString();
    collaborationServiceHelper.notifyNewMessages(dataflowId, providerId, null, null, null,
        eventType);

    LOG.info("Message created: message={}", message);
    return messageMapper.entityToClass(message);
  }


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
   * @throws EEAForbiddenException the EEA forbidden exception
   * @throws EEAIllegalArgumentException the EEA illegal argument exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @Transactional
  public MessageVO createMessageAttachment(Long dataflowId, Long providerId, InputStream is,
      String fileName, String fileSize, String contentType)
      throws EEAForbiddenException, EEAIllegalArgumentException, IOException {

    String userName = SecurityContextHolder.getContext().getAuthentication().getName();
    boolean direction = authorizeAndGetDirection(dataflowId, providerId);
    MessageVO messageVO = new MessageVO();

    try {
      Message message = new Message();
      message.setContent(fileName);
      message.setDataflowId(dataflowId);
      message.setProviderId(providerId);
      message.setDate(new Date());
      message.setRead(false);
      message.setUserName(userName);
      message.setDirection(direction);
      message.setType(MessageTypeEnum.ATTACHMENT);
      message.setFileSize(fileSize);
      message = messageRepository.save(message);

      String extension = FilenameUtils.getExtension(fileName);

      MessageAttachmentVO messageAttachmentVO = new MessageAttachmentVO();
      messageAttachmentVO.setName(fileName);
      messageAttachmentVO.setSize(fileSize);
      messageAttachmentVO.setExtension(extension);

      messageVO = messageMapper.entityToClass(message);
      messageVO.setMessageAttachment(messageAttachmentVO);

      documentControllerZuul.uploadCollaborationDocument(IOUtils.toByteArray(is), dataflowId,
          fileName, contentType, message.getId());

      String eventType = EventType.RECEIVED_MESSAGE.toString();
      collaborationServiceHelper.notifyNewMessages(dataflowId, providerId, null, null, null,
          eventType);

      LOG.info("Message created: message={}", message);
    } finally {
      is.close();
    }

    return messageVO;
  }

  /**
   * Update message read status.
   *
   * @param dataflowId the dataflow id
   * @param messageVOs the message V os
   * @throws EEAIllegalArgumentException the EEA illegal argument exception
   * @throws EEAForbiddenException the EEA forbidden exception
   */
  @Override
  @Transactional
  public void updateMessageReadStatus(Long dataflowId, List<MessageVO> messageVOs)
      throws EEAIllegalArgumentException, EEAForbiddenException {

    List<Message> messages;
    List<Long> providerIds;
    Map<Long, Boolean> messageMap = buildMessageMap(messageVOs);

    messages = messageRepository.findByDataflowIdAndIdIn(dataflowId, messageMap.keySet());
    int previousSize = messages.size();
    Stream<Message> stream = messages.stream();

    Collection<? extends GrantedAuthority> authorities =
        SecurityContextHolder.getContext().getAuthentication().getAuthorities();

    // Case: DATAFLOW_STEWARD or DATAFLOW_CUSTODIAN
    // Allowed messages to update: direction=TRUE and providerId=ANY
    if (authorities.contains(
        new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_STEWARD.getAccessRole(dataflowId)))
        || authorities.contains(new SimpleGrantedAuthority(
            ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(dataflowId)))
        || authorities.contains(new SimpleGrantedAuthority(
            ObjectAccessRoleEnum.DATAFLOW_STEWARD_SUPPORT.getAccessRole(dataflowId)))) {
      stream = stream.filter(Message::isDirection);
    }

    // Case: DATAFLOW_LEAD_REPORTER, DATAFLOW_REPORTER_READ, DATAFLOW_REPOTER_WRITE
    // Allowed messages to update: direction=FALSE and providerId=USER_DEPENDANT
    else {
      providerIds = datasetMetabaseControllerZuul.getUserProviderIdsByDataflowId(dataflowId);
      stream = stream.filter(m -> !m.isDirection() && providerIds.contains(m.getProviderId()));
    }

    messages = stream.collect(Collectors.toList());

    if (previousSize != messages.size()) {
      LOG_ERROR.error("Messaging authorization failed: not allowed to update all messages");
      throw new EEAForbiddenException(EEAErrorMessage.MESSAGING_AUTHORIZATION_FAILED);
    }

    messages.forEach(message -> message.setRead(messageMap.get(message.getId())));
    messageRepository.saveAll(messages);
  }

  /**
   * Delete message.
   *
   * @param messageId the message id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void deleteMessage(Long messageId) throws EEAException {
    try {
      Message message = messageRepository.findById(messageId).orElseThrow(EEAException::new);
      if (message != null) {
        if (MessageTypeEnum.ATTACHMENT.equals(message.getType())) {
          documentControllerZuul.deleteCollaborationDocument(message.getDataflowId(),
              message.getContent(), messageId);
        }
        messageRepository.delete(message);
      }
    } catch (Exception e) {
      LOG_ERROR.error("Error deleting message {}", e.getMessage());
      throw new EEAIllegalArgumentException(EEAErrorMessage.MESSAGE_INCORRECT_ID);
    }
    LOG.info("Message {} deleted", messageId);
  }

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
  @Override
  public MessagePaginatedVO findMessages(Long dataflowId, Long providerId, Boolean read, int page)
      throws EEAForbiddenException {

    authorizeAndGetDirection(dataflowId, providerId);
    PageRequest pageRequest = PageRequest.of(page, 50, Sort.by("date").descending());

    MessagePaginatedVO messagePaginatedVO = new MessagePaginatedVO();
    messagePaginatedVO.setListMessage(null != read
        ? messageMapper.entityListToClass(messageRepository
            .findByDataflowIdAndProviderIdAndRead(dataflowId, providerId, read, pageRequest)
            .getContent())
        : messageMapper.entityListToClass(messageRepository
            .findByDataflowIdAndProviderId(dataflowId, providerId, pageRequest).getContent()));

    messagePaginatedVO
        .setTotalMessages(messageRepository.countByDataflowIdAndProviderId(dataflowId, providerId));

    return messagePaginatedVO;
  }

  /**
   * Gets the message attachment.
   *
   * @param messageId the message id
   * @return the message attachment
   * @throws EEAException the EEA exception
   */
  @Override
  public Message getMessage(Long messageId) throws EEAException {
    return messageRepository.findById(messageId).orElseThrow(EEAException::new);
  }


  /**
   * Gets the message attachment.
   *
   * @param messageId the message id
   * @param dataflowId the dataflow id
   * @param fileName the file name
   * @return the message attachment
   */
  @Override
  public byte[] getMessageAttachment(Long messageId, Long dataflowId, String fileName) {
    return documentControllerZuul.getCollaborationDocument(dataflowId, fileName, messageId);
  }

  /**
   * Verify messaging permissions and get direction.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @return true, if successful
   * @throws EEAForbiddenException the EEA forbidden exception
   */
  private boolean authorizeAndGetDirection(Long dataflowId, Long providerId)
      throws EEAForbiddenException {

    List<Long> datasetIds = datasetMetabaseControllerZuul
        .getDatasetIdsByDataflowIdAndDataProviderId(dataflowId, providerId);

    if (null != datasetIds && !datasetIds.isEmpty()) {
      Collection<? extends GrantedAuthority> authorities =
          SecurityContextHolder.getContext().getAuthentication().getAuthorities();

      boolean direction = authorities
          .contains(new SimpleGrantedAuthority(
              ObjectAccessRoleEnum.DATAFLOW_LEAD_REPORTER.getAccessRole(dataflowId)))
          || authorities.contains(new SimpleGrantedAuthority(
              ObjectAccessRoleEnum.DATAFLOW_REPORTER_READ.getAccessRole(dataflowId)))
          || authorities.contains(new SimpleGrantedAuthority(
              ObjectAccessRoleEnum.DATAFLOW_REPORTER_WRITE.getAccessRole(dataflowId)));

      boolean custodianSteward = authorities
          .contains(new SimpleGrantedAuthority(
              ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(dataflowId)))
          || authorities.contains(new SimpleGrantedAuthority(
              ObjectAccessRoleEnum.DATAFLOW_STEWARD.getAccessRole(dataflowId)))
          || authorities.contains(new SimpleGrantedAuthority(
              ObjectAccessRoleEnum.DATAFLOW_STEWARD_SUPPORT.getAccessRole(dataflowId)));
      if (custodianSteward) {
        direction = false;
      }

      boolean authorizedSender = false;

      if (direction) {
        authorizedSender = datasetIds.stream()
            .anyMatch(datasetId -> authorities
                .contains(new SimpleGrantedAuthority(
                    ObjectAccessRoleEnum.DATASET_LEAD_REPORTER.getAccessRole(datasetId)))
                || authorities.contains(new SimpleGrantedAuthority(
                    ObjectAccessRoleEnum.DATASET_REPORTER_READ.getAccessRole(datasetId)))
                || authorities.contains(new SimpleGrantedAuthority(
                    ObjectAccessRoleEnum.DATASET_REPORTER_WRITE.getAccessRole(datasetId))));

      }

      if (custodianSteward || authorizedSender) {
        return direction;
      }
    }

    LOG_ERROR.error("Messaging authorization failed: dataflowId={}, providerId={}", dataflowId,
        providerId);
    throw new EEAForbiddenException(EEAErrorMessage.MESSAGING_AUTHORIZATION_FAILED);
  }

  /**
   * Builds the message map.
   *
   * @param messageVOs the message V os
   * @return the map
   * @throws EEAIllegalArgumentException the EEA illegal argument exception
   */
  private Map<Long, Boolean> buildMessageMap(List<MessageVO> messageVOs)
      throws EEAIllegalArgumentException {

    if (null == messageVOs || messageVOs.isEmpty()) {
      throw new EEAIllegalArgumentException(EEAErrorMessage.MESSAGING_BAD_REQUEST);
    }

    Map<Long, Boolean> messageMap = new HashMap<>();
    for (MessageVO messageVO : messageVOs) {
      if (null == messageVO.getId()) {
        throw new EEAIllegalArgumentException(EEAErrorMessage.MESSAGING_BAD_REQUEST);
      }
      messageMap.put(messageVO.getId(), messageVO.isRead());
    }

    return messageMap;
  }
}
