package org.eea.validation.kafka.command;

import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.kie.api.KieBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * The Class ExecuteTableValidationCommand.
 */
@Component
public class ExecuteEmptyRuleValidationCommand extends ExecuteValidationCommand {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ExecuteEmptyRuleValidationCommand.class);

  private static void performValidation(EEAEventVO eeaEventVO, Long datasetId, KieBase kieBase, Long taskId) {
    LOG.info("Dataset {} has no rules enabled", datasetId);
  }

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COMMAND_VALIDATE_EMPTY_RULE;
  }

  /**
   * Gets the notification event type.
   *
   * @return the notification event type
   */
  @Override
  public EventType getNotificationEventType() {
    return EventType.COMMAND_VALIDATED_EMPTY_RULE_COMPLETED;
  }

  /**
   * Gets the validation action.
   *
   * @return the validation action
   */
  @Override
  public Validator getValidationAction() {
    try {
      return ExecuteEmptyRuleValidationCommand::performValidation;
    } catch (Exception e) {
      LOG.error("Unexpected error! Error executing event COMMAND_VALIDATE_DL_EMPTY_RULE. Message: {}", e.getMessage());
      throw e;
    }
  }
}
