package org.eea.validation.exception;

import org.eea.exception.EEAException;

/**
 * The Class InvalidSQLException.
 */
public class EEAInvalidSQLException extends EEAException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -3274224431190917014L;

  /**
   * Instantiates a new invalid SQL exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public EEAInvalidSQLException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new invalid SQL exception.
   *
   * @param message the message
   */
  public EEAInvalidSQLException(String message) {
    super(message);
  }

  /**
   * Instantiates a new invalid SQL exception.
   */
  public EEAInvalidSQLException() {
    super();
  }

  /**
   * Instantiates a new invalid file exception.
   *
   * @param cause the cause
   */
  public EEAInvalidSQLException(Throwable cause) {
    super(cause);
  }

}
