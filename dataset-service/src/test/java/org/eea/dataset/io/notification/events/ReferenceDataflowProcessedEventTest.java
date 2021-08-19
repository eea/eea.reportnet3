package org.eea.dataset.io.notification.events;

import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * The Class ReferenceDataflowProcessedEventTest.
 */
public class ReferenceDataflowProcessedEventTest {

  @InjectMocks
  private ReferenceDataflowProcessedEvent referenceDataflowProcessedEvent;

  @Mock
  private DatasetService datasetService;

  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  @Mock
  private DataSetMetabaseVO datasetMetabaseVO;

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
    Assert.assertEquals(EventType.REFERENCE_DATAFLOW_PROCESSED_EVENT,
        referenceDataflowProcessedEvent.getEventType());
  }

  @Test
  public void getMapTest() throws EEAException {
    Assert.assertEquals(3, referenceDataflowProcessedEvent.getMap(
        NotificationVO.builder().user("user").dataflowId(1L).dataflowName("dataflowName").build())
        .size());
  }

  @Test
  public void getMapFromMinimumDataTest() throws EEAException {
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    Assert.assertEquals(3, referenceDataflowProcessedEvent
        .getMap(NotificationVO.builder().user("user").dataflowId(1L).build()).size());
  }
}
