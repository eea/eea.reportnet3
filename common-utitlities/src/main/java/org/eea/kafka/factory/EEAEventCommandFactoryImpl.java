package org.eea.kafka.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.commands.DefaultEventHandlerCommand;
import org.eea.kafka.commands.EEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class EEAEventCommandFactoryImpl.
 */
@Component("EEAEventCommandFactoryImpl")
public class EEAEventCommandFactoryImpl implements EEAEventCommandFactory {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(EEAEventCommandFactoryImpl.class);

  /**
   * The commands.
   */
  @Autowired(required = false)
  private Set<AbstractEEAEventHandlerCommand> commands;

  /**
   * The event handle commands.
   */
  private Map<EventType, AbstractEEAEventHandlerCommand> eventHandleCommands;

  @Autowired(required = false)
  private DefaultEventHandlerCommand defaultCommand;

  /**
   * Inits the.
   */
  @PostConstruct
  private void init() {
    eventHandleCommands = new HashMap<>();
    if (null != commands) {
      commands.stream()
          .forEach(command -> eventHandleCommands.put(command.getEventType(), command));
    }
  }

  /**
   * Gets the event command.
   *
   * @param message the message
   *
   * @return the event command
   */
  @Override
  public EEAEventHandlerCommand getEventCommand(EEAEventVO message) {

    EventType eventKey = message.getEventType();
    EEAEventHandlerCommand command = null;
    if (this.eventHandleCommands.containsKey(eventKey)) {
      command = this.eventHandleCommands.get(eventKey);
    } else {
      command = defaultCommand;
    }
    return command;

  }
}
