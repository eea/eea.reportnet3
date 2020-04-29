package org.eea.exception;

/**
 * Instantiates a new EEA error message.
 */
public final class EEAErrorMessage {

  /** Invalid file format */
  public static final String FILE_FORMAT = "Invalid file format";

  /** Not a valid date */
  public static final String INVALID_DATE = "Not a valid date";

  /** Not a DESIGN dataflow */
  public static final String NOT_DESIGN_DATAFLOW = "Not a DESIGN dataflow";

  /** Not a DRAFT dataflow */
  public static final String NOT_DRAFT_DATAFLOW = "Not a DRAFT dataflow";

  /** File without extension */
  public static final String FILE_EXTENSION = "File without extension";

  /** Error getting the file name */
  public static final String FILE_NAME = "Error getting the file name";

  /** Dataset not found */
  public static final String DATASET_NOTFOUND = "Dataset not found";

  /** Unknown Dataset type */
  public static final String DATASET_UNKNOW_TYPE = "Unknown Dataset type";

  /** Dataset Id incorrect */
  public static final String DATASET_INCORRECT_ID = "Dataset Id incorrect";

  /** TableSchemaId incorrect */
  public static final String IDTABLESCHEMA_INCORRECT = "TableSchemaId incorrect";

  /** DatasetSchemaId incorrect */
  public static final String IDDATASETSCHEMA_INCORRECT = "DatasetSchemaId incorrect";

  /** ruleId incorrect */
  public static final String RULEID_INCORRECT = "ruleId incorrect";

  /** referenceId incorrect */
  public static final String REFERENCEID_INCORRECT = "referenceId incorrect";

  /** Runtime error */
  public static final String EXECUTION_ERROR = "Runtime error";

  /** Validation session error */
  public static final String VALIDATION_SESSION_ERROR = "Validation session error";

  /** File not found in the application */
  public static final String FILE_NOT_FOUND = "File not found in the application";

  /** Record not found */
  public static final String RECORD_NOTFOUND = "Record not found";

  /** Partition not found */
  public static final String PARTITION_ID_NOTFOUND = "Partition not found";

  /** Dataflow Id incorrect */
  public static final String DATAFLOW_INCORRECT_ID = "Dataflow Id incorrect";

  /** Dataflow name already exists */
  public static final String DATAFLOW_EXISTS_NAME = "Dataflow name already exists";

  /** Dataflow not found */
  public static final String DATAFLOW_NOTFOUND = "Dataflow not found";

  /** Dataflow Description or Name empty */
  public static final String DATAFLOW_DESCRIPTION_NAME = "Dataflow Description or Name empty";

  /** Dataflow Obligation empty */
  public static final String DATAFLOW_OBLIGATION = "Dataflow Obligation empty";

  /** User request not found */
  public static final String USER_REQUEST_NOTFOUND = "User request not found";

  /** User not found */
  public static final String USER_NOTFOUND = "User not found";

  /** Schema not found */
  public static final String SCHEMA_NOT_FOUND = "Schema not found";

  /** Document upload error */
  public static final String DOCUMENT_UPLOAD_ERROR = "Document upload error";

  /** Document download error */
  public static final String DOCUMENT_DOWNLOAD_ERROR = "Document download error";

  /** Table not found */
  public static final String TABLE_NOT_FOUND = "Table not found";

  /** Document not found */
  public static final String DOCUMENT_NOT_FOUND = "Document not found";

  /** Repository not found */
  public static final String REPOSITORY_NOT_FOUND = "Repository not found";

  /** Format url is incorrect */
  public static final String URL_FORMAT_INCORRECT = "Format url is incorrect";

  /** Id link is incorrect */
  public static final String ID_LINK_INCORRECT = "Id link is incorrect";

  /** Format date is incorrect */
  public static final String DATE_FORMAT_INCORRECT = "Format date is incorrect";

