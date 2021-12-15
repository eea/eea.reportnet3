package org.eea.dataset.io.kafka.commands;

import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * The Class CreateConnectionCommand.
 */
@Component
public class SaveStatisticsCommand extends AbstractEEAEventHandlerCommand {


  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /**
   * The dataset service.
   */
  @Autowired
  @Lazy
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.VALIDATION_FINISHED_EVENT;
  }

  /**
   * Perform action.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) {
    final Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataset_id")));
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          datasetService.saveStatistics(datasetId);
        } catch (EEAException e) {
          LOG_ERROR.error("Error saving statistics. Error message: {}", e.getMessage(), e);
        }

      }

    }).start();

  }


}
