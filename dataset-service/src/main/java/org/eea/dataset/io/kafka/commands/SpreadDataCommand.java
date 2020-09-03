package org.eea.dataset.io.kafka.commands;

import java.util.List;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.service.DatasetService;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class SpreadDataCommand.
 */
@Component
public class SpreadDataCommand extends AbstractEEAEventHandlerCommand {

  /** The data set metabase repository. */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The design dataset repository. */
  @Autowired
  private DesignDatasetRepository designDatasetRepository;

  /** The dataset service. */
  @Autowired
  private DatasetService datasetService;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.SPREAD_DATA_EVENT;
  }

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) {
    String stringDataset = (String) eeaEventVO.getData().get("dataset_id");
    String idDatasetSchema = (String) eeaEventVO.getData().get("idDatasetSchema");
    Long dataset = Long.valueOf(stringDataset);
    Long dataflowId = dataSetMetabaseRepository.findDataflowIdById(dataset);
    List<DesignDataset> designs = designDatasetRepository.findByDataflowId(dataflowId);
    boolean isdesing = false;
    if (!designs.isEmpty()) {
      for (DesignDataset design : designs) {
        if (design.getId().equals(dataset)) {
          isdesing = true;
        }
      }
      if (!isdesing) {
        datasetService.spreadDataPrefill(designs, dataset, idDatasetSchema);
      }
    }
  }
}
