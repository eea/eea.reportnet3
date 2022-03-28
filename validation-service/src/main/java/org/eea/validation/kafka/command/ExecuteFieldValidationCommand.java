package org.eea.validation.kafka.command;

import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.kie.api.KieBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * The Class ExecuteFieldValidationCommand.
 */
@Component
public class ExecuteFieldValidationCommand extends ExecuteValidationCommand {


  /** The field batch size. */
  @Value("${validation.fieldBatchSize}")
  private int fieldBatchSize;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COMMAND_VALIDATE_FIELD;
  }


  /**
   * Gets the notification event type.
   *
   * @return the notification event type
   */
  @Override
  public EventType getNotificationEventType() {
    return EventType.COMMAND_VALIDATED_FIELD_COMPLETED;
  }

  /**
   * Gets the validation action.
   *
   * @return the validation action
   */
  @Override
  public Validator getValidationAction() {
    return (EEAEventVO eeaEventVO, Long datasetId, KieBase kieBase, Long taskId) -> {
      final int numPag = (int) eeaEventVO.getData().get("numPag");
      Pageable pageable = PageRequest.of(numPag, fieldBatchSize);
      boolean onlyEmptyFields = (boolean) eeaEventVO.getData().get("onlyEmptyFields");
      validationService.validateFields(datasetId, kieBase, pageable, onlyEmptyFields, taskId);
    };
  }


}
