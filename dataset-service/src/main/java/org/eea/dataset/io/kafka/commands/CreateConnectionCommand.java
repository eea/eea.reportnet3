package org.eea.dataset.io.kafka.commands;

import org.apache.commons.lang.StringUtils;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The Class CreateConnectionCommand.
 */
@Component
public class CreateConnectionCommand extends AbstractEEAEventHandlerCommand {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The dataset service. */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.CONNECTION_CREATED_EVENT;
  }

  /**
   * Perform action.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) {

    // When there is a new dataset created, if there is idSchema in the event, call to
    // datasetService.insertSchema
    // to insert that values into the dataset_X.dataset_value and dataset_X.table_value
    if (EventType.CONNECTION_CREATED_EVENT.equals(eeaEventVO.getEventType())) {

      // if there is idDatasetSchema, insert it into the corresponding dataset_value
      String dataset = (String) eeaEventVO.getData().get("dataset_id");
      String idDatasetSchema = (String) eeaEventVO.getData().get("idDatasetSchema");
      if (StringUtils.isNotBlank(dataset) && StringUtils.isNotBlank(idDatasetSchema)) {
        try {
          String[] aux = dataset.split("_");
          Long idDataset = Long.valueOf(aux[aux.length - 1]);
          datasetService.insertSchema(idDataset, idDatasetSchema);

          // First insert of the statistics
          try {
            datasetService.saveStatistics(idDataset);
          } catch (EEAException e) {
            LOG_ERROR.error("Error saving the statistics. Error message: {}", e.getMessage(), e);
          }

        } catch (EEAException e) {
          LOG_ERROR.error(
              "Error executing the processes after creating a new empty dataset. Error message: {}",
              e.getMessage(), e);
        }
      }
    }
  }
}
