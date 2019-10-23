package org.eea.validation.kafka.command;

import java.util.UUID;
import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.validation.util.ValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 */
@Component
public class ExecuteValidationCommand extends AbstractEEAEventHandlerCommand {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The validation helper.
   */
  @Autowired
  private ValidationHelper validationHelper;


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COMMAND_EXECUTE_VALIDATION;
  }

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  @LockMethod(removeWhenFinish = false)
  public void execute(@LockCriteria EEAEventVO eeaEventVO) {
    Long datasetId = (Long) eeaEventVO.getData().get("dataset_id");
    try {
      validationHelper.executeValidation(datasetId, UUID.randomUUID().toString());
    } catch (EEAException e) {
      LOG_ERROR.error("Error processing validations for dataset {} due to exception {}", datasetId,
          e);
    }
  }


}
