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

/**
 * The Class ReleaseValidationBlockersFailEventTest.
 */
public class ReleaseValidationBlockersFailEventTest {



  /** The release validation blockers fail event. */
  @InjectMocks
  private ReleaseValidationBlockersFailEvent releaseValidationBlockersFailEvent;


  /** The dataset metabase controller. */
  @Mock
  private DatasetService datasetService;



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
    Assert.assertEquals(EventType.RELEASE_BLOCKERS_FAILED_EVENT,
        releaseValidationBlockersFailEvent.getEventType());
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
        releaseValidationBlockersFailEvent.getMap(
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
    Mockito.when(datasetService.getDataFlowIdById(1L)).thenReturn(1L);
    Assert.assertEquals(4, releaseValidationBlockersFailEvent
        .getMap(NotificationVO.builder().user("user").datasetId(1L).build()).size());
  }
}
