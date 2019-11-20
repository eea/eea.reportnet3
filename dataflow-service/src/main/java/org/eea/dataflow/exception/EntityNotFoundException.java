package org.eea.dataflow.exception;

import org.eea.exception.EEAException;

/**
 * The Class EntityNotFoundException.
 */
public class EntityNotFoundException extends EEAException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -7747510450705288327L;

  /**
   * Instantiates a new entity not found exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public EntityNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }


  /**
   * Instantiates a new entity not found exception.
   *
   * @param message the message
   */
  public EntityNotFoundException(String message) {
    super(message);
  }

  /**
   * Instantiates a new entity not found exception.
   */
  public EntityNotFoundException() {
    super();
  }

  /**
   * Instantiates a new entity not found exception.
   *
   * @param cause the cause
   */
  public EntityNotFoundException(Throwable cause) {
    super(cause);
  }

}
