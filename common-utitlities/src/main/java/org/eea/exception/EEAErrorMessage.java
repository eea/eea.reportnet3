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
   * The Constant DATASET_NOTFOUND.
   */
  public static final String DATASET_NOTFOUND = "Dataset not found";

  /**
   * The constant DATASET_INCORRECT_ID.
   */
  public static final String DATASET_INCORRECT_ID = "Dataset Id incorrect";

  /**
   * The constant EXECUTION_ERROR.
   */
  public static final String EXECUTION_ERROR = "Error during execution of background process";

  private EEAErrorMessage() {
    throw new IllegalStateException("Utility class");
  }

}
