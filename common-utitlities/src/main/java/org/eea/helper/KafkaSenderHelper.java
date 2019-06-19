
package org.eea.helper;

import java.util.HashMap;
import java.util.Map;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;

/**
 * The Class Helper.
 *
 * @author ruben.lozano
 */
public class KafkaSenderHelper {

  /**
   * Release Dataset kafka event.
   *
   * @param kafkaSender the kafka sender
   * @param eventType the event type
   * @param datasetId the dataset id
   */
  protected static void releaseDatasetKafkaEvent(final KafkaSender kafkaSender,
      final EventType eventType, final Long datasetId) {
    releaseKafkaEvent(kafkaSender, eventType, datasetId, "dataset_id");

  }

  /**
   * Release kafka event.
   *
   * @param kafkaSender the kafka sender
   * @param eventType the event type
   * @param id the id
   * @param value the value
   */
  protected static void releaseKafkaEvent(final KafkaSender kafkaSender, final EventType eventType,
      final Long id, final String value) {
    final EEAEventVO event = new EEAEventVO();
    event.setEventType(eventType);
    final Map<String, Object> dataOutput = new HashMap<>();
    dataOutput.put(value, id);
    event.setData(dataOutput);
    kafkaSender.sendMessage(event);
  }
}
