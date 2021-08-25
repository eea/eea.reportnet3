package org.eea.collaboration.service.impl;

import static org.mockito.Mockito.times;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.eea.collaboration.mapper.MessageMapper;
import org.eea.collaboration.persistence.domain.Message;
import org.eea.collaboration.persistence.domain.MessageAttachment;
import org.eea.collaboration.persistence.repository.MessageAttachmentRepository;
import org.eea.collaboration.persistence.repository.MessageRepository;
import org.eea.collaboration.service.helper.CollaborationServiceHelper;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.exception.EEAForbiddenException;
import org.eea.exception.EEAIllegalArgumentException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class CollaborationServiceImplTest {

  @InjectMocks
  private CollaborationServiceImpl collaborationServiceImpl;

  @Mock
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private CollaborationServiceHelper collaborationServiceHelper;

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private MessageAttachmentRepository messageAttachmentRepository;

  @Mock
  private MessageMapper messageMapper;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @Before
  public void initMocks() {
    SecurityContextHolder.setContext(securityContext);
  }

  @Test(expected = EEAIllegalArgumentException.class)
  public void createMessageEEAIllegalArgumentExceptionTest()
      throws EEAForbiddenException, EEAIllegalArgumentException {
    MessageVO messageVO = new MessageVO();
    messageVO.setProviderId(1L);
    messageVO.setContent("");
    try {
      collaborationServiceImpl.createMessage(1L, messageVO);
    } catch (EEAIllegalArgumentException e) {
      Assert.assertEquals(EEAErrorMessage.MESSAGING_BAD_REQUEST, e.getMessage());
      throw e;
    }
  }

  @Test(expected = EEAForbiddenException.class)
  public void createMessageEEAForbiddenExceptionTest()
      throws EEAForbiddenException, EEAIllegalArgumentException {
    MessageVO messageVO = new MessageVO();
    messageVO.setProviderId(1L);
    messageVO.setContent("content");
    Mockito
        .when(dataSetMetabaseControllerZuul
            .getDatasetIdsByDataflowIdAndDataProviderId(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(new ArrayList<Long>());
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");
    try {
      collaborationServiceImpl.createMessage(1L, messageVO);
    } catch (EEAForbiddenException e) {
      Assert.assertEquals(EEAErrorMessage.MESSAGING_AUTHORIZATION_FAILED, e.getMessage());
      throw e;
    }
  }

  @Test
  public void createMessageReporterWriteTest()
      throws EEAForbiddenException, EEAIllegalArgumentException {
    MessageVO messageVO = new MessageVO();
    messageVO.setProviderId(1L);
    messageVO.setContent("content");
    List<Long> datasetIds = new ArrayList<>();
    datasetIds.add(1L);
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(
        new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_REPORTER_WRITE.getAccessRole(1L)));
    authorities.add(
        new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATASET_REPORTER_WRITE.getAccessRole(1L)));
    Mockito
        .when(dataSetMetabaseControllerZuul
            .getDatasetIdsByDataflowIdAndDataProviderId(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(datasetIds);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    collaborationServiceImpl.createMessage(1L, messageVO);
    Mockito.verify(messageMapper, Mockito.times(1)).entityToClass(Mockito.any());
  }

  @Test
  public void createMessageAttachmentTest()
      throws EEAForbiddenException, EEAIllegalArgumentException, IOException {

    List<Long> datasetIds = new ArrayList<>();
    datasetIds.add(1L);
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(
        new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_REPORTER_WRITE.getAccessRole(1L)));
    authorities.add(
        new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATASET_REPORTER_WRITE.getAccessRole(1L)));
    MessageVO messageVO = new MessageVO();

    Mockito
        .when(dataSetMetabaseControllerZuul
            .getDatasetIdsByDataflowIdAndDataProviderId(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(datasetIds);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    Mockito.when(messageMapper.entityToClass(Mockito.any())).thenReturn(messageVO);
    collaborationServiceImpl.createMessageAttachment(1L, 1L,
        new MockMultipartFile("file.csv", "content".getBytes()).getInputStream(), "fileName",
        "fileSize");
    Mockito.verify(messageMapper, Mockito.times(1)).entityToClass(Mockito.any());
  }

  @Test(expected = EEAIllegalArgumentException.class)
  public void updateMessageReadStatusEEAIllegalArgumentExceptionTest()
      throws EEAForbiddenException, EEAIllegalArgumentException {
    try {
      collaborationServiceImpl.updateMessageReadStatus(1L, new ArrayList<MessageVO>());
    } catch (EEAIllegalArgumentException e) {
      Assert.assertEquals(EEAErrorMessage.MESSAGING_BAD_REQUEST, e.getMessage());
      throw e;
    }
  }

  @Test(expected = EEAIllegalArgumentException.class)
  public void updateMessageReadStatusEEAIllegalArgumentExceptionNoIdTest()
      throws EEAForbiddenException, EEAIllegalArgumentException {
    List<MessageVO> messageVOs = new ArrayList<>();
    messageVOs.add(new MessageVO());
    try {
      collaborationServiceImpl.updateMessageReadStatus(1L, messageVOs);
    } catch (EEAIllegalArgumentException e) {
      Assert.assertEquals(EEAErrorMessage.MESSAGING_BAD_REQUEST, e.getMessage());
      throw e;
    }
  }

  @Test(expected = EEAForbiddenException.class)
  public void updateMessageReadStatusEEAForbiddenExceptionTest()
      throws EEAForbiddenException, EEAIllegalArgumentException {
    List<MessageVO> messageVOs = new ArrayList<>();
    MessageVO messageVO = new MessageVO();
    messageVO.setId(1L);
    messageVOs.add(messageVO);
    List<Message> messages = new ArrayList<>();
    Message message = new Message();
    message.setId(1L);
    message.setDirection(false);
    messages.add(message);
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities
        .add(new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(1L)));
    Mockito.when(messageRepository.findByDataflowIdAndIdIn(Mockito.anyLong(), Mockito.any()))
        .thenReturn(messages);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    try {
      collaborationServiceImpl.updateMessageReadStatus(1L, messageVOs);
    } catch (EEAForbiddenException e) {
      Assert.assertEquals(EEAErrorMessage.MESSAGING_AUTHORIZATION_FAILED, e.getMessage());
      throw e;
    }

  }

  @Test
  public void updateMessageReadStatusCustodianTest()
      throws EEAForbiddenException, EEAIllegalArgumentException {
    List<MessageVO> messageVOs = new ArrayList<>();
    MessageVO messageVO = new MessageVO();
    messageVO.setId(1L);
    messageVOs.add(messageVO);
    List<Message> messages = new ArrayList<>();
    Message message = new Message();
    message.setId(1L);
    message.setDirection(true);
    messages.add(message);
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities
        .add(new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(1L)));
    Mockito.when(messageRepository.findByDataflowIdAndIdIn(Mockito.anyLong(), Mockito.any()))
        .thenReturn(messages);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    collaborationServiceImpl.updateMessageReadStatus(1L, messageVOs);
    Mockito.verify(messageRepository, Mockito.times(1)).saveAll(Mockito.anyIterable());
  }

  @Test
  public void updateMessageReadStatusReporterTest()
      throws EEAForbiddenException, EEAIllegalArgumentException {
    List<MessageVO> messageVOs = new ArrayList<>();
    MessageVO messageVO = new MessageVO();
    messageVO.setId(1L);
    messageVOs.add(messageVO);
    List<Message> messages = new ArrayList<>();
    Message message = new Message();
    message.setId(1L);
    message.setProviderId(1L);
    message.setDirection(false);
    messages.add(message);
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(
        new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_LEAD_REPORTER.getAccessRole(1L)));
    List<Long> providerIds = new ArrayList<>();
    providerIds.add(1L);
    Mockito.when(messageRepository.findByDataflowIdAndIdIn(Mockito.anyLong(), Mockito.any()))
        .thenReturn(messages);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    Mockito.when(dataSetMetabaseControllerZuul.getUserProviderIdsByDataflowId(Mockito.anyLong()))
        .thenReturn(providerIds);
    collaborationServiceImpl.updateMessageReadStatus(1L, messageVOs);
    Mockito.verify(messageRepository, Mockito.times(1)).saveAll(Mockito.anyIterable());
  }

  @Test
  public void deleteMessageTest() throws EEAException {
    Mockito.doNothing().when(messageRepository).deleteById(Mockito.anyLong());
    collaborationServiceImpl.deleteMessage(1L);
    Mockito.verify(messageRepository, times(1)).deleteById(Mockito.anyLong());
  }

  @Test
  public void deleteMessageAndAttachmentTest() throws EEAException {
    MessageAttachment messageAttachment = new MessageAttachment();
    Mockito.when(messageAttachmentRepository.findByMessageId(Mockito.anyLong()))
        .thenReturn(messageAttachment);
    Mockito.doNothing().when(messageRepository).deleteById(Mockito.anyLong());
    collaborationServiceImpl.deleteMessage(1L);
    Mockito.verify(messageRepository, times(1)).deleteById(Mockito.anyLong());
  }

  @Test(expected = EEAIllegalArgumentException.class)
  public void deleteMessageEEAIllegalArgumentExceptionTest() throws EEAException {
    Mockito.doThrow(new EmptyResultDataAccessException(1)).when(messageRepository)
        .deleteById(Mockito.anyLong());

    try {
      collaborationServiceImpl.deleteMessage(1L);
    } catch (EmptyResultDataAccessException e) {
      Assert.assertEquals(EEAErrorMessage.MESSAGE_INCORRECT_ID, e.getMessage());
      throw e;
    }
  }

  @Test
  public void findMessagesReadTest() throws EEAException {
    Page<Message> pageResponse = Mockito.mock(Page.class);
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities
        .add(new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(1L)));
    Mockito
        .when(dataSetMetabaseControllerZuul
            .getDatasetIdsByDataflowIdAndDataProviderId(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(Arrays.asList(1L));
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    Mockito.when(messageRepository.findByDataflowIdAndProviderIdAndRead(Mockito.anyLong(),
        Mockito.anyLong(), Mockito.anyBoolean(), Mockito.any())).thenReturn(pageResponse);
    Mockito.when(pageResponse.getContent()).thenReturn(new ArrayList<Message>());
    Mockito.when(messageMapper.entityListToClass(Mockito.any()))
        .thenReturn(new ArrayList<MessageVO>());
    Assert.assertNotNull(collaborationServiceImpl.findMessages(1L, 1L, true, 1));
  }

  @Test
  public void findMessagesTest() throws EEAException {
    Page<Message> pageResponse = Mockito.mock(Page.class);
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities
        .add(new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(1L)));
    Mockito
        .when(dataSetMetabaseControllerZuul
            .getDatasetIdsByDataflowIdAndDataProviderId(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(Arrays.asList(1L));
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    Mockito.when(messageRepository.findByDataflowIdAndProviderId(Mockito.anyLong(),
        Mockito.anyLong(), Mockito.any())).thenReturn(pageResponse);
    Mockito.when(pageResponse.getContent()).thenReturn(new ArrayList<Message>());
    Mockito.when(messageMapper.entityListToClass(Mockito.any()))
        .thenReturn(new ArrayList<MessageVO>());
    Assert.assertNotNull(collaborationServiceImpl.findMessages(1L, 1L, null, 1));
  }

  @Test
  public void getMessageAttachmentTest() throws EEAException {
    collaborationServiceImpl.getMessageAttachment(1L);
    Mockito.verify(messageAttachmentRepository, times(1)).findByMessageId(Mockito.any());
  }
}
