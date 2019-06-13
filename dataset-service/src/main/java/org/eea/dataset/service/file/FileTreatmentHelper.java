package org.eea.dataset.service.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;

/**
 * The Class FileLoaderHelper.
 */
public class FileTreatmentHelper {

  /**
   * Instantiates a new file loader helper.
   */
  public FileTreatmentHelper() {}

  /**
   * Execute file process.
   *
   * @param kafkaSender the kafka sender
   * @param datasetService the dataset service
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param is the input stream
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
    releaseKafkaEvent(kafkaSender, EventType.LOAD_DATA_COMPLETED_EVENT, datasetId);
  }

  /**
   * Release kafka event.
   *
   * @param eventType the event type
   * @param datasetId the dataset id
   */
  private static void releaseKafkaEvent(final KafkaSender kafkaSender, final EventType eventType,
      final Long datasetId) {

    final EEAEventVO event = new EEAEventVO();
    event.setEventType(eventType);
    final Map<String, Object> dataOutput = new HashMap<>();
    dataOutput.put("dataset_id", datasetId);
    event.setData(dataOutput);
    kafkaSender.sendMessage(event);
  }
}
