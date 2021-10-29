package org.eea.dataset.kafka.io.event;

import org.apache.commons.lang.StringUtils;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.service.DatasetService;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
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

  /** The table repository. */
  @Autowired
  private TableRepository tableRepository;

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
      String[] aux = dataset.split("_");
      Long idDataset = Long.valueOf(aux[aux.length - 1]);
      TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, idDataset));
      // Dataset data and statistics initialization. Check first if this is not an error and the
      // dataset is already initialized
      if (tableRepository.count() > 0) {
        LOG_ERROR.error("The dataset {} is already initialized", idDataset);
      } else {
        datasetService.initializeDataset(idDataset, idDatasetSchema);
      }

    } else {
      LOG_ERROR.error(
          "Error creating the processes creating a new dataset connection because of the null datasetId or idDatasetSchema. DatasetId: {}. IdDatasetSchema: {}",
          dataset, idDatasetSchema);
    }
  }



}
