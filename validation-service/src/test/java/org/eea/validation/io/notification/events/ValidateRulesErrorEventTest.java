package org.eea.validation.io.notification.events;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * The Class ValidateRulesErrorEventTest.
 */
public class ValidateRulesErrorEventTest {

  /** The validate rules error event. */
  @InjectMocks
  private ValidateRulesErrorEvent validateRulesErrorEvent;

  /** The dataset metabase controller. */
  @Mock
  private DatasetMetabaseController datasetMetabaseController;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test get event type.
   *
   * @return the event type test
   */
  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.VALIDATE_RULES_ERROR_EVENT, validateRulesErrorEvent.getEventType());
  }

  /**
   * Test get map.
   *
   * @return the map test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getMapTest() throws EEAException {
    DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
    dataSetMetabaseVO.setDataSetName("name");
    dataSetMetabaseVO.setId(1L);
    when(datasetMetabaseController.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(dataSetMetabaseVO);
    NotificationVO notificationVO = new NotificationVO();
    notificationVO.setDatasetSchemaId(new ObjectId().toString());
    notificationVO.setDatasetName("name");
    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("dataflowId", notificationVO.getDataflowId());
    notification.put("datasetId", notificationVO.getDatasetId());
    notification.put("datasetName", notificationVO.getDatasetName());
    notification.put("disabledRules", notificationVO.getDisabledRules());
    notification.put("invalidRules", notificationVO.getInvalidRules());
    notification.put("error", notificationVO.getError());
    assertEquals(notification, validateRulesErrorEvent.getMap(notificationVO));
  }

}
