package org.eea.validation.service.impl;


import java.util.HashMap;
import java.util.Map;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;
import org.eea.validation.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class ValidationService.
 */
@Service
public class ValidationServiceImpl implements ValidationService {

  @Autowired
  private KafkaSender kafkaSender;

  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void validateDataSetData(Long datasetId) {

    final EEAEventVO event = new EEAEventVO();
    event.setEventType(EventType.VALIDATION_FINISHED_EVENT);
    final Map<String, Object> dataOutput = new HashMap<>();
    // TODO ADD DATA TO MAP
    event.setData(dataOutput);
    kafkaSender.sendMessage(event);
  }
}
