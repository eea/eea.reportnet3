package org.eea.collaboration.controller;

import static org.mockito.Mockito.times;
import java.io.IOException;
import java.util.ArrayList;
import org.eea.collaboration.persistence.domain.MessageAttachment;
import org.eea.collaboration.service.CollaborationService;
import org.eea.collaboration.service.helper.CollaborationServiceHelper;
import org.eea.exception.EEAException;
import org.eea.exception.EEAForbiddenException;
import org.eea.exception.EEAIllegalArgumentException;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import org.eea.interfaces.vo.dataset.enums.MessageTypeEnum;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RunWith(MockitoJUnitRunner.class)
public class CollaborationControllerImplTest {

  @InjectMocks
  private CollaborationControllerImpl collaborationControllerImpl;

  @Mock
  private CollaborationService collaborationService;

  @Mock
  private CollaborationServiceHelper collaborationServiceHelper;


  @Test
  public void createMessageTest() throws EEAIllegalArgumentException, EEAForbiddenException {
    Mockito.when(collaborationService.createMessage(Mockito.anyLong(), Mockito.any()))
        .thenReturn(new MessageVO());
    Assert.assertNotNull(collaborationControllerImpl.createMessage(1L, new MessageVO()));
  }

  @Test(expected = ResponseStatusException.class)
  public void createMessageEEAIllegalArgumentExceptionTest()
      throws EEAIllegalArgumentException, EEAForbiddenException {
    Mockito.when(collaborationService.createMessage(Mockito.anyLong(), Mockito.any()))
        .thenThrow(EEAIllegalArgumentException.class);
    try {
      collaborationControllerImpl.createMessage(1L, new MessageVO());
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void createMessageEEAForbiddenExceptionExceptionTest()
      throws EEAIllegalArgumentException, EEAForbiddenException {
    Mockito.when(collaborationService.createMessage(Mockito.anyLong(), Mockito.any()))
        .thenThrow(EEAForbiddenException.class);
    try {
      collaborationControllerImpl.createMessage(1L, new MessageVO());
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  @Test
  public void createMessageAttachmentTest()
      throws EEAIllegalArgumentException, EEAForbiddenException, IOException {
    Mockito.when(collaborationService.createMessageAttachment(Mockito.anyLong(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new MessageVO());
    Assert.assertNotNull(collaborationControllerImpl.createMessageAttachment(1L, 1L,
        new MockMultipartFile("file.csv", "content".getBytes())));
  }

  @Test(expected = ResponseStatusException.class)
  public void createMessageAttachmentEEAIllegalArgumentExceptionTest()
      throws EEAIllegalArgumentException, EEAForbiddenException, IOException {
    Mockito
        .when(collaborationService.createMessageAttachment(Mockito.anyLong(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any()))
        .thenThrow(EEAIllegalArgumentException.class);
    try {
      collaborationControllerImpl.createMessageAttachment(1L, 1L,
          new MockMultipartFile("file.csv", "content".getBytes()));
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void createMessageAttachmentEEAForbiddenExceptionTest()
      throws EEAIllegalArgumentException, EEAForbiddenException, IOException {
    Mockito.when(collaborationService.createMessageAttachment(Mockito.anyLong(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(EEAForbiddenException.class);
    try {
      collaborationControllerImpl.createMessageAttachment(1L, 1L,
          new MockMultipartFile("file.csv", "content".getBytes()));
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void createMessageAttachmentIOExceptionTest()
      throws EEAIllegalArgumentException, EEAForbiddenException, IOException {
    Mockito.when(collaborationService.createMessageAttachment(Mockito.anyLong(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(IOException.class);
    try {
      collaborationControllerImpl.createMessageAttachment(1L, 1L,
          new MockMultipartFile("file.csv", "content".getBytes()));
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void createMessageAttachmentEEAIllegalArgumentExceptionProviderIdNullTest()
      throws EEAIllegalArgumentException, EEAForbiddenException {
    try {
      collaborationControllerImpl.createMessageAttachment(null, 1L,
          new MockMultipartFile("file.csv", "content".getBytes()));
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void createMessageAttachmentEEAIllegalArgumentExceptionFileSizeTest()
      throws EEAIllegalArgumentException, EEAForbiddenException {
    try {
      byte[] arrayBytes21MB = new byte[1024 * 1024 * 21];
      MockMultipartFile file = new MockMultipartFile("21MB", "file.csv", "content", arrayBytes21MB);
      collaborationControllerImpl.createMessageAttachment(1L, 1L, file);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test
  public void updateMessageReadStatusTest()
      throws EEAIllegalArgumentException, EEAForbiddenException {
    collaborationControllerImpl.updateMessageReadStatus(1L, new ArrayList<MessageVO>());
    Mockito.verify(collaborationService, Mockito.times(1))
        .updateMessageReadStatus(Mockito.anyLong(), Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void updateMessageReadStatusEEAIllegalArgumentExceptionTest()
      throws EEAIllegalArgumentException, EEAForbiddenException {
    Mockito.doThrow(EEAIllegalArgumentException.class).when(collaborationService)
        .updateMessageReadStatus(Mockito.anyLong(), Mockito.any());
    try {
      collaborationControllerImpl.updateMessageReadStatus(1L, new ArrayList<MessageVO>());
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void updateMessageReadStatusEEAForbiddenExceptionExceptionTest()
      throws EEAIllegalArgumentException, EEAForbiddenException {
    Mockito.doThrow(EEAForbiddenException.class).when(collaborationService)
        .updateMessageReadStatus(Mockito.anyLong(), Mockito.any());
    try {
      collaborationControllerImpl.updateMessageReadStatus(1L, new ArrayList<MessageVO>());
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  @Test
  public void deleteMessageTest() throws EEAException {
    Mockito.doNothing().when(collaborationService).deleteMessage(Mockito.anyLong());
    collaborationControllerImpl.deleteMessage(1L, 1L, 1L);
    Mockito.verify(collaborationService, times(1)).deleteMessage(1L);
  }

  @Test(expected = ResponseStatusException.class)
  public void deleteMessageEEAIllegalArgumentExceptionTest() throws EEAException {

    Mockito.doThrow(new EEAIllegalArgumentException()).when(collaborationService)
        .deleteMessage(Mockito.anyLong());

    try {
      collaborationControllerImpl.deleteMessage(1L, 1L, 1L);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void deleteMessageEEAExceptionTest() throws EEAException {

    Mockito.doThrow(new EEAException()).when(collaborationService).deleteMessage(Mockito.anyLong());

    try {
      collaborationControllerImpl.deleteMessage(1L, 1L, 1L);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void deleteMessageEEAIllegalArgumentExceptionProviderIdNullTest()
      throws EEAIllegalArgumentException, EEAForbiddenException {
    try {
      collaborationControllerImpl.deleteMessage(null, 1L, 1L);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test
  public void findMessagesTest() throws EEAIllegalArgumentException, EEAForbiddenException {
    Mockito.when(collaborationService.findMessages(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyBoolean(), Mockito.anyInt())).thenReturn(new ArrayList<MessageVO>());
    Assert.assertNotNull(collaborationControllerImpl.findMessages(1L, 1L, true, 1));
  }

  @Test
  public void findMessagesAttachmentTest()
      throws EEAIllegalArgumentException, EEAForbiddenException, EEAException {
    MessageVO messageVO = new MessageVO();
    ArrayList<MessageVO> arrayListMessageVO = new ArrayList<>();
    messageVO.setType(MessageTypeEnum.TEXT);
    arrayListMessageVO.add(messageVO);
    messageVO = new MessageVO();
    messageVO.setId(1L);
    messageVO.setType(MessageTypeEnum.ATTACHMENT);
    arrayListMessageVO.add(messageVO);
    Mockito.when(collaborationService.findMessages(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyBoolean(), Mockito.anyInt())).thenReturn(arrayListMessageVO);

    MessageAttachment messageAttachment = new MessageAttachment();
    messageAttachment.setFileName("file.csv");
    Mockito.when(collaborationService.getMessageAttachment(Mockito.anyLong()))
        .thenReturn(messageAttachment);

    collaborationControllerImpl.findMessages(1L, 1L, true, 0);
    Mockito.verify(collaborationService, times(1)).findMessages(Mockito.anyLong(),
        Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyInt());
  }

  @Test(expected = ResponseStatusException.class)
  public void findMessagesAttachmentEEAExceptionTest()
      throws EEAIllegalArgumentException, EEAForbiddenException, EEAException {
    MessageVO messageVO = new MessageVO();
    ArrayList<MessageVO> arrayListMessageVO = new ArrayList<>();
    messageVO.setId(1L);
    messageVO.setType(MessageTypeEnum.ATTACHMENT);
    arrayListMessageVO.add(messageVO);
    Mockito.when(collaborationService.findMessages(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyBoolean(), Mockito.anyInt())).thenReturn(arrayListMessageVO);

    Mockito.when(collaborationService.getMessageAttachment(Mockito.anyLong()))
        .thenThrow(EEAException.class);
    try {
      collaborationControllerImpl.findMessages(1L, 1L, true, 0);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void findMessagesEEAForbiddenExceptionExceptionTest()
      throws EEAIllegalArgumentException, EEAForbiddenException {
    Mockito.when(collaborationService.findMessages(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyBoolean(), Mockito.anyInt())).thenThrow(EEAForbiddenException.class);
    try {
      collaborationControllerImpl.findMessages(1L, 1L, true, 1);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  @Test
  public void testGetMessageAttachment() throws EEAException {
    MessageAttachment messageAttachment = new MessageAttachment();
    messageAttachment.setFileName("file.csv");
    messageAttachment.setContent("file.csv".getBytes());
    Mockito.when(collaborationService.getMessageAttachment(Mockito.anyLong()))
        .thenReturn(messageAttachment);
    collaborationControllerImpl.getMessageAttachment(1L, 1L, 1L);
    Mockito.verify(collaborationService, times(1)).getMessageAttachment(Mockito.anyLong());
  }

  @Test(expected = ResponseStatusException.class)
  public void testGetMessageAttachmentException() throws EEAException {
    Mockito.when(collaborationService.getMessageAttachment(Mockito.anyLong()))
        .thenThrow(EEAException.class);
    try {
      collaborationControllerImpl.getMessageAttachment(1L, 1L, 1L);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  @Test
  public void notifyNewMessagesTest() {
    collaborationControllerImpl.notifyNewMessages(1L, 1L, null, null, null, "RECEIVED_MESSAGE");
    Mockito.verify(collaborationServiceHelper, Mockito.times(1)).notifyNewMessages(
        Mockito.anyLong(), Mockito.anyLong(), Mockito.nullable(Long.class),
        Mockito.nullable(DatasetStatusEnum.class), Mockito.nullable(String.class), Mockito.any());
  }
}
