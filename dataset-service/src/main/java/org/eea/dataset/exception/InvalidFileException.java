package org.eea.dataset.exception;

import org.eea.exception.EEAException;

/**
 * The Class InvalidFileException.
 */
public class InvalidFileException extends EEAException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1266830579241817268L;

  /** The Constant ERROR_MESSAGE. */
  public static final String ERROR_MESSAGE = "Invalid Format File";

  /**
   * Instantiates a new invalid file exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public InvalidFileException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new invalid file exception.
   *
   * @param message the message
   */
  public InvalidFileException(String message) {
    super(message);
  }

  /**
   * Instantiates a new invalid file exception.
   */
  public InvalidFileException() {
    super();
  }

  /**
   * Instantiates a new invalid file exception.
   *
   * @param cause the cause
   */
  public InvalidFileException(Throwable cause) {
    super(cause);
  }

}
