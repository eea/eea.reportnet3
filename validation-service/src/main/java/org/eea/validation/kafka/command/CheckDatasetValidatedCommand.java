package org.eea.validation.kafka.command;

import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.util.ValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 */
@Component
public class CheckDatasetValidatedCommand extends CheckValidatedCommand {


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COMMAND_VALIDATED_DATASET_COMPLETED;
  }


}
