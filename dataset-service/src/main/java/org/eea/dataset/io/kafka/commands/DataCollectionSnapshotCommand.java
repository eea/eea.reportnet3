package org.eea.dataset.io.kafka.commands;

import java.util.List;
import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.eea.dataset.persistence.metabase.domain.EUDataset;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.EUDatasetRepository;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class PropagateNewFieldCommand.
 */
@Component
public class DataCollectionSnapshotCommand extends AbstractEEAEventHandlerCommand {


  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  @Autowired
  private EUDatasetRepository euDatasetRepository;

  @Autowired
  private DataCollectionRepository dataCollectionRepository;

  @Autowired
  private DatasetSnapshotService datasetSnapshotService;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.ADD_DATACOLLECTION_SNAPSHOT_COMPLETED_EVENT;
  }

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   * @throws EEAException
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) throws EEAException {
    Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataset_id")));
    Long snapshotId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("snapshot_id")));
    ThreadPropertiesManager.setVariable("user", String.valueOf(eeaEventVO.getData().get("user")));

    DataCollection dataCollection = dataCollectionRepository.findById(datasetId).orElse(null);
    if (dataCollection != null) {
      List<EUDataset> euDatasetList = euDatasetRepository.findByDataflowIdAndDatasetSchema(
          dataCollection.getDataflowId(), dataCollection.getDatasetSchema());
      if (!euDatasetList.isEmpty()) {
        datasetSnapshotService.restoreSnapshotToCloneData(dataCollection.getId(),
            euDatasetList.get(0).getId(), snapshotId, true, DatasetTypeEnum.EUDATASET);
      }
    }
  }

}
