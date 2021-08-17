package org.eea.exception;

/**
 * The Class EEAErrorMessage.
 */
public final class EEAErrorMessage {

  /** The Constant FILE_FORMAT: {@value}. */
  public static final String FILE_FORMAT = "Invalid file format";

  /** The Constant INVALID_DATE: {@value}. */
  public static final String INVALID_DATE = "Not a valid date";

  /** The Constant NOT_DESIGN_DATAFLOW: {@value}. */
  public static final String NOT_DESIGN_DATAFLOW = "Not a DESIGN dataflow";

  /** The Constant NOT_DRAFT_DATAFLOW: {@value}. */
  public static final String NOT_DRAFT_DATAFLOW = "Not a DRAFT dataflow";

  /** The Constant FILE_EXTENSION: {@value}. */
  public static final String FILE_EXTENSION = "File without extension";

  /** The Constant FILE_NAME: {@value}. */
  public static final String FILE_NAME = "Error getting the file name";

  /** The Constant DATASET_NOTFOUND: {@value}. */
  public static final String DATASET_NOTFOUND = "Dataset not found";

  /** The Constant DATASET_UNKNOW_TYPE: {@value}. */
  public static final String DATASET_UNKNOW_TYPE = "Unknown Dataset type";

  /** The Constant DATASET_INCORRECT_ID: {@value}. */
  public static final String DATASET_INCORRECT_ID = "Dataset Id incorrect";

  /** The Constant DATASET_NAME_DUPLICATED: {@value}. */
  public static final String DATASET_NAME_DUPLICATED = "Dataset name duplicated in this dataflow";

  /** The Constant IDTABLESCHEMA_INCORRECT: {@value}. */
  public static final String IDTABLESCHEMA_INCORRECT = "TableSchemaId incorrect";

  /** The Constant IDUNQUECONSTRAINT_INCORRECT: {@value}. */
  public static final String IDUNQUECONSTRAINT_INCORRECT = "Unique Constraint Id incorrect";

  /** The Constant IDDATASETSCHEMA_INCORRECT: {@value}. */
  public static final String IDDATASETSCHEMA_INCORRECT = "DatasetSchemaId incorrect";

  /** The Constant RULEID_INCORRECT: {@value}. */
  public static final String RULEID_INCORRECT = "ruleId incorrect";

  /** The Constant REFERENCEID_INCORRECT: {@value}. */
  public static final String REFERENCEID_INCORRECT = "referenceId incorrect";

  /** The Constant EXECUTION_ERROR: {@value}. */
  public static final String EXECUTION_ERROR = "Runtime error";

  /** The Constant VALIDATION_SESSION_ERROR: {@value}. */
  public static final String VALIDATION_SESSION_ERROR = "Validation session error";

  /** The Constant FILE_NOT_FOUND: {@value}. */
  public static final String FILE_NOT_FOUND = "File not found in the application";

  /** The Constant RECORD_NOTFOUND: {@value}. */
  public static final String RECORD_NOTFOUND = "Record not found";

  /** The Constant RECORD_REQUIRED: {@value}. */
  public static final String RECORD_REQUIRED =
      "Error inserting records. At least one record is required";

  /** The Constant PARTITION_ID_NOTFOUND: {@value}. */
  public static final String PARTITION_ID_NOTFOUND = "Partition not found";

  /** The Constant DATAFLOW_INCORRECT_ID: {@value}. */
  public static final String DATAFLOW_INCORRECT_ID = "Dataflow Id incorrect";

  /** The Constant DATAFLOW_EXISTS_NAME: {@value}. */
  public static final String DATAFLOW_EXISTS_NAME = "Dataflow name already exists";

  /** The Constant DATAFLOW_NOTFOUND: {@value}. */
  public static final String DATAFLOW_NOTFOUND = "Dataflow not found";

  /** The Constant DATAFLOW_DESCRIPTION_NAME: {@value}. */
  public static final String DATAFLOW_DESCRIPTION_NAME = "Dataflow Description or Name empty";

  /** The Constant DATAFLOW_OBLIGATION: {@value}. */
  public static final String DATAFLOW_OBLIGATION = "Dataflow Obligation empty";

  /** The Constant USER_REQUEST_NOTFOUND: {@value}. */
  public static final String USER_REQUEST_NOTFOUND = "User request not found";

  /** The Constant USER_NOTFOUND: {@value}. */
  public static final String USER_NOTFOUND = "User %s not found";

  /** The Constant SCHEMA_NOT_FOUND: {@value}. */
  public static final String SCHEMA_NOT_FOUND = "Schema not found";

