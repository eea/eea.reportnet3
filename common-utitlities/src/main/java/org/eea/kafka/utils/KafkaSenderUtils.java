
package org.eea.kafka.utils;

import java.util.HashMap;
import java.util.Map;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class KafkaSenderHelper.
 *
 * @author ruben.lozano
 */
@Component
public class KafkaSenderUtils {

  /**
   * The kafka sender.
   */
  @Autowired
  private KafkaSender kafkaSender;

  /**
   * Release Dataset kafka event.
   *
   * @param eventType the event type
   * @param datasetId the dataset id
   */
  public void releaseDatasetKafkaEvent(final EventType eventType, final Long datasetId) {
    final Map<String, Object> dataOutput = new HashMap<>();
    dataOutput.put("dataset_id", datasetId);
    releaseKafkaEvent(eventType, dataOutput);
  }

  /**
   * Release kafka event.
   *
   * @param eventType the event type
   * @param value the value
   */
  public void releaseKafkaEvent(final EventType eventType, final Map<String, Object> value) {
    final EEAEventVO event = new EEAEventVO();
    event.setEventType(eventType);
    event.setData(value);
    kafkaSender.sendMessage(event);
  }
}
