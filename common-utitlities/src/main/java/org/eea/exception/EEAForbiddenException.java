package org.eea.exception;

/**
 * The Class EEAForbiddenException.
 */
public class EEAForbiddenException extends EEAException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 4646732627597079884L;

  /**
   * Instantiates a new EEA forbidden exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public EEAForbiddenException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new EEA forbidden exception.
   *
   * @param message the message
   */
  public EEAForbiddenException(String message) {
    super(message);
  }

  /**
   * Instantiates a new EEA forbidden exception.
   */
  public EEAForbiddenException() {}

  /**
   * Instantiates a new EEA forbidden exception.
   *
   * @param cause the cause
   */
  public EEAForbiddenException(Throwable cause) {
    super(cause);
  }
}
