package org.eea.validation.kafka.command;

import org.eea.kafka.domain.EventType;
import org.springframework.stereotype.Component;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 */
@Component
public class CheckRecordValidatedCommand extends CheckValidatedCommand {

  @Override
  public EventType getEventType() {
    return EventType.COMMAND_VALIDATED_RECORD_COMPLETED;
  }


}
