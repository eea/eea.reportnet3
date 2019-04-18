package org.eea.kafka.handler;

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
   */
  void processMessage(T message);

}
