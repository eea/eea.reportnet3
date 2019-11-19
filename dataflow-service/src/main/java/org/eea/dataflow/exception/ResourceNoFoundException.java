package org.eea.dataflow.exception;

import org.eea.exception.EEAException;

/**
 * The Class ResourceNoFoundException.
 */
public class ResourceNoFoundException extends EEAException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -1947051728807687762L;


  /**
   * Instantiates a new resource no found exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public ResourceNoFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new resource no found exception.
   *
   * @param message the message
   */
  public ResourceNoFoundException(String message) {
    super(message);
  }

  /**
   * Instantiates a new resource no found exception.
   */
  public ResourceNoFoundException() {
    super();
  }

  /**
   * Instantiates a new resource no found exception.
   *
   * @param cause the cause
   */
  public ResourceNoFoundException(Throwable cause) {
    super(cause);
  }


}
