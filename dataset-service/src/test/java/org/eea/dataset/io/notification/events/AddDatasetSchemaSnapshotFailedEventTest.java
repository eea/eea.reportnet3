package org.eea.dataset.io.notification.events;

import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class AddDatasetSchemaSnapshotFailedEventTest {

  @InjectMocks
  private AddDatasetSchemaSnapshotFailedEvent addDatasetSchemaSnapshotFailedEvent;

  @Mock
  private DatasetService datasetService;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.ADD_DATASET_SCHEMA_SNAPSHOT_FAILED_EVENT,
        addDatasetSchemaSnapshotFailedEvent.getEventType());
  }

  @Test
  public void getMapTest1() throws EEAException {
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    Assert.assertEquals(4, addDatasetSchemaSnapshotFailedEvent
        .getMap(NotificationVO.builder().user("user").datasetId(1L).error("error").build()).size());
  }

  @Test
  public void getMapTest2() throws EEAException {
    Assert.assertEquals(4, addDatasetSchemaSnapshotFailedEvent.getMap(
        NotificationVO.builder().user("user").datasetId(1L).error("error").dataflowId(1L).build())
        .size());
  }
}