  /** The Constant DOCUMENT_UPLOAD_ERROR: {@value}. */
  public static final String DOCUMENT_UPLOAD_ERROR = "Document upload error";

  /** The Constant DOCUMENT_DOWNLOAD_ERROR: {@value}. */
  public static final String DOCUMENT_DOWNLOAD_ERROR = "Document download error";

  /** The Constant TABLE_NOT_FOUND: {@value}. */
  public static final String TABLE_NOT_FOUND =
      "Table with schema %s from the datasetId %s not found";

  /** The Constant DOCUMENT_NOT_FOUND: {@value}. */
  public static final String DOCUMENT_NOT_FOUND = "Document not found";

  /** The Constant REPOSITORY_NOT_FOUND: {@value}. */
  public static final String REPOSITORY_NOT_FOUND = "Repository not found";

  /** The Constant URL_FORMAT_INCORRECT: {@value}. */
  public static final String URL_FORMAT_INCORRECT = "Format url is incorrect";

  /** The Constant WEBLINK_ALREADY_EXIST: {@value}. */
  public static final String WEBLINK_ALREADY_EXIST =
      "Weblink already exist in this dataflow in reportnet";


  /** The Constant ID_LINK_INCORRECT: {@value}. */
  public static final String ID_LINK_INCORRECT = "Id link is incorrect";

  /** The Constant DATE_FORMAT_INCORRECT: {@value}. */
  public static final String DATE_FORMAT_INCORRECT = "Format date is incorrect";

  /** The Constant DATE_AFTER_INCORRECT: {@value}. */
  public static final String DATE_AFTER_INCORRECT = "The date has to be later than today's date";

  /** The Constant FIELD_NOT_FOUND: {@value}. */
  public static final String FIELD_NOT_FOUND = "Field not found";

  /** The Constant FIELD_NAME_DUPLICATED: {@value}. */
  public static final String FIELD_NAME_DUPLICATED =
      "Field name %s duplicated in the row %s, in the datasetSchema %s";

  /** The Constant FIELD_SCHEMA_ID_NOT_FOUND: {@value}. */
  public static final String FIELD_SCHEMA_ID_NOT_FOUND = "fieldSchemaId not found";

  /** The Constant INVALID_OBJECTID: {@value}. */
  public static final String INVALID_OBJECTID = "A given ObjectId is not valid";

  /** The Constant FIELD_NAME_NULL: {@value}. */
  public static final String FIELD_NAME_NULL = "The name of fieldSchema should be filled";

  /** The Constant FORBIDDEN: {@value}. */
  public static final String FORBIDDEN = "The user has no permissions";

  /** The Constant UNAUTHORIZED: {@value}. */
  public static final String UNAUTHORIZED = "Method secured. Authentication needed";

  /** The Constant ID_LINK_NOT_FOUND: {@value}. */
  public static final String ID_LINK_NOT_FOUND = "WebLink not found";

  /** The Constant NOT_ENOUGH_PERMISSION: {@value}. */
  public static final String NOT_ENOUGH_PERMISSION = "Not enough permission to perform";

  /** The Constant METHOD_LOCKED: {@value}. */
  public static final String METHOD_LOCKED = "Method locked";

  /** The Constant REPRESENTATIVE_TYPE_INCORRECT: {@value}. */
  public static final String REPRESENTATIVE_TYPE_INCORRECT = "Representative type incorrect";

  /** The Constant REPRESENTATIVE_NOT_FOUND: {@value}. */
  public static final String REPRESENTATIVE_NOT_FOUND = "Representative not found";

  /** The Constant CSV_FILE_ERROR: {@value}. */
  public static final String CSV_FILE_ERROR = "File format invalid, use csv file ";

  /** The Constant DATA_FILE_ERROR: {@value}. */
  public static final String DATA_FILE_ERROR = "File format invalid, use valid format file ";

  /** The Constant REPRESENTATIVE_DUPLICATED: {@value}. */
  public static final String REPRESENTATIVE_DUPLICATED = "Representative duplicated";

  /** The Constant REFERENCE_ID_REQUIRED: {@value}. */
  public static final String REFERENCE_ID_REQUIRED = "ReferenceId is required";

  /** The Constant DESCRIPTION_REQUIRED: {@value}. */
  public static final String DESCRIPTION_REQUIRED = "Description is required";

  /** The Constant RULE_NAME_REQUIRED: {@value}. */
  public static final String RULE_NAME_REQUIRED = "RuleName is required";

  /** The Constant WHEN_CONDITION_REQUIRED: {@value}. */
  public static final String WHEN_CONDITION_REQUIRED = "WhenCondition is required";

