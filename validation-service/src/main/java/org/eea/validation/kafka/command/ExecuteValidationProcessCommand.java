package org.eea.validation.kafka.command;

import org.eea.exception.EEAException;
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
   * @throws EEAException the EEA exception
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) throws EEAException {
    Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataset_id")));
    ThreadPropertiesManager.setVariable("user", eeaEventVO.getData().get("user"));
    Object aux = eeaEventVO.getData().get("updateViews");
    String processId = String.valueOf(eeaEventVO.getData().get("processId"));
    boolean updateViews = !(aux instanceof Boolean) || (boolean) aux;
    aux = eeaEventVO.getData().get("released");
    boolean released = aux instanceof Boolean && (boolean) aux;

    // Add lock to the release process if necessary
    validationHelper.executeValidation(datasetId, processId, released, updateViews);
    validationHelper.addLockToReleaseProcess(datasetId);
  }


}
