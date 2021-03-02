package org.eea.dataset.kafka.io.event;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.multitenancy.TenantResolver;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


/**
 * The Class CreateConnectionEvent.
 */
@Component
public class CreateConnectionEvent extends AbstractEEAEventHandlerCommand {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The dataset service. */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

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

    String dataset = (String) eeaEventVO.getData().get(LiteralConstants.DATASET_ID);
    String idDatasetSchema = (String) eeaEventVO.getData().get(LiteralConstants.ID_DATASET_SCHEMA);
    if (StringUtils.isNotBlank(dataset) && StringUtils.isNotBlank(idDatasetSchema)) {
      try {
        String[] aux = dataset.split("_");
        Long idDataset = Long.valueOf(aux[aux.length - 1]);
        TenantResolver
            .setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, idDataset));
        // Initialize the dataset values (insert datasetId and tables into dataset_value and
        // table_value of the new schema)
        // datasetService.insertSchema(idDataset, idDatasetSchema);
        // First insert of the statistics
        // datasetService.saveStatistics(idDataset);

        datasetService.initializeDataset(idDataset, idDatasetSchema);

        Map<String, Object> result = new HashMap<>();
        result.put(LiteralConstants.DATASET_ID, idDataset.toString());
        result.put(LiteralConstants.ID_DATASET_SCHEMA, idDatasetSchema);
        if (DatasetTypeEnum.REPORTING.equals(datasetService.getDatasetType(idDataset))) {
          sendEvent(result);
        }
      } catch (EEAException e) {
        LOG_ERROR.error(
            "Error executing the processes after creating a new empty dataset. Error message: {}",
            e.getMessage(), e);
      }
    } else {
      LOG_ERROR.error(
          "Error creating the processes creating a new dataset connection because of the null datasetId or idDatasetSchema. DatasetId: {}. IdDatasetSchema: {}",
          dataset, idDatasetSchema);
    }
  }


  /**
   * Send event spread Data to copy prefilled tables in reporting datasets and data collection.
   *
   * @param result the result
   */
  private void sendEvent(Map<String, Object> result) {
    kafkaSenderUtils.releaseKafkaEvent(EventType.SPREAD_DATA_EVENT, result);

  }
}
