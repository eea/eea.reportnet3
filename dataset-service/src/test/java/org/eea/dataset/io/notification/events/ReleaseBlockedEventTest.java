package org.eea.dataset.io.notification.events;

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

/**
 * The Class ReleaseBlockedEventTest.
 */
public class ReleaseBlockedEventTest {


  /** The release blocked event. */
  @InjectMocks
  private ReleaseBlockedEvent releaseBlockedEvent;


  /** The dataset metabase controller. */
  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseController;

  /** The dataset VO. */
  @Mock
  private DataSetMetabaseVO datasetVO;



  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Gets the event type test.
   *
   * @return the event type test
   */
  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.RELEASE_BLOCKED_EVENT, releaseBlockedEvent.getEventType());
  }

  /**
   * Gets the map test.
   *
   * @return the map test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getMapTest() throws EEAException {
    Assert.assertEquals(4,
        releaseBlockedEvent.getMap(
            NotificationVO.builder().user("user").datasetId(1L).datasetName("datasetName").build())
            .size());
  }

  /**
   * Gets the map test 1.
   *
   * @return the map test 1
   * @throws EEAException the EEA exception
   */
  @Test
  public void getMapFromMinimumDataTest() throws EEAException {
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(datasetVO);
    Mockito.when(datasetVO.getDataSetName()).thenReturn("datasetName");
    Assert.assertEquals(4, releaseBlockedEvent
        .getMap(NotificationVO.builder().user("user").datasetId(1L).build()).size());
  }
}
