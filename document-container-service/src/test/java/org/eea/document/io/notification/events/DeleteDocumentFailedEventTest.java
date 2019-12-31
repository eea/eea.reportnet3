package org.eea.document.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class DeleteDocumentFailedEventTest {

  @InjectMocks
  private DeleteDocumentFailedEvent deleteDocumentFailedEvent;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.DELETE_DOCUMENT_FAILED_EVENT,
        deleteDocumentFailedEvent.getEventType());
  }

  @Test
  public void getMapTest() throws EEAException {
    Assert.assertEquals(3,
        deleteDocumentFailedEvent
            .getMap(NotificationVO.builder().user("user").dataflowId(1L).error("error").build())
            .size());
  }
}
