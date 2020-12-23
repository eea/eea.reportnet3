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

  /**
   * The data set metabase repository.
   */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /**
   * The design dataset repository.
   */
  @Autowired
  private DesignDatasetRepository designDatasetRepository;

  /**
   * The dataset service.
   */
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
    String stringTargetDataset = (String) eeaEventVO.getData().get("dataset_id");
    String idDatasetSchema = (String) eeaEventVO.getData().get("idDatasetSchema");
    Long targetDatasetId = Long.valueOf(stringTargetDataset);
    Long dataflowId = dataSetMetabaseRepository.findDataflowIdById(targetDatasetId);
    List<DesignDataset> designs = designDatasetRepository.findByDataflowId(dataflowId);
    boolean isDesignDataset = false;
    DesignDataset originDatasetDesign = null;
    if (!designs.isEmpty()) {
      for (DesignDataset design : designs) {
        if (design.getId().equals(targetDatasetId)) {
          isDesignDataset = true; //target datasetId is a design, break loop as it will not be processed
          break;
        }
        if (idDatasetSchema.equals(design.getDatasetSchema())) {
          originDatasetDesign = design;
        }
      }
      if (!isDesignDataset && null
          != originDatasetDesign) {//target dataset is a reporting dataset and we have found the design dataset with data to be copied into the target dataset
        datasetService.spreadDataPrefill(originDatasetDesign, targetDatasetId);
      }
    }
  }
}
