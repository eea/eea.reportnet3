package org.eea.dataset.io.notification.events;

import java.util.Optional;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
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

public class CopyDataToEUDatasetFailedEventTest {

  @InjectMocks
  private CopyDataToEUDatasetFailedEvent copyDataToEUDatasetFailedEvent;

  @Mock
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  @Mock
  private DataSetMetabase dataSetMetabase;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.COPY_DATA_TO_EUDATASET_FAILED_EVENT,
        copyDataToEUDatasetFailedEvent.getEventType());
  }

  @Test
  public void getMapTest() throws EEAException {
    Assert.assertEquals(4, copyDataToEUDatasetFailedEvent.getMap(NotificationVO.builder()
        .user("user").datasetId(1L).datasetName("datasetName").error("error").build()).size());
  }

  @Test
  public void getMapFromMinimumDataTest() throws EEAException {
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(dataSetMetabase));
    Mockito.when(dataSetMetabase.getDataSetName()).thenReturn("datasetName");
    Assert.assertEquals(4, copyDataToEUDatasetFailedEvent
        .getMap(NotificationVO.builder().user("user").datasetId(1L).error("error").build()).size());
  }

}
