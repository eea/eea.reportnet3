package org.eea.exception;

/**
 * The Class EEARuntimeException.
 */
public class EEARuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1266830579241817268L;


  /**
   * Instantiates a new Eea runtime exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public EEARuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new Eea runtime exception.
   *
   * @param message the message
   */
  public EEARuntimeException(String message) {
    super(message);
  }

  /**
   * Instantiates a new Eea runtime exception.
   */
  public EEARuntimeException() {
    super();
  }

  /**
   * Instantiates a new Eea runtime exception.
   *
   * @param cause the cause
   */
  public EEARuntimeException(Throwable cause) {
    super(cause);
  }

}
