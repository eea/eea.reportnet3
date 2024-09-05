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
public class ExecuteTableValidationCommand extends ExecuteValidationCommand {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COMMAND_VALIDATE_TABLE;
  }

  /**
   * Gets the notification event type.
   *
   * @return the notification event type
   */
  @Override
  public EventType getNotificationEventType() {
    return EventType.COMMAND_VALIDATED_TABLE_COMPLETED;
  }

  /**
   * Gets the validation action.
   *
   * @return the validation action
   */
  @Override
  public Validator getValidationAction() {
    try {
      return (EEAEventVO eeaEventVO, Long datasetId, KieBase kieBase, Long taskId) -> {
        final Long idTable = Long.parseLong(String.valueOf(eeaEventVO.getData().get("idTable")));
        final String sqlRule = String.valueOf(eeaEventVO.getData().get("sqlRule"));
        final String dataProviderId = String.valueOf(eeaEventVO.getData().get("dataProviderId"));
        validationService.validateTable(datasetId, idTable, kieBase, sqlRule, dataProviderId, taskId);
      };
    } catch (Exception e) {
      LOG_ERROR.error("Unexpected error! Error executing event COMMAND_VALIDATE_TABLE. Message: {}", e.getMessage());
      throw e;
    }
  }
}
