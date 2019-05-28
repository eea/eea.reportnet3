package org.eea.exception;

/**
 * The Class EEAException.
 */
public class EEAException extends Exception {

  private static final long serialVersionUID = 1266830579241817268L;

  /**
   * Instantiates a new EEA exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public EEAException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new EEA exception.
   *
   * @param message the message
   */
  public EEAException(String message) {
    super(message);
  }

  /**
   * Instantiates a new EEA exception.
   */
  public EEAException() {
    super();
  }

  /**
   * Instantiates a new EEA exception.
   *
   * @param cause the cause
   */
  public EEAException(Throwable cause) {
    super(cause);
  }

}
