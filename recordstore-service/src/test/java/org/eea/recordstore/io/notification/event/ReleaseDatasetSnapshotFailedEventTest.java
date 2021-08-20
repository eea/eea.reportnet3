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
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  @Mock
  private DataSetMetabaseVO datasetVO;



  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.RELEASE_FAILED_EVENT,
        releaseDatasetSnapshotFailedEvent.getEventType());
  }

  @Test
  public void getMapTest() throws EEAException {
    Assert.assertEquals(4,
        releaseDatasetSnapshotFailedEvent.getMap(
            NotificationVO.builder().user("user").datasetId(1L).datasetName("datasetName").build())
            .size());
  }

  @Test
  public void getMapFromMinimumDataTest() throws EEAException {
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(datasetVO);
    Mockito.when(datasetVO.getDataSetName()).thenReturn("datasetName");
    Assert.assertEquals(4, releaseDatasetSnapshotFailedEvent
        .getMap(NotificationVO.builder().user("user").datasetId(1L).build()).size());
  }
}
