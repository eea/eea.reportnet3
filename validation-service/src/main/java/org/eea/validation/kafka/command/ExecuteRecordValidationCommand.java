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
public class ExecuteRecordValidationCommand extends ExecuteValidationCommand {

  /**
   * The record batch size.
   */
  @Value("${validation.recordBatchSize}")
  private int recordBatchSize;

  @Override
  public EventType getNotificationEventType() {
    return EventType.COMMAND_VALIDATED_RECORD_COMPLETED;
  }

  @Override
  public Validator getValidationAction() {
    return (EEAEventVO eeaEventVO, Long datasetId, KieBase kieBase) -> {
      final int numPag = (int) eeaEventVO.getData().get("numPag");
      Pageable pageable = PageRequest.of(numPag, recordBatchSize);
      validationService.validateRecord(datasetId, kieBase, pageable);
    };
  }

  @Override
  public EventType getEventType() {
    return EventType.COMMAND_VALIDATE_RECORD;
  }

}
