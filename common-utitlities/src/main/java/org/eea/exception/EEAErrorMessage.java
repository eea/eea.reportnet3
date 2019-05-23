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

  public static final String DATASET_INCORRECT_ID = "Dataset Id incorrect";

  private EEAErrorMessage() {
    throw new IllegalStateException("Utility class");
  }

}
