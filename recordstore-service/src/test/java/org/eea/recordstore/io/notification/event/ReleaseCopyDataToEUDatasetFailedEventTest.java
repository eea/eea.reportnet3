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

public class ReleaseCopyDataToEUDatasetFailedEventTest {

  @InjectMocks
  private ReleaseCopyDataToEUDatasetFailedEvent releaseCopyDataToEUDatasetFailedEvent;

  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  @Mock
  private DataSetMetabaseVO datasetMetabaseVO;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.COPY_DATA_TO_EUDATASET_FAILED_EVENT,
        releaseCopyDataToEUDatasetFailedEvent.getEventType());
  }

  @Test
  public void getMapTest() throws EEAException {
    Assert.assertEquals(4, releaseCopyDataToEUDatasetFailedEvent.getMap(NotificationVO.builder()
        .user("user").datasetId(1L).datasetName("datasetName").error("error").build()).size());
  }

  @Test
  public void getMapFromMinimumDataTest() throws EEAException {
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(datasetMetabaseVO.getDataSetName()).thenReturn("datasetName");
    Assert.assertEquals(4, releaseCopyDataToEUDatasetFailedEvent
        .getMap(NotificationVO.builder().user("user").datasetId(1L).error("error").build()).size());
  }

}
