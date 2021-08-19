package org.eea.kafka.factory;


import java.util.HashMap;
import java.util.Map;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.commands.EEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;


/**
 * The type Eea event command factory impl test.
 */
public class EEAEventCommandFactoryImplTest {

  @InjectMocks
  private EEAEventCommandFactoryImpl eeaEventCommandFactory;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Gets event command.
   */
  @Test
  public void getEventCommand() {
    Map<EventType, AbstractEEAEventHandlerCommand> eventHandleCommands = new HashMap<>();
    AbstractEEAEventHandlerCommand command = Mockito.mock(AbstractEEAEventHandlerCommand.class);

    Mockito.when(command.getEventType()).thenReturn(EventType.CONNECTION_CREATED_EVENT);
    eventHandleCommands.put(EventType.CONNECTION_CREATED_EVENT, command);

    ReflectionTestUtils.setField(eeaEventCommandFactory, "eventHandleCommands",
        eventHandleCommands);

    EEAEventVO vo = new EEAEventVO();
    vo.setEventType(EventType.CONNECTION_CREATED_EVENT);

    EEAEventHandlerCommand result = eeaEventCommandFactory.getEventCommand(vo);

    Assert.assertNotNull("Retrieved event handler is null", result);
    Assert.assertEquals(
        "Event handler is not the one to treat the event type CONNECTION_CREATED_EVENT",
        EventType.CONNECTION_CREATED_EVENT,
        ((AbstractEEAEventHandlerCommand) result).getEventType());

  }
}
