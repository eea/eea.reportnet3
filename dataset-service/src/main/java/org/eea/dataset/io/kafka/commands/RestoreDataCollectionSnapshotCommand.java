package org.eea.dataset.io.kafka.commands;

import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.service.EUDatasetService;
import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class PropagateNewFieldCommand.
 */
@Component
public class RestoreDataCollectionSnapshotCommand extends AbstractEEAEventHandlerCommand {


  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The dataset snapshot service. */
  @Autowired
  private DatasetSnapshotService datasetSnapshotService;

  /** The eu dataset service. */
  @Autowired
  private EUDatasetService euDatasetService;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.RESTORE_DATACOLLECTION_SNAPSHOT_COMPLETED_EVENT;
  }

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) throws EEAException {
    Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataset_id")));
    ThreadPropertiesManager.setVariable("user", String.valueOf(eeaEventVO.getData().get("user")));

    if (datasetSnapshotService.getSnapshotsByIdDataset(datasetId).isEmpty()) {
      Map<String, Object> value = new HashMap<>();
      value.put(LiteralConstants.DATASET_ID, datasetId);
      euDatasetService.removeLocksRelatedToPopulateEU(
          datasetMetabaseService.findDatasetMetabase(datasetId).getDataflowId());

      kafkaSenderUtils.releaseNotificableKafkaEvent(
          EventType.COPY_DATA_TO_EUDATASET_COMPLETED_EVENT, value,
          NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
              .datasetId(datasetId).build());
    }

  }

}
