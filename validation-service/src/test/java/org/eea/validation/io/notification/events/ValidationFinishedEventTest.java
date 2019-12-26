package org.eea.validation.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ValidationFinishedEventTest {

  @InjectMocks
  private ValidationFinishedEvent validationFinishedEvent;

  @Mock
  private DataSetControllerZuul dataSetControllerZuul;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.VALIDATION_FINISHED_EVENT,
        validationFinishedEvent.getEventType());
  }

  @Test
  public void getMapTest1() throws EEAException {
    Mockito.when(dataSetControllerZuul.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    Assert.assertEquals(3, validationFinishedEvent
        .getMap(NotificationVO.builder().user("user").datasetId(1L).build()).size());;
  }

  @Test
  public void getMapTest2() throws EEAException {
    Assert.assertEquals(3,
        validationFinishedEvent
            .getMap(NotificationVO.builder().user("user").datasetId(1L).dataflowId(1L).build())
            .size());;
  }
}
