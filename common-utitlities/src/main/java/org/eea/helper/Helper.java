
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
public class Helper {

  /**
   * Release kafka event.
   *
   * @param kafkaSender the kafka sender
   * @param eventType the event type
   * @param datasetId the dataset id
   */
  protected static void releaseDatasetKafkaEvent(final KafkaSender kafkaSender, final EventType eventType,
      final Long datasetId) {

    final EEAEventVO event = new EEAEventVO();
    event.setEventType(eventType);
    final Map<String, Object> dataOutput = new HashMap<>();
    dataOutput.put("dataset_id", datasetId);
    event.setData(dataOutput);
    kafkaSender.sendMessage(event);
  }
}
