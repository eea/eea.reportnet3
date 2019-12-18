package org.eea.kafka.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.kafka.common.PartitionInfo;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * The Class KafkaSenderTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class KafkaSenderTest {

  /**
   * The kafka sender.
   */
  @InjectMocks
  private KafkaSender kafkaSender;

  /**
   * The kafka template.
   */
  @Mock
  private KafkaTemplate<String, EEAEventVO> kafkaTemplate;

  /**
   * Inits the mocks.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test full test.
   *
   * @throws Exception the exception
   */
  @Test(expected = Exception.class)
  public void testFullTest() throws Exception {
    EEAEventVO event = new EEAEventVO();
    List<PartitionInfo> infoList = new ArrayList<>();
    PartitionInfo partition = new PartitionInfo("1", 1, null, null, null);
    infoList.add(partition);
    event.setEventType(EventType.LOAD_DATA_COMPLETED_EVENT);
    kafkaSender.sendMessage(event);
  }

}
