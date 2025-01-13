package org.eea.exception;

public class DremioApiException extends Exception {
    private static final long serialVersionUID = 1266830579241817268L;

    /**
     * Instantiates a new Dremio Api exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public DremioApiException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Dremio Api exception.
     *
     * @param message the message
     */
    public DremioApiException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Dremio Api exception.
     */
    public DremioApiException() {
        super();
    }

    /**
     * Instantiates a new Dremio Api exception.
     *
     * @param cause the cause
     */
    public DremioApiException(Throwable cause) {
        super(cause);
    }

}
