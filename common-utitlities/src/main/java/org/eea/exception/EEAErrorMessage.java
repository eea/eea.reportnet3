package org.eea.exception;

/**
 * Instantiates a new EEA error message.
 */
public final class EEAErrorMessage {

  /**
   * The Constant DB_FILEFORMAT.
   */
  public static final String FILE_FORMAT = "Invalid file format";

  /**
   * The Constant FILE_EXTENSION.
   */
  public static final String FILE_EXTENSION = "File without extension";

  /**
   * The Constant FILE_EXTENSION.
   */
  public static final String FILE_NAME = "Error getting the file name";

  /**
   * The Constant DATASET_NOTFOUND.
   */
  public static final String DATASET_NOTFOUND = "Dataset not found";

  /**
   * The constant DATASET_INCORRECT_ID.
   */
  public static final String DATASET_INCORRECT_ID = "Dataset Id incorrect";

  /** The Constant EXECUTION_ERROR. */
  public static final String EXECUTION_ERROR = "Runtime error";

  /** The Constant VALIDATION_SESSION_ERROR. */
  public static final String VALIDATION_SESSION_ERROR = "Validation session error";

  /** The Constant FILE_NOT_FOUND. */
  public static final String FILE_NOT_FOUND = "File not found in the application";

  /** The Constant FILE_NOT_FOUND. */
  public static final String RECORD_NOTFOUND = "Record not found";

  public static final String PARTITION_ID_NOTFOUND = "Partition not found";

  /**
   * Instantiates a new EEA error message.
   */
  private EEAErrorMessage() {
    throw new IllegalStateException("Utility class");
  }

}
