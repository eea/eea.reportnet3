package org.eea.dataset.service.callable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.eea.dataset.service.DatasetService;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;

/**
 * The type Load data callable.
 */
public class SaveValidationsCallable implements Callable<Void> {

  /** The dataset. */
  private DataSetVO dataset;

  /** The dataset service. */
  private DatasetService datasetService;

  /** The kafka sender. */
  private KafkaSender kafkaSender;

  /**
   * Instantiates a new save validations callable.
   *
   * @param datasetService the dataset service
   * @param dataset the dataset
   * @param kafkaSender the kafka sender
   */
  public SaveValidationsCallable(final DatasetService datasetService, final DataSetVO dataset,
      final KafkaSender kafkaSender) {
    this.datasetService = datasetService;
    this.dataset = dataset;
    this.kafkaSender = kafkaSender;

  }

  /**
   * Call.
   *
   * @return the void
   * @throws Exception the exception
   */
  @Override
  public Void call() throws Exception {
    datasetService.updateDataset(dataset.getId(), dataset);
    // after the dataset has been saved, an event is sent to notify it
    releaseKafkaEvent(kafkaSender, EventType.VALIDATION_FINISHED_EVENT, dataset.getId());
    return null;
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
