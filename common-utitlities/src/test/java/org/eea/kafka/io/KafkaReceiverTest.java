package org.eea.kafka.io;

import static org.mockito.Mockito.times;

import java.io.IOException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.handler.EEAEventHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.Message;


/**
 * The Class KafkaReceiverTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class KafkaReceiverTest {

  /**
   * The message.
   */
  @Mock
  private Message<EEAEventVO> message;

  /**
   * The handler.
   */
  @Mock
  private EEAEventHandler handler;

  /**
   * The kafka receiver.
   */
  @InjectMocks
  private KafkaReceiver kafkaReceiver;

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
   * Test listen message.
   */
  @Test
  public void testListenMessage() {
    kafkaReceiver.listenMessage(message);
    Mockito.verify(handler, times(1)).processMessage(Mockito.any());
  }

}
