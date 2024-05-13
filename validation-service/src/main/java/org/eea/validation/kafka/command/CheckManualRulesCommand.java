package org.eea.validation.kafka.command;

import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.utils.LiteralConstants;
import org.eea.validation.service.RulesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class CheckManualRulesCommand.
 */
@Component
public class CheckManualRulesCommand extends AbstractEEAEventHandlerCommand {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(CheckManualRulesCommand.class);

  @Autowired
  private RulesService rulesService;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.VALIDATE_MANUAL_QC_COMMAND;
  }

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) throws EEAException {
    try {
      Long datasetId =
              Long.parseLong(String.valueOf(eeaEventVO.getData().get(LiteralConstants.DATASET_ID)));
      boolean checkNoSQL = (boolean) eeaEventVO.getData().get("checkNoSQL");
      String user = (String) eeaEventVO.getData().get("user");

      // validate all rules (SQL and non-SQL rules), then send a notification
      rulesService.validateAllRules(datasetId, checkNoSQL, user);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error executing event {}. Message: {}", eeaEventVO, e.getMessage());
      throw e;
    }
  }

}
