package org.eea.recordstore.exception;


/**
 * The Class DockerAccessException.
 */
public class RecordStoreAccessException extends Exception {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -5550710134695719559L;

  /**
   * Instantiates a new docker access exception.
   */
  public RecordStoreAccessException() {
    super();
  }

  /**
   * Instantiates a new docker access exception.
   *
   * @param message the message
   */
  public RecordStoreAccessException(String message) {
    super(message);
  }


  /**
   * Instantiates a new docker access exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public RecordStoreAccessException(String message, Throwable cause) {
    super(message, cause);
  }


  /**
   * Instantiates a new docker access exception.
   *
   * @param cause the cause
   */
  public RecordStoreAccessException(Throwable cause) {
    super(cause);
  }


  /**
   * Instantiates a new docker access exception.
   *
   * @param message the message
   * @param cause the cause
   * @param enableSuppression the enable suppression
   * @param writableStackTrace the writable stack trace
   */
  protected RecordStoreAccessException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
