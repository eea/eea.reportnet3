package org.eea.validation.exception;

import org.eea.exception.EEAException;

/**
 * The Class EEAInvalidSQLCommentsException.
 */
public class EEAInvalidSQLCommentsException extends EEAException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 139711384193913014L;

  /**
   * Instantiates a new invalid SQL exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public EEAInvalidSQLCommentsException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new invalid SQL exception.
   *
   * @param message the message
   */
  public EEAInvalidSQLCommentsException(String message) {
    super(message);
  }

  /**
   * Instantiates a new invalid SQL exception.
   */
  public EEAInvalidSQLCommentsException() {
    super();
  }

  /**
   * Instantiates a new invalid file exception.
   *
   * @param cause the cause
   */
  public EEAInvalidSQLCommentsException(Throwable cause) {
    super(cause);
  }

}
