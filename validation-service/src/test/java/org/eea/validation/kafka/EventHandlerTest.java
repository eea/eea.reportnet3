package org.eea.validation.kafka;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.multitenancy.MultiTenantDataSource;
import org.eea.validation.util.ValidationHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class EventHandlerTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class EventHandlerTest {


  /** The event handler. */
  @InjectMocks
  private EventHandler eventHandler;

  /** The validation helper. */
  @Mock
  private ValidationHelper validationHelper;

  /** The data source. */
  @Mock
  private MultiTenantDataSource dataSource;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test kafka.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testKafka() throws EEAException {
    EEAEventVO event = new EEAEventVO();
    event.setEventType(EventType.LOAD_DATA_COMPLETED_EVENT);
    Map<String, Object> data = new HashMap<>();
    event.setData(data);
    doNothing().when(validationHelper).executeValidation(Mockito.any(), Mockito.any());
    eventHandler.processMessage(event);
    Mockito.verify(validationHelper, times(1)).executeValidation(Mockito.any(), Mockito.any());
  }

  /**
   * Test kafka exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testKafkaException() throws EEAException {
    EEAEventVO event = new EEAEventVO();
    event.setEventType(EventType.LOAD_DATA_COMPLETED_EVENT);
    Map<String, Object> data = new HashMap<>();
    event.setData(data);
    doThrow(new EEAException()).when(validationHelper).executeValidation(Mockito.any(),
        Mockito.any());
    eventHandler.processMessage(event);
    Mockito.verify(validationHelper, times(1)).executeValidation(Mockito.any(), Mockito.any());
  }

  /**
   * Test kafkanot load.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testKafkanotLoad() throws EEAException {
    EEAEventVO event = new EEAEventVO();
    event.setEventType(EventType.CONNECTION_CREATED_EVENT);
    Map<String, Object> data = new HashMap<>();
    event.setData(data);
    eventHandler.processMessage(event);
    Mockito.verify(validationHelper, times(0)).executeValidation(Mockito.any(), Mockito.any());
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  @Test
  public void getType() {
    assertEquals("failed", EEAEventVO.class, eventHandler.getType());
  }

  /**
   * Process message test.
   */
  @Test
  public void processMessageTest() {
    EEAEventVO event = new EEAEventVO();
    Map<String, Object> map = new HashMap<>();
    map.put("connectionDataVO", new ConnectionDataVO());
    event.setData(map);
    event.setEventType(EventType.CONNECTION_CREATED_EVENT);
    eventHandler.processMessage(event);
  }

}
