package org.eea.recordstore.exception;


/**
 * The Class DockerAccessException.
 */
public class DockerAccessException extends Exception {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -5550710134695719559L;

  /**
   * Instantiates a new docker access exception.
   */
  public DockerAccessException() {
    super();
  }

  /**
   * Instantiates a new docker access exception.
   *
   * @param message the message
   */
  public DockerAccessException(String message) {
    super(message);
  }


  /**
   * Instantiates a new docker access exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public DockerAccessException(String message, Throwable cause) {
    super(message, cause);
  }


  /**
   * Instantiates a new docker access exception.
   *
   * @param cause the cause
   */
  public DockerAccessException(Throwable cause) {
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
  protected DockerAccessException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
