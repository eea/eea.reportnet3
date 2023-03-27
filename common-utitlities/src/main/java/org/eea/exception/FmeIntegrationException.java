package org.eea.exception;

/**
 * The Class FmeIntegrationException.
 */
public class FmeIntegrationException extends Exception {
    private static final long serialVersionUID = 1266830579241817268L;

    /**
     * Instantiates a new FME exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public FmeIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new FME exception.
     *
     * @param message the message
     */
    public FmeIntegrationException(String message) {
        super(message);
    }

    /**
     * Instantiates a new FME exception.
     */
    public FmeIntegrationException() {
        super();
    }

    /**
     * Instantiates a new FME exception.
     *
     * @param cause the cause
     */
    public FmeIntegrationException(Throwable cause) {
        super(cause);
    }

}
