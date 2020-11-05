package org.eea.collaboration.service.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.eea.collaboration.mapper.MessageMapper;
import org.eea.collaboration.persistence.domain.Message;
import org.eea.collaboration.persistence.repository.MessageRepository;
import org.eea.collaboration.service.CollaborationService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CollaborationServiceImpl implements CollaborationService {

  private static final Logger LOG = LoggerFactory.getLogger(CollaborationServiceImpl.class);

  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  @Value("${spring.health.db.check.frequency}")
  private int maxMessageLength;

  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private MessageMapper messageMapper;

  @Override
  public MessageVO createMessage(Long dataflowId, MessageVO messageVO) throws EEAException {

    String userName = SecurityContextHolder.getContext().getAuthentication().getName();
    boolean direction =
        verifyMessagingPermissionsAndGetDirection(dataflowId, messageVO.getProviderId());

    if (messageVO.getContent().length() > maxMessageLength) {
      messageVO.setContent(messageVO.getContent().substring(0, maxMessageLength));
    }

    Message message = new Message();
    message.setContent(messageVO.getContent());
    message.setDataflowId(dataflowId);
    message.setProviderId(messageVO.getProviderId());
    message.setDate(new Date());
    message.setRead(false);
    message.setUserName(userName);
    message.setDirection(direction);
    message = messageRepository.save(message);

    LOG.info("Message created: message={}", message);
    return messageMapper.entityToClass(message);
  }

  @Override
  @Transactional
  public void updateMessageReadStatus(Long dataflowId, List<MessageVO> messageVOs)
      throws EEAException {

    Map<Long, Boolean> map = new HashMap<>();
    messageVOs.forEach(messageVO -> map.put(messageVO.getId(), messageVO.isRead()));

    List<Message> messages = messageRepository.findByDataflowIdAndIdIn(dataflowId, map.keySet());
    int previousSize = messages.size();

    Collection<? extends GrantedAuthority> authorities =
        SecurityContextHolder.getContext().getAuthentication().getAuthorities();

    // Case: DATAFLOW_STEWARD or DATAFLOW_CUSTODIAN
    if (authorities.contains(
        new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_STEWARD.getAccessRole(dataflowId)))
        || authorities.contains(new SimpleGrantedAuthority(
            ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(dataflowId)))) {

      // Allowed messages to update: direction=TRUE and providerId=ANY
      messages = messages.stream().filter(Message::isDirection).collect(Collectors.toList());
    }

    // Case: DATAFLOW_LEAD_REPORTER, DATAFLOW_REPORTER_READ, DATAFLOW_REPOTER_WRITE
    else {
      List<Long> providerIds =
          datasetMetabaseControllerZuul.getUserProviderIdsByDataflowId(dataflowId);

      // Allowed messages to update: direction=FALSE and providerId=USER_DEPENDANT
      messages = messages.stream()
          .filter(
              message -> !message.isDirection() && providerIds.contains(message.getProviderId()))
          .collect(Collectors.toList());
    }

    if (messages.size() != previousSize) {
      LOG_ERROR.error("Messaging authorization failed: unable to update all messages");
      throw new EEAException(EEAErrorMessage.MESSAGING_AUTHORIZATION_FAILED);
    }

    messages = messages.stream().map(message -> {
      message.setRead(map.get(message.getId()));
      return message;
    }).collect(Collectors.toList());

    messageRepository.saveAll(messages);
  }

  @Override
  public List<MessageVO> findMessages(Long dataflowId, Long providerId, Boolean read, int page)
      throws EEAException {

    Page<Message> pageResponse;
    PageRequest pageRequest = PageRequest.of(page, 50, Sort.by("date").descending());
    verifyMessagingPermissionsAndGetDirection(dataflowId, providerId);

    if (null != read) {
      pageResponse = messageRepository.findByDataflowIdAndProviderIdAndRead(dataflowId, providerId,
          read, pageRequest);
    } else {
      pageResponse =
          messageRepository.findByDataflowIdAndProviderId(dataflowId, providerId, pageRequest);
    }

    return messageMapper.entityListToClass(pageResponse.getContent());
  }


  private boolean verifyMessagingPermissionsAndGetDirection(Long dataflowId, Long providerId)
      throws EEAException {

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

      if (!direction || authorizedSender) {
        return direction;
      }
    }

    LOG_ERROR.error("Messaging authorization failed: dataflowId={}, providerId={}", dataflowId,
        providerId);
    throw new EEAException(EEAErrorMessage.MESSAGING_AUTHORIZATION_FAILED);
  }
}
