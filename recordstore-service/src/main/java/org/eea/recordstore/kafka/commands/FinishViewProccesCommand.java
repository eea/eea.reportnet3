package org.eea.recordstore.kafka.commands;

import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.recordstore.util.ViewHelper;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class FinishViewProccesCommand.
 */
@Component
public class FinishViewProccesCommand extends AbstractEEAEventHandlerCommand {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");
  private static final Logger LOG = LoggerFactory.getLogger(ViewHelper.class);

  /** The view helper. */
  @Autowired
  private ViewHelper viewHelper;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.FINISH_VIEW_PROCCES_EVENT;
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
      Boolean isMaterialized =
              Boolean.parseBoolean(String.valueOf(eeaEventVO.getData().get("isMaterialized")));
      Boolean checkSQL = Boolean.parseBoolean(String.valueOf(eeaEventVO.getData().get("checkSQL")));
      LOG.info("VAGOS inside FinishViewProccesCommand.execute with datasetId : {}", datasetId);
      viewHelper.finishProcces(datasetId, isMaterialized, checkSQL);
    } catch (Exception e) {
      LOG_ERROR.error("Unexpected error! Error executing event {}. Message: {}", eeaEventVO, e.getMessage());
      throw e;
    }
  }

}
