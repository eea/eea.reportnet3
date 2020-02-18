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

  /** The Constant IDDATASETSCHEMA_INCORRECT. */
  public static final String IDDATASETSCHEMA_INCORRECT = "DatasetSchemaId incorrect";

  /** The Constant RULEID_INCORRECT. */
  public static final String RULEID_INCORRECT = "ruleId incorrect";

  /** The Constant REFERENCEID_INCORRECT. */
  public static final String REFERENCEID_INCORRECT = "referenceId incorrect";

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

  /** The Constant DATAFLOW_EXISTS_NAME. */
  public static final String DATAFLOW_EXISTS_NAME = "Dataflow name already exists";

  /** The Constant DATAFLOW_NOTFOUND. */
  public static final String DATAFLOW_NOTFOUND = "Dataflow not found";

  /** The Constant DATAFLOW_CRITERIA. */
  public static final String DATAFLOW_DESCRIPTION_NAME = "Dataflow Description or Name empty";

  /** The Constant USER_REQUEST_NOTFOUND. */
  public static final String USER_REQUEST_NOTFOUND = "User request not found";

  /** The Constant SCHEMA_ID_NONFOUND. */
  public static final String SCHEMA_NOT_FOUND = "Schema not found";

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

  /** The Constant DATE_AFTER_INCORRECT. */
  public static final String DATE_AFTER_INCORRECT = "The date has to be later than today's date";

  /** The Constant FIELD_NOTFOUND. */
  public static final String FIELD_NOT_FOUND = "Field not found";

  /** The Constant FIELD_SCHEMA_ID_NOT_FOUND. */
  public static final String FIELD_SCHEMA_ID_NOT_FOUND = "fieldSchemaId not found";

  /** The Constant WRONG_DATASET_SCHEMA. */
  public static final String INVALID_OBJECTID = "A given ObjectId is not valid";

  /** The Constant FIELD_NAME_NULL. */
  public static final String FIELD_NAME_NULL = "The name of fieldSchema should be filled";

  /** The Constant FORBIDDEN. */
  public static final String FORBIDDEN = "The user has no permissions";

  /** The Constant ID_LINK_NOT_FOUND. */
  public static final String ID_LINK_NOT_FOUND = "WebLink not found";

  /** The Constant NOT_ENOUGH_PERMISSION. */
  public static final String NOT_ENOUGH_PERMISSION = "Not enough permission to perform";

  /** The Constant METHOD_LOCKED. */
  public static final String METHOD_LOCKED = "Method locked";

  /** The Constant REPRESENTATIVE_TYPE_INCORRECT. */
  public static final String REPRESENTATIVE_TYPE_INCORRECT = "Representative type incorrect";

  /** The Constant REPRESENTATIVE_NOT_FOUND. */
  public static final String REPRESENTATIVE_NOT_FOUND = "Representative not found";

  /** The Constant REPRESENTATIVE_DUPLICATED. */
  public static final String REPRESENTATIVE_DUPLICATED = "Representative duplicated";

  /** The Constant CODELIST_NOT_FOUND. */
  public static final String CODELIST_NOT_FOUND = "Codelist not found";

  /** The Constant REPRESENTATIVE_DUPLICATED. */
  public static final String DATA_COLLECTION_NOT_CREATED = "Data Collection not created";

  /** The Constant CODELIST_CATEGORY_NOT_FOUND. */
  public static final String CODELIST_CATEGORY_NOT_FOUND = "Codelist category not found";

  /** The Constant CODELIST_VERSION_DUPLICATED. */
  public static final String CODELIST_VERSION_DUPLICATED = "Codelist version duplicated";

  /** The Constant ERROR_DELETING_RULE. */
  public static final String ERROR_DELETING_RULE = "Error deleting rules";

  /** The Constant ERROR_CREATING_RULE. */
  public static final String ERROR_CREATING_RULE = "Error creating rules";

  /**
   * Instantiates a new EEA error message.
   */
  private EEAErrorMessage() {
    throw new IllegalStateException("Utility class");
  }

}
