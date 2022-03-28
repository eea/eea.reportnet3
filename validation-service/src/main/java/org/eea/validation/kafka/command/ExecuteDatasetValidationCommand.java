package org.eea.validation.kafka.command;

import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.kie.api.KieBase;
import org.springframework.stereotype.Component;

/**
 * The Class ExecuteDatasetValidationCommand.
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

  /**
   * Gets the notification event type.
   *
   * @return the notification event type
   */
  @Override
  public EventType getNotificationEventType() {
    return EventType.COMMAND_VALIDATED_DATASET_COMPLETED;
  }

  /**
   * Gets the validation action.
   *
   * @return the validation action
   */
  @Override
  public Validator getValidationAction() {
    return (EEAEventVO eeaEventVO, Long datasetId, KieBase kieBase,
        Long taskId) -> validationService.validateDataSet(datasetId, kieBase, taskId);
  }


}
