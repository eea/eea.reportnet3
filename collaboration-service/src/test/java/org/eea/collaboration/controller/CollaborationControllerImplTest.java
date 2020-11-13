package org.eea.collaboration.controller;

import java.util.ArrayList;
import org.eea.collaboration.service.CollaborationService;
import org.eea.collaboration.service.helper.CollaborationServiceHelper;
import org.eea.exception.EEAForbiddenException;
import org.eea.exception.EEAIllegalArgumentException;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
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
  public void findMessagesTest() throws EEAIllegalArgumentException, EEAForbiddenException {
    Mockito.when(collaborationService.findMessages(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyBoolean(), Mockito.anyInt())).thenReturn(new ArrayList<MessageVO>());
    Assert.assertNotNull(collaborationControllerImpl.findMessages(1L, 1L, true, 1));
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
  public void notifyNewMessagesTest() {
    collaborationControllerImpl.notifyNewMessages(1L, 1L, null, null, null, "RECEIVED_MESSAGE");
    Mockito.verify(collaborationServiceHelper, Mockito.times(1)).notifyNewMessages(
        Mockito.anyLong(), Mockito.anyLong(), Mockito.nullable(Long.class),
        Mockito.nullable(DatasetStatusEnum.class), Mockito.nullable(String.class), Mockito.any());
  }
}
