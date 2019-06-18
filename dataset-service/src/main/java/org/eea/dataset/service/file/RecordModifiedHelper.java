package org.eea.dataset.service.file;

import java.io.IOException;
import java.util.List;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.helper.Helper;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;

/**
 * The Class FileTreatmentHelper.
 */
public class RecordModifiedHelper extends Helper {

  /**
   * Instantiates a new file loader helper.
   */
  public RecordModifiedHelper() {
    super();
  }


  /**
   * Execute update process.
   *
   * @param kafkaSender the kafka sender
   * @param datasetService the dataset service
   * @param datasetId the dataset id
   * @param records the records
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void executeUpdateProcess(final KafkaSender kafkaSender,
      final DatasetService datasetService, final Long datasetId, List<RecordVO> records)
      throws EEAException {
    datasetService.updateRecords(datasetId, records);

    // after the records have been saved, an event is sent to notify it
    releaseDatasetKafkaEvent(kafkaSender, EventType.RECORD_UPDATED_COMPLETED_EVENT, datasetId);
  }

  /**
   * Execute create process.
   *
   * @param kafkaSender the kafka sender
   * @param datasetService the dataset service
   * @param datasetId the dataset id
   * @param records the records
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void executeCreateProcess(final KafkaSender kafkaSender,
      final DatasetService datasetService, final Long datasetId, List<RecordVO> records)
      throws EEAException {
    datasetService.updateRecords(datasetId, records);

    // after the records have been saved, an event is sent to notify it
    releaseDatasetKafkaEvent(kafkaSender, EventType.RECORD_UPDATED_COMPLETED_EVENT, datasetId);
  }

}
