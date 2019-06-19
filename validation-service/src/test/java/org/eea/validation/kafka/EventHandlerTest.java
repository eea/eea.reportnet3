package org.eea.validation.kafka;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;
import org.eea.validation.service.ValidationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventHandlerTest {


  @InjectMocks
  private EventHandler eventHandler;

  @Mock
  private ValidationService validationService;

  @Mock
  private KafkaSender kafkaSender;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testKafka() throws EEAException {
    EEAEventVO event = new EEAEventVO();
    event.setEventType(EventType.LOAD_DATA_COMPLETED_EVENT);
    Map<String, Object> data = new HashMap<String, Object>();
    event.setData(data);
    doNothing().when(validationService).deleteAllValidation(Mockito.any());
    doNothing().when(validationService).validateDataSetData(Mockito.any());
    eventHandler.processMessage(event);
    Mockito.verify(validationService, times(1)).validateDataSetData(Mockito.any());
  }

  @Test
  public void testKafkaException() throws EEAException {
    EEAEventVO event = new EEAEventVO();
    event.setEventType(EventType.LOAD_DATA_COMPLETED_EVENT);
    Map<String, Object> data = new HashMap<String, Object>();
    event.setData(data);
    doNothing().when(validationService).deleteAllValidation(Mockito.any());
    doThrow(new EEAException()).when(validationService).validateDataSetData(Mockito.any());
    eventHandler.processMessage(event);
    Mockito.verify(validationService, times(1)).validateDataSetData(Mockito.any());
  }

  @Test
  public void testKafkanotLoad() {
    EEAEventVO event = new EEAEventVO();
    event.setEventType(EventType.CONNECTION_CREATED_EVENT);
    Map<String, Object> data = new HashMap<String, Object>();
    event.setData(data);
    eventHandler.processMessage(event);
  }

  @Test
  public void getType() {
    assertEquals("failed", EEAEventVO.class, eventHandler.getType());
  }


}
