package org.eea.validation.kafka;

import static org.mockito.Mockito.times;
import org.eea.exception.EEAException;
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
import org.springframework.messaging.MessageHeaders;

/**
 * The Class CommandKafkaReceiverTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class CommandKafkaReceiverTest {

  /** The command kafka receiver. */
  @InjectMocks
  private CommandKafkaReceiver commandKafkaReceiver;

  /** The handler. */
  @Mock
  private EEAEventHandler handler;

  /** The message. */
  private Message<EEAEventVO> message;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
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
   * Listen message test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void listenMessageTest() throws EEAException {
    commandKafkaReceiver.listenMessage(message);
    Mockito.verify(handler, times(1)).processMessage(Mockito.any());
  }
}
