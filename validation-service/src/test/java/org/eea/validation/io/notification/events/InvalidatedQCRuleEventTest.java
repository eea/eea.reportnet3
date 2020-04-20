package org.eea.validation.io.notification.events;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DataSetSchemaControllerZuul;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * The Class DisabledQCRuleEventTest.
 */
public class InvalidatedQCRuleEventTest {

  /** The disabled QC rule event. */
  @InjectMocks
  private InvalidatedQCRuleEvent disabledQCRuleEvent;

  /** The dataset schema controller. */
  @Mock
  private DataSetSchemaControllerZuul datasetSchemaController;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test get event type.
   */
  @Test
  public void testGetEventType() {
    assertEquals(EventType.INVALIDATED_QC_RULE_EVENT, disabledQCRuleEvent.getEventType());
  }

  /**
   * Test get map.
   * 
   * @throws EEAException
   */
  @Test
  public void testGetMap() throws EEAException {
    NotificationVO notificationVO = new NotificationVO();
    notificationVO.setDatasetSchemaId(new ObjectId().toString());
    notificationVO.setDatasetName("");
    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("error", notificationVO.getError());
    notification.put("datasetSchemaId", notificationVO.getDatasetSchemaId());
    notification.put("datasetName", notificationVO.getDatasetName());
    notification.put("shortCode", notificationVO.getShortCode());
    assertEquals(notification, disabledQCRuleEvent.getMap(notificationVO));
  }

  /**
   * Test get map.
   * 
   * @throws EEAException
   */
  @Test
  public void testGetMapSearchName() throws EEAException {
    NotificationVO notificationVO = new NotificationVO();
    notificationVO.setDatasetSchemaId(new ObjectId().toString());
    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("error", notificationVO.getError());
    notification.put("datasetSchemaId", notificationVO.getDatasetSchemaId());
    notification.put("datasetName", null);
    notification.put("shortCode", notificationVO.getShortCode());
    Mockito.when(datasetSchemaController.findDataSchemaById(Mockito.any()))
        .thenReturn(new DataSetSchemaVO());
    assertEquals(notification, disabledQCRuleEvent.getMap(notificationVO));
  }

}
