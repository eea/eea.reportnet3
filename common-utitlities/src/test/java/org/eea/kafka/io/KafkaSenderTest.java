package org.eea.kafka.io;

import java.io.IOException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test full test.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFullTest() throws Exception {

    EEAEventVO event = new EEAEventVO();
    event.setEventType(EventType.IMPORT_REPORTING_COMPLETED_EVENT);

    Mockito.when(kafkaTemplate.executeInTransaction(Mockito.any())).thenReturn(true);
    kafkaSender.sendMessage(event);
    Mockito.verify(kafkaTemplate, Mockito.times(1)).executeInTransaction(Mockito.any());
  }

}
