package org.eea.validation.kafka.command;

import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.kie.api.KieBase;
import org.springframework.stereotype.Component;


/**
 * The Class ExecuteTableValidationCommand.
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

  /**
   * Gets the notification event type.
   *
   * @return the notification event type
   */
  @Override
  public EventType getNotificationEventType() {
    return EventType.COMMAND_VALIDATED_TABLE_COMPLETED;
  }

  /**
   * Gets the validation action.
   *
   * @return the validation action
   */
  @Override
  public Validator getValidationAction() {
    return (EEAEventVO eeaEventVO, Long datasetId, KieBase kieBase) -> {
      final Long idTable = Long.parseLong(String.valueOf(eeaEventVO.getData().get("idTable")));
      final String sqlRule = String.valueOf(eeaEventVO.getData().get("sqlRule"));
      validationService.validateTable(datasetId, idTable, kieBase, sqlRule);
    };
  }
}
