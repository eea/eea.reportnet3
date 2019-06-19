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
import org.eea.validation.util.ValidationHelper;
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
  private ValidationHelper validationHelper;


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
    doNothing().when(validationHelper).executeValidation(Mockito.any());
    eventHandler.processMessage(event);
    Mockito.verify(validationHelper, times(1)).executeValidation(Mockito.any());
  }

  @Test
  public void testKafkaException() throws EEAException {
    EEAEventVO event = new EEAEventVO();
    event.setEventType(EventType.LOAD_DATA_COMPLETED_EVENT);
    Map<String, Object> data = new HashMap<String, Object>();
    event.setData(data);
    doThrow(new EEAException()).when(validationHelper).executeValidation(Mockito.any());
    eventHandler.processMessage(event);
    Mockito.verify(validationHelper, times(1)).executeValidation(Mockito.any());
  }

  @Test
  public void testKafkanotLoad() throws EEAException {
    EEAEventVO event = new EEAEventVO();
    event.setEventType(EventType.CONNECTION_CREATED_EVENT);
    Map<String, Object> data = new HashMap<String, Object>();
    event.setData(data);
    eventHandler.processMessage(event);
    Mockito.verify(validationHelper, times(0)).executeValidation(Mockito.any());
  }

  @Test
  public void getType() {
    assertEquals("failed", EEAEventVO.class, eventHandler.getType());
  }


}
