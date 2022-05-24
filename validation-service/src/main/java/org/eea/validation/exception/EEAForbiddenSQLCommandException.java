package org.eea.validation.exception;

import org.eea.exception.EEAException;

/**
 * The Class EEAForbiddenSQLCommandException.
 */
public class EEAForbiddenSQLCommandException extends EEAException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1015001248700152138L;



  /**
   * Instantiates a new invalid SQL exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public EEAForbiddenSQLCommandException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new invalid SQL exception.
   *
   * @param message the message
   */
  public EEAForbiddenSQLCommandException(String message) {
    super(message);
  }

  /**
   * Instantiates a new invalid SQL exception.
   */
  public EEAForbiddenSQLCommandException() {
    super();
  }

  /**
   * Instantiates a new invalid file exception.
   *
   * @param cause the cause
   */
  public EEAForbiddenSQLCommandException(Throwable cause) {
    super(cause);
  }

}
