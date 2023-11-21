package org.eea.exception;

/**
 * The Class EEAException.
 */
public class DremioValidationException extends Exception {

  private static final long serialVersionUID = 1266830579241817268L;

  /**
   * Instantiates a new EEA exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public DremioValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new EEA exception.
   *
   * @param message the message
   */
  public DremioValidationException(String message) {
    super(message);
  }

  /**
   * Instantiates a new EEA exception.
   */
  public DremioValidationException() {
    super();
  }

  /**
   * Instantiates a new EEA exception.
   *
   * @param cause the cause
   */
  public DremioValidationException(Throwable cause) {
    super(cause);
  }

}
