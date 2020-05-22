package org.eea.validation.kafka.command;

import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.multitenancy.TenantResolver;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.ValidationHelper;
import org.kie.api.KieBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 */
public abstract class ExecuteValidationCommand extends AbstractEEAEventHandlerCommand {

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
   * The validation service.
   */
  @Autowired
  @Qualifier("proxyValidationService")
  protected ValidationService validationService;

  /**
   * The kafka sender utils.
   */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

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

    try {
      KieBase kieBase = validationHelper.getKieBase(processId, datasetId);
      getValidationAction().performValidation(eeaEventVO, datasetId, kieBase);
    } catch (EEAException e) {
      LOG_ERROR.error("Error processing validations for dataset {} due to exception {}", datasetId,
          e);
      eeaEventVO.getData().put("error", e);
    } finally {
      // if this is the coordinator validation instance, then no need to send message, just to update
      // expected pending ok's and verify if process is finished

      if (validationHelper.isProcessCoordinator(processId)) {
        // if it's not finished a message with the next task will be sent as part of the reducePendingTasks execution
        validationHelper.reducePendingTasks(datasetId, processId);
      } else {// send the message to coordinator validation instance
        kafkaSenderUtils.releaseKafkaEvent(getNotificationEventType(),
            eeaEventVO.getData());
      }
    }
  }
}
