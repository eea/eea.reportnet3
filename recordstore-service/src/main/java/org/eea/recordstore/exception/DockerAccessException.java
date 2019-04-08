package org.eea.recordstore.exception;


public class DockerAccessException extends Exception {

  private static final long serialVersionUID = -5550710134695719559L;

  public DockerAccessException() {
    super();
  }

  public DockerAccessException(String message) {
    super(message);
  }


  public DockerAccessException(String message, Throwable cause) {
    super(message, cause);
  }


  public DockerAccessException(Throwable cause) {
    super(cause);
  }


  protected DockerAccessException(String message, Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
