package org.eea.recordstore.io.notification.event;

import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
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

public class ReleaseDatasetSnapshotFailedEventTest {

  @InjectMocks
  private ReleaseDatasetSnapshotFailedEvent releaseDatasetSnapshotFailedEvent;


  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseController;

  @Mock
  private DataSetMetabaseVO datasetVO;



  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.RELEASE_DATASET_SNAPSHOT_FAILED_EVENT,
        releaseDatasetSnapshotFailedEvent.getEventType());
  }

  @Test
  public void getMapTest1() throws EEAException {
    Assert.assertEquals(4,
        releaseDatasetSnapshotFailedEvent.getMap(
            NotificationVO.builder().user("user").datasetId(1L).datasetName("datasetName").build())
            .size());
  }

  @Test
  public void getMapTest2() throws EEAException {
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(datasetVO);
    Mockito.when(datasetVO.getDataSetName()).thenReturn("datasetName");
    Assert.assertEquals(4, releaseDatasetSnapshotFailedEvent
        .getMap(NotificationVO.builder().user("user").datasetId(1L).build()).size());
  }
}
