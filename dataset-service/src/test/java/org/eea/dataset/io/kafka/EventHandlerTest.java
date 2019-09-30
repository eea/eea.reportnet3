package org.eea.dataset.io.kafka;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventHandlerTest {

  @InjectMocks
  private EventHandler eventHandler;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getType() {
    assertEquals("failed", EEAEventVO.class, eventHandler.getType());
  }


  @Test
  public void porcessMessageTestNoEvent() {
    EEAEventVO event = new EEAEventVO();
    event.setEventType(EventType.HELLO_KAFKA_EVENT);
    eventHandler.processMessage(event);
  }


  @Test
  public void porcessMessageTest() {
    EEAEventVO event = new EEAEventVO();
    Map<String, Object> map = new HashMap<>();
    map.put("connectionDataVO", new ConnectionDataVO());
    event.setData(map);
    event.setEventType(EventType.CONNECTION_CREATED_EVENT);
    eventHandler.processMessage(event);
  }


}
