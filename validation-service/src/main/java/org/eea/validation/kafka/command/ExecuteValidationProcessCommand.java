package org.eea.validation.kafka.command;

import java.util.UUID;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.validation.util.ValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 */
@Component
public class ExecuteValidationProcessCommand extends AbstractEEAEventHandlerCommand {

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
   * Perform action.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) {
    Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataset_id")));
    ThreadPropertiesManager.setVariable("user", eeaEventVO.getData().get("user"));
    Boolean updateViews = Boolean.TRUE;
    if (null != eeaEventVO.getData().get("updateViews")) {
      updateViews = Boolean.parseBoolean(String.valueOf(eeaEventVO.getData().get("updateViews")));
    }
    Boolean released = null != eeaEventVO.getData().get("released")
        ? Boolean.parseBoolean(String.valueOf(eeaEventVO.getData().get("released")))
        : Boolean.FALSE;

    validationHelper.executeValidation(datasetId, UUID.randomUUID().toString(), released,
        updateViews);
  }
}
