package org.eea.dataset.exception;

import org.eea.exception.EEAException;

/**
 * The Class InvalidJsonException.
 */
public class InvalidJsonException extends EEAException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1266830579241817268L;

  /** The Constant ERROR_MESSAGE. */
  public static final String ERROR_MESSAGE = "Invalid json";

  /**
   * Instantiates a new invalid json exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public InvalidJsonException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new invalid json exception.
   *
   * @param message the message
   */
  public InvalidJsonException(String message) {
    super(message);
  }

  /**
   * Instantiates a new invalid json exception.
   */
  public InvalidJsonException() {
    super();
  }

  /**
   * Instantiates a new invalid json exception.
   *
   * @param cause the cause
   */
  public InvalidJsonException(Throwable cause) {
    super(cause);
  }

}
