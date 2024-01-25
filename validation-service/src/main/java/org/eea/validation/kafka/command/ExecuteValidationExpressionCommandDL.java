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
public class ExecuteValidationExpressionCommandDL extends ExecuteValidationCommand {

  @Qualifier("dremioExpressionRulesExecuteServiceImpl")
  @Autowired
  private DremioRulesExecuteService dremioRulesExecuteService;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ExecuteValidationExpressionCommandDL.class);

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COMMAND_VALIDATE_EXPRESSION_DL;
  }

  /**
   * Gets the notification event type.
   *
   * @return the notification event type
   */
  @Override
  public EventType getNotificationEventType() {
    return EventType.COMMAND_VALIDATED_EXPRESSION_DL_COMPLETED;
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
        final boolean createParquetWithSQL = Boolean.valueOf(String.valueOf(eeaEventVO.getData().get("createParquetWithSQL")));
        dremioRulesExecuteService.execute(dataflowId, datasetId, datasetSchemaId, tableName, tableSchemaId, ruleId, dataProviderId, taskId, createParquetWithSQL);
      };
    } catch (Exception e) {
      LOG.error("Unexpected error! Error executing event COMMAND_VALIDATE_EXPRESSION_DL. Message: {}", e.getMessage());
      throw e;
    }
  }
}
