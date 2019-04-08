package org.eea.dataset.multitenancy;

public class TransactionExecutionException extends RuntimeException {

  private static final long serialVersionUID = -9010701052790164042L;

  public TransactionExecutionException() {
    super();
  }

  public TransactionExecutionException(String message) {
    super(message);
  }

  public TransactionExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

  public TransactionExecutionException(Throwable cause) {
    super(cause);
  }

  protected TransactionExecutionException(String message, Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
