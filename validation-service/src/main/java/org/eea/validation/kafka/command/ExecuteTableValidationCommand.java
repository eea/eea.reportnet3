package org.eea.validation.kafka.command;

import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.kie.api.KieBase;
import org.springframework.stereotype.Component;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 */
@Component
public class ExecuteTableValidationCommand extends ExecuteValidationCommand {


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COMMAND_VALIDATE_TABLE;
  }

  @Override
  public EventType getNotificationEventType() {
    return EventType.COMMAND_VALIDATED_TABLE_COMPLETED;
  }

  @Override
  public Validator getValidationAction() {
    return (EEAEventVO eeaEventVO, Long datasetId, KieBase kieBase) -> {
      final Long idTable = Long.parseLong(String.valueOf(eeaEventVO.getData().get("idTable")));
      final String processId = String.valueOf(eeaEventVO.getData().get("processId"));
      validationService.validateTable(datasetId, idTable, kieBase, processId);
    };
  }
}
