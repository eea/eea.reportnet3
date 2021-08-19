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

public class UpdateDocumentCompletedEventTest {

  @InjectMocks
  private UpdateDocumentCompletedEvent updateDocumentCompletedEvent;

  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  @Mock
  private DataFlowVO dataflowVO;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.UPDATED_DOCUMENT_COMPLETED_EVENT,
        updateDocumentCompletedEvent.getEventType());
  }

  @Test
  public void getMapTest() throws EEAException {
    Assert.assertEquals(4, updateDocumentCompletedEvent.getMap(NotificationVO.builder().user("user")
        .dataflowId(1L).dataflowName("dataflowName").fileName("fileName").build()).size());
  }

  @Test
  public void getMapFromMinimumDataTest() throws EEAException {
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    Mockito.when(dataflowVO.getName()).thenReturn("dataflowName");
    Assert.assertEquals(4,
        updateDocumentCompletedEvent
            .getMap(
                NotificationVO.builder().user("user").dataflowId(1L).fileName("fileName").build())
            .size());
  }
}
