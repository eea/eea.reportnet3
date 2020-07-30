package org.eea.kafka.handler;

import org.eea.exception.EEAException;

/**
 * The interface Message handler.
 *
 * @param <T> the type parameter
 */
public interface MessageHandler<T> {

  /**
   * Gets type.
   *
   * @return the type
   */
  Class<T> getType();

  /**
   * Process message.
   *
   * @param message the message
   * @throws EEAException the EEA exception
   */
  void processMessage(T message) throws EEAException;

}
