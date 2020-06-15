package org.eea.validation.kafka.command;

import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.multitenancy.TenantResolver;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.ValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 */
public abstract class ExecuteValidationCommand extends AbstractEEAEventHandlerCommand {


  /**
   * The validation helper.
   */
  @Autowired
  private ValidationHelper validationHelper;

  /**
   * The validation service.
   */
  @Autowired
  @Qualifier("proxyValidationService")
  protected ValidationService validationService;

  /**
   * Gets the event type.
   *
   * @return the event type
   */

  @Override
  public abstract EventType getEventType();

  /**
   * Retrieves the type of Kafka Event to be released once the execution of the validation is done.
   * This value will be used if and only if this command is NOT executed by the process coordinator
   *
   * @return the notification event type
   */
  public abstract EventType getNotificationEventType();

  /**
   * Gets validation action.
   *
   * @return the validation action
   */
  public abstract Validator getValidationAction();


  /**
   * Perform action.
   *
   * @param eeaEventVO the eea event VO
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void execute(final EEAEventVO eeaEventVO) throws EEAException {
    final Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataset_id")));
    final String processId = (String) eeaEventVO.getData().get("uuid");
    TenantResolver.setTenantName("dataset_" + datasetId);

    validationHelper
        .processValidation(eeaEventVO, processId, datasetId, this.getValidationAction(),
            this.getNotificationEventType());
  }
}
