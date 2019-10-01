package org.eea.validation.kafka.command;

import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.service.ValidationService;
import org.kie.api.KieBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 *
 */
@Component
public class ExecuteFieldValidationCommand extends AbstractEEAEventHandlerCommand {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The validation service. */
  @Autowired
  @Qualifier("proxyValidationService")
  private ValidationService validationService;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

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
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(final EEAEventVO eeaEventVO) {
    final Long datasetId = (Long) eeaEventVO.getData().get("dataset_id");
    final KieBase kieBase = (KieBase) eeaEventVO.getData().get("kieBase");
    final int numPag = (int) eeaEventVO.getData().get("numPag");
    try {
      Pageable pageable = PageRequest.of(numPag, fieldBatchSize);
      validationService.validateFields(datasetId, kieBase, pageable);
      kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_VALIDATED_FIELD_COMPLETED,
          eeaEventVO.getData());

    } catch (EEAException e) {
      LOG_ERROR.error("Error processing validations for dataset {} due to exception {}", datasetId,
          e);
    }
  }

}
