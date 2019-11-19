package org.eea.kafka.utils;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class KafkaSenderUtilsTest {

  @InjectMocks
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private KafkaSender kafkaSender;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);

  }

  @Test
  public void releaseDatasetKafkaEvent() {
    kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, 1l);
    Mockito.verify(kafkaSender, Mockito.times(1)).sendMessage(Mockito.any(EEAEventVO.class));
  }

  @Test
  public void releaseKafkaEvent() {
    final Map<String, Object> dataOutput = new HashMap<>();
    dataOutput.put("dataset_id", 1l);
    kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, dataOutput);
    Mockito.verify(kafkaSender, Mockito.times(1)).sendMessage(Mockito.any(EEAEventVO.class));
  }
}