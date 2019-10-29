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
   * The Constant DATASET_UNKNOW_TYPE.
   */
  public static final String DATASET_UNKNOW_TYPE = "Unknown Dataset type";

  /**
   * The constant DATASET_INCORRECT_ID.
   */
  public static final String DATASET_INCORRECT_ID = "Dataset Id incorrect";

  /** The Constant IDTABLESCHEMA_INCORRECT. */
  public static final String IDTABLESCHEMA_INCORRECT = "TableSchemaId incorrect";

  /** The Constant EXECUTION_ERROR. */
  public static final String EXECUTION_ERROR = "Runtime error";

  /** The Constant VALIDATION_SESSION_ERROR. */
  public static final String VALIDATION_SESSION_ERROR = "Validation session error";

  /** The Constant FILE_NOT_FOUND. */
  public static final String FILE_NOT_FOUND = "File not found in the application";

  /** The Constant FILE_NOT_FOUND. */
  public static final String RECORD_NOTFOUND = "Record not found";

  /** The Constant PARTITION_ID_NOTFOUND. */
  public static final String PARTITION_ID_NOTFOUND = "Partition not found";

  /** The Constant DATAFLOW_INCORRECT_ID. */
  public static final String DATAFLOW_INCORRECT_ID = "Dataflow Id incorrect";

  /** The Constant DATAFLOW_NOTFOUND. */
  public static final String DATAFLOW_NOTFOUND = "Dataflow not found";

  /** The Constant USER_REQUEST_NOTFOUND. */
  public static final String USER_REQUEST_NOTFOUND = "User request not found";

  /** The Constant SCHEMA_ID_NONFOUND. */
  public static final String SCHEMA_ID_NONFOUND = "IdSchema not found";

  /** The Constant DOCUMENT_UPLOAD_ERROR. */
  public static final String DOCUMENT_UPLOAD_ERROR = "Document upload error";

  /** The Constant DOCUMENT_DOWNLOAD_ERROR. */
  public static final String DOCUMENT_DOWNLOAD_ERROR = "Document download error";

  /** The Constant TABLE_NOT_FOUND. */
  public static final String TABLE_NOT_FOUND = "Table not found";

  /** The Constant DOCUMENT_NOT_FOUND. */
  public static final String DOCUMENT_NOT_FOUND = "Document not found";

  /** The Constant REPOSITORY_NOT_FOUND. */
  public static final String REPOSITORY_NOT_FOUND = "Repository not found";

  /** The Constant URL_FORMAT_INCORRECT. */
  public static final String URL_FORMAT_INCORRECT = "Format url is incorrect";

  /** The Constant ID_LINK_INCORRECT. */
  public static final String ID_LINK_INCORRECT = "Id link is incorrect";

  /** The Constant DATE_FORMAT_INCORRECT. */
  public static final String DATE_FORMAT_INCORRECT = "Format date is incorrect";

  public static final String DATE_AFTER_INCORRECT = "The date has to be later than today's date";

  /** The Constant FIELD_NOTFOUND. */
  public static final String FIELD_NOT_FOUND = "Field not found";

  /**
   * Instantiates a new EEA error message.
   */
  private EEAErrorMessage() {
    throw new IllegalStateException("Utility class");
  }

}
