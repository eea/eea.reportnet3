package org.eea.kafka.io;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.handler.EEAEventHandler;
import org.junit.Assert;
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
    MockitoAnnotations.initMocks(this);
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


}
