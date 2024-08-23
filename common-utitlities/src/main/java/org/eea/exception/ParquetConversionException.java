package org.eea.exception;

public class ParquetConversionException extends Exception {

    private static final long serialVersionUID = 1266830579241817268L;

    /**
     * Instantiates a new Parquet Conversion exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public ParquetConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Parquet Conversion exception.
     *
     * @param message the message
     */
    public ParquetConversionException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Parquet Conversion exception.
     */
    public ParquetConversionException() {
        super();
    }

    /**
     * Instantiates a new Parquet Conversion exception.
     *
     * @param cause the cause
     */
    public ParquetConversionException(Throwable cause) {
        super(cause);
    }

}
