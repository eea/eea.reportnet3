package org.eea.dataset.service.file;

import java.io.IOException;
import java.io.InputStream;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.helper.Helper;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;

/**
 * The Class FileTreatmentHelper.
 */
public class FileTreatmentHelper extends Helper {

  /**
   * Instantiates a new file loader helper.
   */
  public FileTreatmentHelper() {
    super();
  }

  /**
   * Execute file process.
   *
   * @param kafkaSender the kafka sender
   * @param datasetService the dataset service
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param is the input stream
   * @param idTableSchema the id table schema
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InterruptedException the interrupted exception
   */
  public static void executeFileProcess(final KafkaSender kafkaSender,
      final DatasetService datasetService, final Long datasetId, final String fileName,
      final InputStream is, String idTableSchema)
      throws EEAException, IOException, InterruptedException {
    datasetService.processFile(datasetId, fileName, is, idTableSchema);

    // after the dataset has been saved, an event is sent to notify it
    releaseDatasetKafkaEvent(kafkaSender, EventType.LOAD_DATA_COMPLETED_EVENT, datasetId);
  }

}
