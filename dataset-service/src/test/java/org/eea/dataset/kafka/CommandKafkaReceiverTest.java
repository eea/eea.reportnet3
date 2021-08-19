package org.eea.dataset.kafka;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.handler.EEAEventHandler;
import org.eea.kafka.io.KafkaReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The Class CommandKafkaReceiverTest.
 */
public class CommandKafkaReceiverTest {

  /** The command kafka receiver. */
  @InjectMocks
  private CommandKafkaReceiver commandKafkaReceiver;

  /** The kafka receiver. */
  @Mock
  private KafkaReceiver kafkaReceiver;

  /** The e EA event handler. */
  @Mock
  private EEAEventHandler eEAEventHandler;

  /** The message. */
  private Message<EEAEventVO> message;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
    ReflectionTestUtils.setField(kafkaReceiver, "handler", eEAEventHandler);
    message = new Message<EEAEventVO>() {

      @Override
      public EEAEventVO getPayload() {
        return null;
      }

      @Override
      public MessageHeaders getHeaders() {
        return null;
      }
    };
  }

  /**
   * Test listen message.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testListenMessage() throws EEAException {
    commandKafkaReceiver.listenMessage(message);
    Mockito.verify(eEAEventHandler, times(1)).processMessage(Mockito.any());
  }

  /**
   * Test listen message EEA exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testListenMessageEEAException() throws EEAException {
    doThrow(new EEAException()).when(eEAEventHandler).processMessage(Mockito.any());
    commandKafkaReceiver.listenMessage(message);
    Mockito.verify(eEAEventHandler, times(1)).processMessage(Mockito.any());
  }


}
