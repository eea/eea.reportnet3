package org.eea.dataset.io.kafka.commands;

import java.util.List;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.ReferenceDataset;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.ReferenceDatasetRepository;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.thread.ThreadPropertiesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReferenceDatasetSnapshotCommand extends AbstractEEAEventHandlerCommand {



  @Autowired
  private ReferenceDatasetRepository referenceDatasetRepository;


  @Autowired
  private DesignDatasetRepository designDatasetRepository;

  /** The dataset snapshot service. */
  @Autowired
  private DatasetSnapshotService datasetSnapshotService;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COPY_REFERENCE_DATASET_SNAPSHOT_COMPLETED_EVENT;
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
    Long snapshotId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("snapshot_id")));
    ThreadPropertiesManager.setVariable("user", String.valueOf(eeaEventVO.getData().get("user")));

    DesignDataset designDataset = designDatasetRepository.findById(datasetId).orElse(null);

    if (designDataset != null) {
      List<ReferenceDataset> referenceDatasets =
          referenceDatasetRepository.findByDataflowIdAndDatasetSchema(designDataset.getDataflowId(),
              designDataset.getDatasetSchema());
      if (!referenceDatasets.isEmpty()) {
        datasetSnapshotService.restoreSnapshotToCloneData(designDataset.getId(),
            referenceDatasets.get(0).getId(), snapshotId, true, DatasetTypeEnum.REFERENCE, true);
      }
    }

  }

}
