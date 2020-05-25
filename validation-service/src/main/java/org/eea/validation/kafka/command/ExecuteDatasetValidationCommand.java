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
public class ExecuteDatasetValidationCommand extends ExecuteValidationCommand {


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COMMAND_VALIDATE_DATASET;
  }

  @Override
  public EventType getNotificationEventType() {
    return EventType.COMMAND_VALIDATED_DATASET_COMPLETED;
  }

  @Override
  public Validator getValidationAction() {
    return (EEAEventVO eeaEventVO, Long datasetId, KieBase kieBase) -> {
      validationService.validateDataSet(datasetId, kieBase);
    };
  }


}
