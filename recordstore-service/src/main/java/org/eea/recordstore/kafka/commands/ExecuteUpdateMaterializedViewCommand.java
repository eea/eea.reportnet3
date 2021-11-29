package org.eea.recordstore.kafka.commands;

import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class ExecuteUpdateMaterialicedViewCommand.
 */
@Component
public class ExecuteUpdateMaterializedViewCommand extends AbstractEEAEventHandlerCommand {

  /** The database management service. */
  @Autowired
  private RecordStoreService recordStoreService;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.UPDATE_MATERIALIZED_VIEW_EVENT;
  }

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

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
    String user = String.valueOf(eeaEventVO.getData().get(LiteralConstants.USER));
    Boolean released = Boolean.parseBoolean(String.valueOf(eeaEventVO.getData().get("released")));
    recordStoreService.updateMaterializedQueryView(datasetId, user, released);
  }

}
