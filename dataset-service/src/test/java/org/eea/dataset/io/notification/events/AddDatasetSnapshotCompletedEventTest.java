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

public class AddDatasetSnapshotCompletedEventTest {

  @InjectMocks
  private AddDatasetSnapshotCompletedEvent addDatasetSnapshotCompletedEvent;

  @Mock
  private DatasetService datasetService;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.ADD_DATASET_SNAPSHOT_COMPLETED_EVENT,
        addDatasetSnapshotCompletedEvent.getEventType());
  }

  @Test
  public void getMapTest1() throws EEAException {
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    Assert.assertEquals(3, addDatasetSnapshotCompletedEvent
        .getMap(NotificationVO.builder().user("user").datasetId(1L).build()).size());
  }

  @Test
  public void getMapTest2() throws EEAException {
    Assert.assertEquals(3, addDatasetSnapshotCompletedEvent
        .getMap(NotificationVO.builder().user("user").datasetId(1L).dataflowId(1L).build()).size());
  }
}