  /** The Constant THEN_CONDITION_REQUIRED: {@value}. */
  public static final String THEN_CONDITION_REQUIRED = "ThenCondition is required";

  /** The Constant RULE_ID_REQUIRED: {@value}. */
  public static final String RULE_ID_REQUIRED = "RuleId is required";

  /** The Constant DATA_COLLECTION_NOT_CREATED: {@value}. */
  public static final String DATA_COLLECTION_NOT_CREATED = "Data Collection not created";

  /** The Constant ERROR_DELETING_RULE: {@value}. */
  public static final String ERROR_DELETING_RULE = "Error deleting rules";

  /** The Constant PERMISSION_NOT_CREATED: {@value}. */
  public static final String PERMISSION_NOT_CREATED = "Permission not created";

  /** The Constant PERMISSION_NOT_REMOVED: {@value}. */
  public static final String PERMISSION_NOT_REMOVED = "Permission %s not removed";

  /** The Constant ERROR_CREATING_RULE: {@value}. */
  public static final String ERROR_CREATING_RULE = "Error creating rule";

  /** The Constant ERROR_CREATING_RULE_TABLE: {@value}. */
  public static final String ERROR_CREATING_RULE_TABLE =
      "Error creating rule, IntegrityV0 or sqlSentence should be filled";

  /** The Constant ERROR_CREATING_RULE_FIELD_RECORD: {@value}. */
  public static final String ERROR_CREATING_RULE_FIELD_RECORD =
      "Error creating rule, whenCondition or sqlSentence should be filled";

  /** The Constant ERROR_CREATING_RULE_NOT_CORRECT: {@value}. */
  public static final String ERROR_CREATING_RULE_NOT_CORRECT =
      "Error creating rule, the expression rule is not correct";

  /** The Constant ERROR_ORDERING_RULE: {@value}. */
  public static final String ERROR_ORDERING_RULE = "Error ordering rule";

  /** The Constant ERROR_UPDATING_RULE: {@value}. */
  public static final String ERROR_UPDATING_RULE = "Error updating rule";

  /** The Constant PK_REFERENCED: {@value}. */
  public static final String PK_REFERENCED = "PK with existing references";

  /** The Constant PK_ALREADY_EXISTS: {@value}. */
  public static final String PK_ALREADY_EXISTS = "There is an existing PK";

  /** The Constant SHORT_CODE_REQUIRED: {@value}. */
  public static final String SHORT_CODE_REQUIRED = "ShortCode is required";

  /** The Constant DATASET_SCHEMA_NOT_FOUND: {@value}. */
  public static final String DATASET_SCHEMA_NOT_FOUND =
      "DatasetSchema not found for datasetSchemaId %s";

  /** The Constant DATASET_SCHEMA_ID_NOT_FOUND: {@value}. */
  public static final String DATASET_SCHEMA_ID_NOT_FOUND =
      "DatasetSchemaId not found for datasetId %s";

  /** The Constant DATASET_SCHEMA_INVALID_NAME_ERROR: {@value}. */
  public static final String DATASET_SCHEMA_INVALID_NAME_ERROR =
      "Dataset name invalid, use valid name";

  /** The Constant TABLE_READ_ONLY: {@value}. */
  public static final String TABLE_READ_ONLY = "The table is marked as read only";

  /** The Constant FIELD_READ_ONLY: {@value}. */
  public static final String FIELD_READ_ONLY = "The field is marked as read only";

  /** The Constant RULE_NOT_FOUND: {@value}. */
  public static final String RULE_NOT_FOUND = "Rule not found for datasetSchemaId %s and ruleId %s";

  /** The Constant DATASET_NOT_BELONG_DATAFLOW: {@value}. */
  public static final String DATASET_NOT_BELONG_DATAFLOW =
      "Forbidden: Dataset %d does not belongs to dataflow %d";

  /** The Constant UNREPORTED_DATA: {@value}. */
  public static final String UNREPORTED_DATA = "Unreported data";

  /** The Constant ENTITY_TYPE_REQUIRED: {@value}. */
  public static final String ENTITY_TYPE_REQUIRED = "Type is required";

  /** The Constant UNIQUE_NOT_FOUND: {@value}. */
  public static final String UNIQUE_NOT_FOUND = "Unique Constraint with id %s not found";

  /** The Constant UNREPORTED_FIELDSCHEMAS: {@value}. */
  public static final String UNREPORTED_FIELDSCHEMAS = "Unreported fieldSchemas";

  /** The Constant MISSING_PARAMETERS_INTEGRATION. */
  public static final String MISSING_PARAMETERS_INTEGRATION = "Parameters incorrect";

