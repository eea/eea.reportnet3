package org.eea.exception;

/**
 * Instantiates a new EEA error message.
 */
public class EEAErrorMessage {

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

  private EEAErrorMessage() {
    throw new IllegalStateException("Utility class");
  }

}
