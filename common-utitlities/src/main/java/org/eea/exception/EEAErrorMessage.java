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
  public static final String EXECUTION_ERROR =
      "An error was produced during the process execution.";

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

  /** The Constant DOCUMENT_NOT_PUBLIC: {@value}. */
  public static final String DOCUMENT_NOT_PUBLIC = "Document is not public";

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

  /** The Constant MESSAGE_INCORRECT_ID: {@value}. */
  public static final String MESSAGE_INCORRECT_ID = "Message Id incorrect";

  /** The Constant IS_RESTRICT_FROM_PUBLIC: {@value}. */
  public static final String IS_RESTRICT_FROM_PUBLIC =
      "The representative is restricted from public";

  /** The Constant ERROR_VALIDATING_LEAD_REPORTERS: {@value}. */
  public static final String ERROR_VALIDATING_LEAD_REPORTERS =
      "There was an error validating the Lead Reporters.";

  /** The Constant HISTORIC_QC_NOT_FOUND: {@value}. */
  public static final String HISTORIC_QC_NOT_FOUND = "There aren't any historic information.";

  /** The Constant CREATING_A_MESSAGE_IN_A_DATAFLOW: {@value}. */
  public static final String CREATING_A_MESSAGE_IN_A_DATAFLOW =
      "There was an error creating a message in the Dataflow.";

  /** The Constant UPDATING_A_MESSAGE_IN_A_DATAFLOW: {@value}. */
  public static final String UPDATING_A_MESSAGE_IN_A_DATAFLOW =
      "There was an error updating a message in the Dataflow.";

  /** The Constant DELETING_A_MESSAGE_IN_A_DATAFLOW: {@value}. */
  public static final String DELETING_A_MESSAGE_IN_A_DATAFLOW =
      "There was an error deleting a message in the Dataflow.";

  /** The Constant RETRIEVING_A_MESSAGE_IN_A_DATAFLOW: {@value}. */
  public static final String RETRIEVING_A_MESSAGE_FROM_A_DATAFLOW =
      "There was an error retrieving messages from a Dataflow.";

  /** The Constant DOWNLOADING_ATTACHMENT_IN_A_DATAFLOW: {@value}. */
  public static final String DOWNLOADING_ATTACHMENT_IN_A_DATAFLOW =
      "There was an error downloading an attachment in a Dataflow.";

  /** The Constant UPDATING_ATTACHMENT_IN_A_DATAFLOW: {@value}. */
  public static final String UPDATING_ATTACHMENT_IN_A_DATAFLOW =
      "There was an error updating an attachment in a Dataflow.";

  /** The Constant DELETING_ATTACHMENT_IN_A_DATAFLOW: {@value}. */
  public static final String DELETING_ATTACHMENT_IN_A_DATAFLOW =
      "There was an error deleting an attachment in a Dataflow.";

  /** The Constant CONVERTING_FROM_STRING_TO_LONG: {@value}. */
  public static final String CONVERTING_FROM_STRING_TO_LONG =
      "There was an error converting the value from String to Long";

  /** The Constant CREATING_NOTIFICATION: {@value}. */
  public static final String CREATING_NOTIFICATION =
      "An unknown error happenned sending the notification.";

  /** The Constant CREATING_SYSTEM_NOTIFICATION: {@value}. */
  public static final String CREATING_SYSTEM_NOTIFICATION =
      "An unknown error happenned while creating the system notification.";

  /** The Constant DELETING_SYSTEM_NOTIFICATION: {@value}. */
  public static final String DELETING_SYSTEM_NOTIFICATION =
      "An unknown error happenned while deleting the system notification.";

  /** The Constant UPDATING_SYSTEM_NOTIFICATION: {@value}. */
  public static final String UPDATING_SYSTEM_NOTIFICATION =
      "An unknown error happenned while updating the system notification.";

  /** The Constant DELETING_REQUESTER: {@value}. */
  public static final String DELETING_REQUESTER =
      "An unknown error happenned while deleting the requester.";

  /** The Constant DELETING_REPORTER: {@value}. */
  public static final String DELETING_REPORTER =
      "An unknown error happenned while deleting the reporter.";

  /** The Constant CREATING_ASSOCIATED_PERMISSIONS: {@value}. */
  public static final String CREATING_ASSOCIATED_PERMISSIONS =
      "An unknown error happenned while creating associated permissions.";

  /** The Constant RETRIEVING_WEBLINK: {@value}. */
  public static final String RETRIEVING_WEBLINK =
      "An unknown error happenned while retrieving the weblink.";

  /** The Constant CREATING_WEBLINK: {@value}. */
  public static final String CREATING_WEBLINK =
      "An unknown error happenned while creating the weblink.";

  /** The Constant DELETING_WEBLINK: {@value}. */
  public static final String DELETING_WEBLINK =
      "An unknown error happenned while deleting the weblink.";

  /** The Constant DELETING_WEBLINK: {@value}. */
  public static final String UPDATING_WEBLINK =
      "An unknown error happenned while updating the weblink.";

  /** The Constant RETRIEVING_INTEGRATIONS: {@value}. */
  public static final String RETRIEVING_INTEGRATIONS =
      "An unknown error happenned while retrieving integrations.";

  /** The Constant CREATING_INTEGRATION: {@value}. */
  public static final String CREATING_INTEGRATION =
      "An unknown error happenned while creating the integration.";

  /** The Constant DELETING_INTEGRATION: {@value}. */
  public static final String DELETING_INTEGRATION =
      "An unknown error happenned while deleting the integration.";

  /** The Constant UPDATING_INTEGRATION: {@value}. */
  public static final String UPDATING_INTEGRATION =
      "An unknown error happenned while updating the integration.";

  /** The Constant COPYING_INTEGRATIONS: {@value}. */
  public static final String COPYING_INTEGRATIONS =
      "An unknown error happenned while copying integrations.";

  /** The Constant EXECUTING_INTEGRATIONS: {@value}. */
  public static final String EXECUTING_INTEGRATIONS =
      "An unknown error happenned while executing integrations.";

  /** The Constant EXPORTING_EU_DATASET: {@value}. */
  public static final String EXPORTING_EU_DATASET =
      "An unknown error happenned while exporting an EU Dataset.";

  /** The Constant CREATING_REPRESENTATIVE: {@value}. */
  public static final String CREATING_REPRESENTATIVE =
      "An unknown error happenned while creating a representative.";

  /** The Constant EXPORT_LEAD_REPORTERS: {@value}. */
  public static final String EXPORT_LEAD_REPORTERS =
      "An unknown error happenned while exporting the lead reporters.";

  /** The Constant IMPORT_LEAD_REPORTERS: {@value}. */
  public static final String IMPORT_LEAD_REPORTERS =
      "An unknown error happenned while importing the lead reporters.";

  /** The Constant CREATE_LEAD_REPORTER: {@value}. */
  public static final String CREATE_LEAD_REPORTER =
      "An unknown error happenned while creating a lead reporter.";

  /** The Constant OBTAINING_TABLE_DATA: {@value}. */
  public static final String OBTAINING_TABLE_DATA =
      "An unknown error happenned while obtaining the table data.";

  /** The Constant INSERTING_TABLE_DATA: {@value}. */
  public static final String INSERTING_TABLE_DATA =
      "An unknown error happenned while inserting data in the table.";

  /** The Constant UPDATING_TABLE_DATA: {@value}. */
  public static final String UPDATING_TABLE_DATA =
      "An unknown error happenned while updating the table data.";

  /** The Constant DELETING_TABLE_DATA: {@value}. */
  public static final String DELETING_TABLE_DATA =
      "An unknown error happenned while deleting the table data.";

  /** The Constant UPDATING_DATASET: {@value}. */
  public static final String UPDATING_DATASET =
      "An unknown error happenned while updating the dataset.";

  /** The Constant IMPORTING_FILE_DATASET: {@value}. */
  public static final String IMPORTING_FILE_DATASET =
      "An unknown error happenned while importing a file into the dataset.";

  /** The Constant EXPORTING_FILE_INTEGRATION: {@value}. */
  public static final String EXPORTING_FILE_INTEGRATION =
      "An unknown error happenned while exporting a file through integration.";

  /** The Constant INSERTING_TABLE_DATA: {@value}. */
  public static final String INSERTING_DATASCHEMA =
      "An unknown error happenned while inserting a dataschema.";

  /** The Constant UPDATING_FIELD: {@value}. */
  public static final String UPDATING_FIELD = "An unknown error happenned while updating a field.";

  /** The Constant RETRIEVING_REFERENCED_FIELD: {@value}. */
  public static final String RETRIEVING_REFERENCED_FIELD =
      "An unknown error happenned while retrieving data from a referenced field.";

  /** The Constant IMPORTING_DATA_DATASET: {@value}. */
  public static final String IMPORTING_DATA_DATASET =
      "An unknown error happenned while importing data into a dataset.";

  /** The Constant UPDATING_DATASET_STATUS: {@value}. */
  public static final String UPDATING_DATASET_STATUS =
      "An unknown error happenned while updating the dataset status.";

  /** The Constant RETRIEVING_DATASET_SCHEMA: {@value}. */
  public static final String RETRIEVING_DATASET_SCHEMA =
      "An unknown error happenned while retrieving the dataset schema.";

  /** The Constant DELETING_TABLE_SCHEMA: {@value}. */
  public static final String DELETING_TABLE_SCHEMA =
      "An unknown error happenned while deleting the table schema.";

  /** The Constant RETRIEVING_TABLE_SCHEMAS: {@value}. */
  public static final String RETRIEVING_TABLE_SCHEMAS =
      "An unknown error happenned while retrieving the table schemas.";

  /** The Constant DELETING_DESIGN_DATASET: {@value}. */
  public static final String DELETING_DESIGN_DATASET =
      "An unknown error happenned while deleting a dataset.";

  /** The Constant CREATING_FIELD_SCHEMA: {@value}. */
  public static final String CREATING_FIELD_SCHEMA =
      "An unknown error happenned while creating a field schema.";

  /** The Constant UPDATING_FIELD_SCHEMA: {@value}. */
  public static final String UPDATING_FIELD_SCHEMA =
      "An unknown error happenned while updating a field schema.";

  /** The Constant DELETING_FIELD_SCHEMA: {@value}. */
  public static final String DELETING_FIELD_SCHEMA =
      "An unknown error happenned while deleting a field schema.";

  /** The Constant COPYING_DESIGN_DATAFLOW: {@value}. */
  public static final String COPYING_DESIGN_DATAFLOW =
      "An unknown error happenned while copying designs from another dataflow.";

  /** The Constant RETRIEVING_DATASET_SIMPLE_SCHEMA: {@value}. */
  public static final String RETRIEVING_DATASET_SIMPLE_SCHEMA =
      "An unknown error happenned while retrieving dataset simple schema.";

  /** The Constant EXPORTING_SCHEMAS: {@value}. */
  public static final String EXPORTING_SCHEMAS =
      "An unknown error happenned while exporting schemas.";

  /** The Constant EXPORTING_FIELD_SCHEMAS: {@value}. */
  public static final String EXPORTING_FIELD_SCHEMAS =
      "An unknown error happenned while exporting field schemas.";

  /** The Constant IMPORTING_SCHEMAS: {@value}. */
  public static final String IMPORTING_SCHEMAS =
      "An unknown error happenned while importing schemas.";

  /** The Constant EXPORTING_DATASET_DEFINITION: {@value}. */
  public static final String EXPORTING_DATASET_DEFINITION =
      "An unknown error happenned while exporting dataset definition.";

  /** The Constant DELETING_SNAPSHOT: {@value}. */
  public static final String DELETING_SNAPSHOT =
      "An unknown error happenned while deleting a snapshot.";

  /** The Constant DELETING_SCHEMA_SNAPSHOT: {@value}. */
  public static final String DELETING_SCHEMA_SNAPSHOT =
      "An unknown error happenned while deleting a schema snapshot.";

  /** The Constant RETRIEVING_SINGLE_PAM_LIST: {@value}. */
  public static final String RETRIEVING_SINGLE_PAM_LIST =
      "An unknown error happenned while retrieving single PAM list.";

  /** The Constant UPDATING_REFERENCE_DATASET: {@value}. */
  public static final String UPDATING_REFERENCE_DATASET =
      "An unknown error happenned while updating a reference dataset.";

  /** The Constant OBTAINING_WEBFORM_CONFIG: {@value}. */
  public static final String OBTAINING_WEBFORM_CONFIG =
      "An unknown error happenned while obtaining the webform config.";

  /** The Constant RETRIEVING_DOCUMENT: {@value}. */
  public static final String RETRIEVING_DOCUMENT =
      "An unknown error happenned while retrieving the document.";

  /** The Constant DELETING_DOCUMENT: {@value}. */
  public static final String DELETING_DOCUMENT =
      "An unknown error happenned while deleting the document.";

  /** The Constant UPDATING_DOCUMENT: {@value}. */
  public static final String UPDATING_DOCUMENT =
      "An unknown error happenned while updating the document.";

  /** The Constant UPDATING_SCHEMA_SNAPSHOT_DOCUMENT: {@value}. */
  public static final String UPDATING_SCHEMA_SNAPSHOT_DOCUMENT =
      "An unknown error happenned while updating the schema snapshot document.";

  /** The Constant RETRIEVING_SNAPSHOT_DOCUMENT: {@value}. */
  public static final String RETRIEVING_SNAPSHOT_DOCUMENT =
      "An unknown error happenned while retrieving the snapshot document.";

  /** The Constant DELETING_SNAPSHOT_DOCUMENT: {@value}. */
  public static final String DELETING_SNAPSHOT_DOCUMENT =
      "An unknown error happenned while deleting the snapshot document.";

  /** The Constant UPDATING_COLLABORATION_DOCUMENT: {@value}. */
  public static final String UPDATING_COLLABORATION_DOCUMENT =
      "An unknown error happenned while uploading a collaboration document.";

  /** The Constant DELETING_COLLABORATION_DOCUMENT: {@value}. */
  public static final String DELETING_COLLABORATION_DOCUMENT =
      "An unknown error happenned while deleting the collaboration document.";

  /** The Constant RETRIEVING_COLLABORATION_DOCUMENT: {@value}. */
  public static final String RETRIEVING_COLLABORATION_DOCUMENT =
      "An unknown error happenned while retrieving the collaboration document.";

  /** The Constant CREATING_EMPTY_DATASET: {@value}. */
  public static final String CREATING_EMPTY_DATASET =
      "An unknown error happenned while creating a dataset.";

  /** The Constant CREATING_SNAPSHOT: {@value}. */
  public static final String CREATING_SNAPSHOT =
      "An unknown error happenned while creating a snapshot.";

  /** The Constant RESTORING_SNAPSHOT: {@value}. */
  public static final String RESTORING_SNAPSHOT =
      "An unknown error happenned while restoring a snapshot.";

  /** The Constant CREATING_USERS_THROUGH_FILE: {@value}. */
  public static final String CREATING_USERS_THROUGH_FILE =
      "An unknown error happenned while creating users through a file.";

  /** The Constant COPYING_RULE: {@value}. */
  public static final String COPYING_RULE = "An unknown error happenned while copying a rule.";

  /** The Constant IMPORTING_RULE: {@value}. */
  public static final String IMPORTING_RULE = "An unknown error happenned while importing a rule.";

  /** The Constant SQL_COMMAND_NOT_ALLOWED: {@value}. */
  public static final String SQL_COMMAND_NOT_ALLOWED = "SQL Command not allowed in SQL Sentence.";

  /** The Constant RUNNING_RULE: {@value}. */
  public static final String RUNNING_RULE =
      "An unknown error happenned while running the SQL rule.";

  /** The Constant EVALUATING_RULE: {@value}. */
  public static final String EVALUATING_RULE =
      "An unknown error happenned while evaluating the SQL rule.";

  /** The Constant AUDIT_NOT_FOUND: {@value}. */
  public static final String AUDIT_NOT_FOUND = "Audit not found.";

  /**
   * Instantiates a new EEA error message.
   */
  private EEAErrorMessage() {
    throw new IllegalStateException("Utility class");
  }
}