  /** The Constant INTEGRATION_NOT_FOUND. */
  public static final String INTEGRATION_NOT_FOUND = "Integration not found";

  /** The Constant DATASET_NOT_REPORTABLE: {@value}. */
  public static final String DATASET_NOT_REPORTABLE = "Dataset %d is not Reportable";

  /** The Constant NOT_EMAIL: {@value}. */
  public static final String NOT_EMAIL = "%s is not an email";

  /** The Constant FORBIDDEN_EXPORT_EU_DATASET_INTEGRATION_CREATION: {@value}. */
  public static final String FORBIDDEN_EXPORT_EU_DATASET_INTEGRATION_CREATION =
      "Aditional EXPORT_EU_DATASET integrations cannot be created";

  /** The Constant FORBIDDEN_EXPORT_EU_DATASET_INTEGRATION_DELETION: {@value}. */
  public static final String FORBIDDEN_EXPORT_EU_DATASET_INTEGRATION_DELETION =
      "EXPORT_EU_DATASET integrations cannot be deleted";

  /** The Constant NO_DESIGNS_TO_COPY: {@value}. */
  public static final String NO_DESIGNS_TO_COPY =
      "No designs datasets found to copy in the dataflow %s";

  /** The Constant ERROR COPYING SCHEMAS: {@value}. */
  public static final String ERROR_COPYING_SCHEMAS =
      "Error copying schemas from the dataflow %s to the dataflow %s";

  /** The Constant OPERATION_TYPE_NOT_EDITABLE: {@value}. */
  public static final String OPERATION_TYPE_NOT_EDITABLE = "The operation type is not editable";

  /** The Constant FIXED_NUMBER_OF_RECORDS: {@value}. */
  public static final String FIXED_NUMBER_OF_RECORDS =
      "The table with id schema %s has a fixed number of records";

  /** The Constant ERROR_UPDATING_TABLE_SCHEMA: {@value}. */
  public static final String ERROR_UPDATING_TABLE_SCHEMA =
      "Error updating the table with id schema %s from the dataset id %s";

  /** The Constant ERROR_DELETING_SNAPSHOT: {@value}. */
  public static final String ERROR_DELETING_SNAPSHOT = "Snapshot didn't deleted , It is Automatic";

  /** The Constant MESSAGING_AUTHORIZATION_FAILED: {@value}. */
  public static final String MESSAGING_AUTHORIZATION_FAILED = "Messaging authorization failed";

  /** The Constant MESSAGING_BAD_REQUEST: {@value}. */
  public static final String MESSAGING_BAD_REQUEST = "Missing required data";

  /** The Constant DUPLICATED_NAME_INTEGRATION: {@value}. */
  public static final String DUPLICATED_INTEGRATION_NAME = "Duplicated integration name";

  /** The Constant PK_ID_ALREADY_EXIST: {@value}. */
  public static final String PK_ID_ALREADY_EXIST =
      "The value %s in a pk already exist in this Webform";

  /** The Constant DATAFLOW_NOT_RELEASABLE: {@value}. */
  public static final String DATAFLOW_NOT_RELEASABLE = "The dataflow %s is not releasable";

  /** The Constant USER_REQUEST_NOTFOUND: {@value}. */
  public static final String USER_AND_COUNTRY_EXIST =
      "Email and country already exist in this dataflow";

  /** The Constant NOT_DESIGN_TO_DATACOLLECTION: {@value}. */
  public static final String NOT_DESIGN_TO_DATACOLLECTION =
      "There aren't design datasets to create to datacollection";

  /** The Constant NOT_REFERENCE_TO_PROCESS: {@value}. */
  public static final String NOT_REFERENCE_TO_PROCESS =
      "There aren't reference datasets to process";

  /** The Constant NO_PK_REFERENCE_DATAFLOW: {@value}. */
  public static final String NO_PK_REFERENCE_DATAFLOW =
      "There aren't primary keys in the reference dataflow";

  /** The Constant COMPANY_GROUP_NOTFOUND: {@value}. */
  public static final String COMPANY_GROUP_NOTFOUND = "The company group selected is not found";

  /** The Constant USERFME_NOTFOUND: {@value}. */
  public static final String USERFME_NOTFOUND = "The user for fme access is not found";

  /** The Constant EXISTING_REPRESENTATIVES: {@value}. */
  public static final String EXISTING_REPRESENTATIVES =
      "There are existing representatives selected right now";

  /**
   * Instantiates a new EEA error message.
   */
  private EEAErrorMessage() {
    throw new IllegalStateException("Utility class");
  }
}
