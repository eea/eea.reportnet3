package org.eea.dataflow.exception;

import org.eea.exception.EEAException;

/**
 * The Class WrongDataExceptions.
 */
public class WrongDataExceptions extends EEAException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -7962420073539191953L;


  /**
   * Instantiates a new wrong data exceptions.
   *
   * @param message the message
   * @param cause the cause
   */
  public WrongDataExceptions(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new wrong data exceptions.
   *
   * @param message the message
   */
  public WrongDataExceptions(String message) {
    super(message);
  }

  /**
   * Instantiates a new wrong data exceptions.
   */
  public WrongDataExceptions() {
    super();
  }

  /**
   * Instantiates a new wrong data exceptions.
   *
   * @param cause the cause
   */
  public WrongDataExceptions(Throwable cause) {
    super(cause);
  }


}
