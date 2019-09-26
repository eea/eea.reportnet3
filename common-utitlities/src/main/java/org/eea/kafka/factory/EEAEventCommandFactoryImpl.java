package org.eea.kafka.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.eea.kafka.commands.AbstracEEAEventHandlerCommand;
import org.eea.kafka.commands.EEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("EEAEventCommandFactoryImpl")
public class EEAEventCommandFactoryImpl implements EEAEventCommandFactory {

  private static final Logger LOG = LoggerFactory.getLogger(EEAEventCommandFactoryImpl.class);

  @Autowired(required = false)
  private Set<AbstracEEAEventHandlerCommand> commands;

  private Map<EventType, AbstracEEAEventHandlerCommand> eventHandleCommands;

  @PostConstruct
  private void init() {
    eventHandleCommands = new HashMap<>();
    if (null != commands) {
      commands.stream().forEach(command -> {
        eventHandleCommands.put(command.getEventType(), command);
      });
    }
  }

  /**
   * Gets the event command.
   *
   * @param message the message
   * @return the event command
   */
  @Override
  public EEAEventHandlerCommand getEventCommand(EEAEventVO message) {

    EventType eventKey = message.getEventType();
    EEAEventHandlerCommand command = null;
    if (this.eventHandleCommands.containsKey(eventKey)) {
      command = this.eventHandleCommands.get(eventKey);
    }
    return command;

  }
}
