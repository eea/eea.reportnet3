package org.eea.kafka.io;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.handler.EEAEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;

/**
 * The type Kafka receiver.
 */

public abstract class KafkaReceiver {

  /**
   * The handler.
   */
  @Autowired(required = false)
  protected EEAEventHandler handler;



  /**
   * Listen message.
   *
   * @param message the message
   */

  public abstract void listenMessage(Message<EEAEventVO> message) throws EEAException;

}