  /** The date has to be later than today's date */
  public static final String DATE_AFTER_INCORRECT = "The date has to be later than today's date";

  /** Field not found */
  public static final String FIELD_NOT_FOUND = "Field not found";

  /** fieldSchemaId not found */
  public static final String FIELD_SCHEMA_ID_NOT_FOUND = "fieldSchemaId not found";

  /** The name of fieldSchema should be filled */
  public static final String INVALID_OBJECTID = "A given ObjectId is not valid";

  /** The name of fieldSchema should be filled */
  public static final String FIELD_NAME_NULL = "The name of fieldSchema should be filled";

  /** The user has no permissions */
  public static final String FORBIDDEN = "The user has no permissions";

  /** WebLink not found */
  public static final String ID_LINK_NOT_FOUND = "WebLink not found";

  /** Not enough permission to perform */
  public static final String NOT_ENOUGH_PERMISSION = "Not enough permission to perform";

  /** Method locked */
  public static final String METHOD_LOCKED = "Method locked";

  /** Representative type incorrect */
  public static final String REPRESENTATIVE_TYPE_INCORRECT = "Representative type incorrect";

  /** Representative not found */
  public static final String REPRESENTATIVE_NOT_FOUND = "Representative not found";

  /** Representative duplicated */
  public static final String REPRESENTATIVE_DUPLICATED = "Representative duplicated";

  /** ReferenceId is required */
  public static final String REFERENCE_ID_REQUIRED = "ReferenceId is required";

  /** Description is required" */
  public static final String DESCRIPTION_REQUIRED = "Description is required";

  /** RuleName is required */
  public static final String RULE_NAME_REQUIRED = "RuleName is required";

  /** WhenCondition is required */
  public static final String WHEN_CONDITION_REQUIRED = "WhenCondition is required";

  /** ThenCondition is required */
  public static final String THEN_CONDITION_REQUIRED = "ThenCondition is required";

  /** RuleId is required */
  public static final String RULE_ID_REQUIRED = "RuleId is required";

  /** Data Collection not created */
  public static final String DATA_COLLECTION_NOT_CREATED = "Data Collection not created";

  /** Error deleting rules */
  public static final String ERROR_DELETING_RULE = "Error deleting rules";

  /** Permission not created */
  public static final String PERMISSION_NOT_CREATED = "Permission not created";

  /** Error creating rule */
  public static final String ERROR_CREATING_RULE = "Error creating rule";

  /** Error creating rule, the expression rule is not correct */
  public static final String ERROR_CREATING_RULE_NOT_CORRECT =
      "Error creating rule, the expression rule is not correct";

  /** Error ordering rule */
  public static final String ERROR_ORDERING_RULE = "Error ordering rule";

  /** Error updating rule */
  public static final String ERROR_UPDATING_RULE = "Error updating rule";

  /** PK with existing references */
  public static final String PK_REFERENCED = "PK with existing references";

  /** There is an existing PK */
  public static final String PK_ALREADY_EXISTS = "There is an existing PK";

  /** ShortCode is required */
  public static final String SHORT_CODE_REQUIRED = "ShortCode is required";

  /** DatasetSchema not found for datasetSchemaId %s */
  public static final String DATASET_SCHEMA_NOT_FOUND =
      "DatasetSchema not found for datasetSchemaId %s";

  /** DatasetSchemaId not found for datasetId %s */
  public static final String DATASET_SCHEMA_ID_NOT_FOUND =
      "DatasetSchemaId not found for datasetId %s";

  /** The table is marked as read only */
  public static final String TABLE_READ_ONLY = "The table is marked as read only";

  /** Rule not found for datasetSchemaId %s and ruleId %s */
  public static final String RULE_NOT_FOUND = "Rule not found for datasetSchemaId %s and ruleId %s";

  /**
   * Instantiates a new EEA error message.
   */
  private EEAErrorMessage() {
    throw new IllegalStateException("Utility class");
  }

}
