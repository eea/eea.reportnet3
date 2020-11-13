package org.eea.exception;

/**
 * The Class EEABadRequestException.
 */
public class EEAIllegalArgumentException extends EEAException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -3797781436509543L;

  /**
   * Instantiates a new EEA bad request exception.
   */
  public EEAIllegalArgumentException() {}

  /**
   * Instantiates a new EEA bad request exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public EEAIllegalArgumentException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new EEA bad request exception.
   *
   * @param message the message
   */
  public EEAIllegalArgumentException(String message) {
    super(message);
  }

  /**
   * Instantiates a new EEA bad request exception.
   *
   * @param cause the cause
   */
  public EEAIllegalArgumentException(Throwable cause) {
    super(cause);
  }
}
