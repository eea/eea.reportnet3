package org.eea.validation.kafka.command;

import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.kie.api.KieBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 */
@Component
public class ExecuteFieldValidationCommand extends ExecuteValidationCommand {


  /**
   * The field batch size.
   */
  @Value("${validation.fieldBatchSize}")
  private int fieldBatchSize;

  @Override
  public EventType getEventType() {
    return EventType.COMMAND_VALIDATE_FIELD;
  }


  @Override
  public EventType getNotificationEventType() {
    return EventType.COMMAND_VALIDATED_FIELD_COMPLETED;
  }

  @Override
  public Validator getValidationAction() {
    return (EEAEventVO eeaEventVO, Long datasetId, KieBase kieBase) -> {
      final int numPag = (int) eeaEventVO.getData().get("numPag");
      Pageable pageable = PageRequest.of(numPag, fieldBatchSize);
      boolean onlyEmptyFields = (boolean) eeaEventVO.getData().get("onlyEmptyFields");
      Long dataProviderId = (Long) eeaEventVO.getData().get("dataProviderId");
      String datasetSchema = (String) eeaEventVO.getData().get("datasetSchema");
      validationService.validateFields(datasetId, kieBase, pageable, onlyEmptyFields,
          dataProviderId, datasetSchema);
    };
  }


}
