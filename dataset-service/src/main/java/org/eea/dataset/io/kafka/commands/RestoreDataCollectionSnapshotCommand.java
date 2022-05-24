package org.eea.dataset.io.kafka.commands;

import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.persistence.metabase.repository.ChangesEUDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.service.EUDatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class PropagateNewFieldCommand.
 */
@Component
public class RestoreDataCollectionSnapshotCommand extends AbstractEEAEventHandlerCommand {


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

  /** The data collection repository. */
  @Autowired
  private DataCollectionRepository dataCollectionRepository;

  /** The changes repository. */
  @Autowired
  private ChangesEUDatasetRepository changesRepository;



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
  @Transactional
  public void execute(EEAEventVO eeaEventVO) throws EEAException {
    Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataset_id")));
    ThreadPropertiesManager.setVariable("user", String.valueOf(eeaEventVO.getData().get("user")));

    if (datasetSnapshotService.getSnapshotsByIdDataset(datasetId).isEmpty()) {
      Map<String, Object> value = new HashMap<>();
      value.put(LiteralConstants.DATASET_ID, datasetId);
      DataSetMetabaseVO datasetMetabase = datasetMetabaseService.findDatasetMetabase(datasetId);
      Boolean removed =
          euDatasetService.removeLocksRelatedToPopulateEU(datasetMetabase.getDataflowId());

      if (Boolean.TRUE.equals(removed)) {
        kafkaSenderUtils.releaseNotificableKafkaEvent(
            EventType.COPY_DATA_TO_EUDATASET_COMPLETED_EVENT, value,
            NotificationVO.builder()
                .user(SecurityContextHolder.getContext().getAuthentication().getName())
                .datasetId(datasetId).build());

        // delete the datacollections in the table of changes now that the data is copied
        dataCollectionRepository.findByDataflowId(datasetMetabase.getDataflowId()).stream()
            .forEach(dc -> changesRepository.deleteByDatacollection(dc.getId()));
      }
    }

  }

}
