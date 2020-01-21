package org.eea.document.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class UploadDocumentFailedEventTest {

  @InjectMocks
  private UploadDocumentFailedEvent uploadDocumentFailedEvent;

  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  @Mock
  private DataFlowVO dataflowVO;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.UPLOAD_DOCUMENT_FAILED_EVENT,
        uploadDocumentFailedEvent.getEventType());
  }

  @Test
  public void getMapTest1() throws EEAException {
    Assert
        .assertEquals(5,
            uploadDocumentFailedEvent
                .getMap(NotificationVO.builder().user("user").dataflowId(1L)
                    .dataflowName("dataflowName").fileName("fileName").error("error").build())
                .size());
  }

  @Test
  public void getMapTest2() throws EEAException {
    Mockito.when(dataflowControllerZuul.findById(Mockito.any())).thenReturn(dataflowVO);
    Mockito.when(dataflowVO.getName()).thenReturn("dataflowName");
    Assert.assertEquals(5, uploadDocumentFailedEvent.getMap(NotificationVO.builder().user("user")
        .dataflowId(1L).fileName("fileName").error("error").build()).size());
  }
}
