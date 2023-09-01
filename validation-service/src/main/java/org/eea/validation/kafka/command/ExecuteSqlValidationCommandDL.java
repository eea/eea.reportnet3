package org.eea.validation.kafka.command;

import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.validation.service.DremioRulesExecuteService;
import org.kie.api.KieBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


/**
 * The Class ExecuteTableValidationCommand.
 */
@Component
public class ExecuteSqlValidationCommandDL extends ExecuteValidationCommand {

  @Qualifier("dremioSqlRulesExecuteServiceImpl")
  @Autowired
  private DremioRulesExecuteService dremioRulesExecuteService;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COMMAND_VALIDATE_DL_WITH_SQL;
  }

  /**
   * Gets the notification event type.
   *
   * @return the notification event type
   */
  @Override
  public EventType getNotificationEventType() {
    return EventType.COMMAND_VALIDATED_DL_WITH_SQL_COMPLETED;
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
        final Long dataflowId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataflowId")));
        final String tableName = String.valueOf(eeaEventVO.getData().get("tableName"));
        final String ruleId = String.valueOf(eeaEventVO.getData().get("ruleId"));
        final Long dataProviderId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataProviderId")));
        final String datasetSchemaId = String.valueOf(eeaEventVO.getData().get("datasetSchema"));
        final String tableSchemaId = String.valueOf(eeaEventVO.getData().get("tableSchemaId"));
        dremioRulesExecuteService.execute(dataflowId, datasetId, datasetSchemaId, tableName, tableSchemaId, ruleId, dataProviderId, taskId);
      };
    } catch (Exception e) {
      LOG_ERROR.error("Unexpected error! Error executing event COMMAND_VALIDATE_DL_WITH_SQL. Message: {}", e.getMessage());
      throw e;
    }
  }
}
