package org.eea.dataset.io.kafka.commands;

import java.io.IOException;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class PrefillingReferenceDatasetSnapshotCommand.
 */
@Component
public class PrefillingReferenceDatasetSnapshotCommand extends AbstractEEAEventHandlerCommand {


  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The file treatment helper. */
  @Autowired
  private FileTreatmentHelper fileTreatmentHelper;

  /** The dataset metabase repository. */
  @Autowired
  private DataSetMetabaseRepository datasetMetabaseRepository;

  /** The record store controller zuul. */
  @Autowired
  private RecordStoreControllerZuul recordStoreControllerZuul;

  /** The dataset schema repository. */
  @Autowired
  private SchemasRepository datasetSchemaRepository;


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.RESTORE_PREFILLING_REFERENCE_SNAPSHOT_COMPLETED_EVENT;
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
    DataSetMetabase dataset = datasetMetabaseRepository.findById(datasetId).orElse(null);
    if (null != dataset) {
      try {
        DataSetSchema schema =
            datasetSchemaRepository.findByIdDataSetSchema(new ObjectId(dataset.getDatasetSchema()));
        fileTreatmentHelper.updateGeometry(datasetId, schema);

        Thread.sleep(10000);

        fileTreatmentHelper.createReferenceDatasetFiles(dataset);
        recordStoreControllerZuul.refreshMaterializedView(datasetId, null);
      } catch (IOException | InterruptedException e) {
        LOG_ERROR.error(
            "Error creating the reference dataset {} files during the creation. Error: {}",
            datasetId, e.getMessage(), e);
      }
    }

  }

}
