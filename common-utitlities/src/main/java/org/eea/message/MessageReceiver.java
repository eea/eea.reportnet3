package org.eea.message;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;

/**
 * The Class MessageReceiver.
 */

public abstract class MessageReceiver {

  /** The handler. */
  @Autowired(required = false)
  protected EEAEventHandler handler;



  /**
   * Receive message.
   *
   * @param message the message
   * @throws EEAException the EEA exception
   */

  public abstract void consumeMessage(Message<EEAEventVO> message) throws EEAException;

}
