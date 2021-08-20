package org.eea.kafka.io;

import static org.mockito.Mockito.doThrow;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.handler.EEAEventHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;

/**
 * The type Default kafka receiver test.
 */
public class DefaultKafkaReceiverTest {

  @InjectMocks
  private DefaultKafkaReceiver defaultKafkaReceiver;
  /**
   * The Handler.
   */
  @Mock
  protected EEAEventHandler handler;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Listen message test.
   *
   * @throws EEAException the eea exception
   */
  @Test
  public void listenMessageTest() throws EEAException {
    Message<EEAEventVO> messageMock = Mockito.mock(Message.class);
    defaultKafkaReceiver.listenMessage(messageMock);
    Mockito.verify(handler, Mockito.times(1)).processMessage(messageMock.getPayload());

  }

  /**
   * Listen message EEA exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void listenMessageEEAExceptionTest() throws EEAException {
    Message<EEAEventVO> messageMock = Mockito.mock(Message.class);
    doThrow(new EEAException()).when(handler).processMessage(Mockito.any());
    defaultKafkaReceiver.listenMessage(messageMock);
    Mockito.verify(handler, Mockito.times(1)).processMessage(messageMock.getPayload());
  }


  /**
   * Listen message exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void listenMessageExceptionTest() throws EEAException {
    Message<EEAEventVO> messageMock = Mockito.mock(Message.class);
    doThrow(new NullPointerException()).when(handler).processMessage(Mockito.any());
    defaultKafkaReceiver.listenMessage(messageMock);
    Mockito.verify(handler, Mockito.times(1)).processMessage(messageMock.getPayload());
  }
}
