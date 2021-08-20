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

public class CopyDataToEUDatasetCompletedEventTest {

  @InjectMocks
  private CopyDataToEUDatasetCompletedEvent copyDataToEUDatasetCompletedEvent;

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
    Assert.assertEquals(EventType.COPY_DATA_TO_EUDATASET_COMPLETED_EVENT,
        copyDataToEUDatasetCompletedEvent.getEventType());
  }

  @Test
  public void getMapTest() throws EEAException {
    Assert.assertEquals(3,
        copyDataToEUDatasetCompletedEvent.getMap(
            NotificationVO.builder().user("user").datasetId(1L).datasetName("datasetName").build())
            .size());
  }

  @Test
  public void getMapFromMinimumDataTest() throws EEAException {
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(dataSetMetabase));
    Mockito.when(dataSetMetabase.getDataSetName()).thenReturn("datasetName");
    Assert.assertEquals(3, copyDataToEUDatasetCompletedEvent
        .getMap(NotificationVO.builder().user("user").datasetId(1L).build()).size());
  }

}
