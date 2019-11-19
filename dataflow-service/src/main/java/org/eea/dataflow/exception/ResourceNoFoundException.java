package org.eea.dataflow.exception;

import org.eea.exception.EEAException;

public class ResourceNoFoundException extends EEAException {

  private static final long serialVersionUID = -1947051728807687762L;


  public ResourceNoFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public ResourceNoFoundException(String message) {
    super(message);
  }

  public ResourceNoFoundException() {
    super();
  }

  public ResourceNoFoundException(Throwable cause) {
    super(cause);
  }


}
