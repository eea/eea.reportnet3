package org.eea.exception;

/**
 * The Class EEAUnauthorizedException.
 */
public class EEAUnauthorizedException extends EEAException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 2666837025964134520L;

  /**
   * Instantiates a new EEA unauthorized exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public EEAUnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new EEA unauthorized exception.
   *
   * @param message the message
   */
  public EEAUnauthorizedException(String message) {
    super(message);
  }

  /**
   * Instantiates a new EEA unauthorized exception.
   */
  public EEAUnauthorizedException() {}

  /**
   * Instantiates a new EEA unauthorized exception.
   *
   * @param cause the cause
   */
  public EEAUnauthorizedException(Throwable cause) {
    super(cause);
  }
}
