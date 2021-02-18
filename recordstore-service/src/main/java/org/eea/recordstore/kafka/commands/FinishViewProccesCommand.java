package org.eea.recordstore.kafka.commands;

import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.recordstore.util.ViewHelper;
import org.eea.utils.LiteralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class FinishViewProccesCommand.
 */
@Component
public class FinishViewProccesCommand extends AbstractEEAEventHandlerCommand {

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
    Long datasetId =
        Long.parseLong(String.valueOf(eeaEventVO.getData().get(LiteralConstants.DATASET_ID)));
    Boolean isMaterialized =
        Boolean.parseBoolean(String.valueOf(eeaEventVO.getData().get("isMaterialized")));
    Boolean checkSQL = Boolean.parseBoolean(String.valueOf(eeaEventVO.getData().get("checkSQL")));

    viewHelper.finishProcces(datasetId, isMaterialized, checkSQL);
  }

}
