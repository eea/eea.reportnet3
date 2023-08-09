package org.eea.utils;

/**
 * The Class LiteralConstants.
 */
public final class LiteralConstants {

  /** The Constant DATA_REPORTING_TOPIC: {@value}. */
  public static final String DATA_REPORTING_TOPIC = "DATA_REPORTING_TOPIC";

  /** The Constant COMMAND_TOPIC: {@value}. */
  public static final String COMMAND_TOPIC = "COMMAND_TOPIC";

  /** The Constant BROADCAST_TOPIC: {@value}. */
  public static final String BROADCAST_TOPIC = "BROADCAST_TOPIC";

  /** The Constant DATASET_PREFIX: {@value}. */
  public static final String DATASET_PREFIX = "dataset_";

  /** The Constant PK: {@value}. */
  public static final String PK = "pk";

  /** The Constant DATASET_FORMAT_NAME: {@value}. */
  public static final String DATASET_FORMAT_NAME = "dataset_%s";

  /** The Constant SNAPSHOT_EXTENSION: {@value}. */
  public static final String SNAPSHOT_EXTENSION = ".snap";

  /** The Constant BEARER_TOKEN: {@value}. */
  public static final String BEARER_TOKEN = "Bearer ";

  /** The Constant AUTHORIZATION_HEADER: {@value}. */
  public static final String AUTHORIZATION_HEADER = "Authorization";

  /** The Constant ID_DATASET_SCHEMA: {@value}. */
  public static final String ID_DATASET_SCHEMA = "idDatasetSchema";

  /** The Constant DATASET_NAME: {@value}. */
  public static final String DATASET_NAME = "datasetName";

  /** The Constant COUNTRY_CODE: {@value}. */
  public static final String COUNTRY_CODE = "countryCode";

  /** The Constant DATA_CALL_YEAR: {@value}. */
  public static final String DATA_CALL_YEAR = "dataCallYear";

  /** The Constant DATASET_ID: {@value}. */
  public static final String DATASET_ID = "dataset_id";

  /** The Constant DATASET_ID: {@value}. */
  public static final String DATASETID = "datasetId";

  /** The Constant DATASET_ID: {@value}. */
  public static final String SIGNATURE = "signature";

  /** The Constant PK_HAS_MULTIPLE_VALUES: {@value}. */
  public static final String PK_HAS_MULTIPLE_VALUES = "pkHasMultipleValues";

  /** The Constant RULES: {@value}. */
  public static final String RULES = "rules";

  /** The Constant TABLE_SCHEMAS: {@value}. */
  public static final String TABLE_SCHEMAS = "tableSchemas";

  /** The Constant DATASET_SCHEMA: {@value}. */
  public static final String DATASET_SCHEMA = "DataSetSchema";

  /** The Constant TYPE_DATA: {@value}. */
  public static final String TYPE_DATA = "typeData";

  /** The Constant REFERENCED_FIELD: {@value}. */
  public static final String REFERENCED_FIELD = "referencedField";

  /** The Constant CODELIST_ITEMS: {@value}. */
  public static final String CODELIST_ITEMS = "codelistItems";

  /** The Constant NOT_FOUND: {@value}. */
  public static final String NOT_FOUND = "not_found";

  /** The Constant GRANT_TYPE: {@value}. */
  public static final String GRANT_TYPE = "grant_type";

  /** The Constant CLIENT_SECRET: {@value}. */
  public static final String CLIENT_SECRET = "client_secret";

  /** The Constant REFRESH_TOKEN: {@value}. */
  public static final String REFRESH_TOKEN = "refresh_token";

  /** The Constant CLIENT_ID: {@value}. */
  public static final String CLIENT_ID = "client_id";

  /** The Constant SNAPSHOT_FILE_DATASET_SUFFIX: {@value}. */
  public static final String SNAPSHOT_FILE_DATASET_SUFFIX = "_table_DatasetValue.snap";

  /** The Constant SNAPSHOT_FILE_TABLE_SUFFIX: {@value}. */
  public static final String SNAPSHOT_FILE_TABLE_SUFFIX = "_table_TableValue.snap";

  /** The Constant SNAPSHOT_FILE_RECORD_SUFFIX: {@value}. */
  public static final String SNAPSHOT_FILE_RECORD_SUFFIX = "_table_RecordValue.snap";

  /** The Constant SNAPSHOT_FILE_FIELD_SUFFIX: {@value}. */
  public static final String SNAPSHOT_FILE_FIELD_SUFFIX = "_table_FieldValue.snap";

  /** The Constant SNAPSHOT_FILE_ATTACHMENT_SUFFIX: {@value}. */
  public static final String SNAPSHOT_FILE_ATTACHMENT_SUFFIX = "_table_AttachmentValue.snap";

  /** The Constant RULE_TABLE_MANDATORY: {@value}. */
  public static final String RULE_TABLE_MANDATORY = "Mandatory table records check";

  /** The Constant READ_ONLY: {@value}. */
  public static final String READ_ONLY = "readOnly";

  /** The Constant INTEGRATION_ID: {@value}. */
  public static final String INTEGRATION_ID = "integrationId";

  /** The Constant OPERATION: {@value}. */
  public static final String OPERATION = "operation";

  /** The Constant USER: {@value}. */
  public static final String USER = "user";

  /** The Constant ID: {@value}. */
  public static final String ID = "_id";

  /** The Constant FIELD_SCHEMAS: {@value}. */
  public static final String FIELD_SCHEMAS = "fieldSchemas";

  /** The Constant DATAFLOWID: {@value}. */
  public static final String DATAFLOWID = "dataflowId";

  /** The Constant DATAPROVIDERID: {@value}. */
  public static final String DATAPROVIDERID = "dataProviderId";

  /** The Constant FIELDSCHEMAID: {@value}. */
  public static final String FIELDSCHEMAID = "fieldSchemaId";

  /** The Constant TABLESCHEMAID: {@value}. */
  public static final String TABLESCHEMAID = "tableSchemaId";

  /** The Constant RELEASED: {@value}. */
  public static final String RELEASED = "released";

  /** The Constant DATAFLOWIDORIGIN: {@value}. */
  public static final String DATAFLOWIDORIGIN = "dataflowIdOrigin";

  /** The Constant DATAFLOWIDDESTINATION: {@value}. */
  public static final String DATAFLOWIDDESTINATION = "dataflowIdDestination";

  /** The Constant RELEASESUBJECT. */
  public static final String RELEASESUBJECT = "%s released %s";

  /** The Constant RELEASEMESSAGE. */
  public static final String RELEASEMESSAGE =
      "This automatic notification informs that %s has successfully released in %s on %s (CET).";

  /** The Constant GEOMETRYERROR. : {@value} */
  public static final String GEOMETRYERROR =
      "The value does not follow the expected syntax for a valid ";

  /** The Constant POLYGONERROR.: {@value} */
  public static final String POLYGONERROR =
      "Polygon geometry is not valid - polygon ring not closed";

  /** The Constant REQUESTER. : {@value} */
  public static final String REQUESTER = "REQUESTER";

  /** The Constant REPORTER: {@value}. */
  public static final String REPORTER = "REPORTER";

  /** The Constant IMPORT_LOCKED. */
  public static final String IMPORT_LOCKED = "Import is locked";

  /** The Constant NO_IMPORT_IN_PROGRESS. */
  public static final String NO_IMPORT_IN_PROGRESS = "There is no import process in progress";

  /** The Constant S3_NAME_PATTERN_LENGTH: {@value}. */
  public static final int S3_NAME_PATTERN_LENGTH = 7;

  /** The Constant S3_DEFAULT_BUCKET: {@value}. */
  public static final String S3_DEFAULT_BUCKET = "\"rn3-dataset\".\"rn3-dataset\"";

  /** The Constant S3_DEFAULT_BUCKET_PATH: {@value}. */
  public static final String S3_DEFAULT_BUCKET_PATH = "rn3-dataset/rn3-dataset";

  /** The Constant S3_LEFT_PAD: {@value}. */
  public static final String S3_LEFT_PAD= "0";

  /** The Constant S3_DATAFLOW_PATTERN: {@value}. */
  public static final String S3_DATAFLOW_PATTERN = "df-%s";

  /** The Constant S3_DATASET_PATTERN: {@value}. */
  public static final String S3_DATASET_PATTERN = "ds-%s";

  /** The Constant S3_PROVIDER_PATTERN: {@value}. */
  public static final String S3_DATA_PROVIDER_PATTERN = "dp-%s";

  /** The Constant S3_DATA_COLLECTION_PATTERN: {@value}. */
  public static final String S3_DATA_COLLECTION_PATTERN = "dc-%s";

  /** The Constant S3_COLLECTIONS: {@value}. */
  public static final String S3_COLLECTIONS = "collections";

  /** The Constant S3_IMPORT_PATH: {@value}. */
  public static final String S3_IMPORT_PATH = "/%s/%s/%s/current/import";

  /** The Constant S3_PROVIDER_IMPORT_PATH: {@value}. */
  public static final String S3_PROVIDER_IMPORT_PATH = "/%s/%s/%s/current/provider_import";

  /** The Constant S3_IMPORT_TABLE_NAME_FOLDER_PATH: {@value}. */
  public static final String S3_IMPORT_TABLE_NAME_FOLDER_PATH = "/%s/%s/%s/current/import/%s";

  /** The Constant S3_IMPORT_FILE_PATH: {@value}. */
  public static final String S3_IMPORT_FILE_PATH = "%s/%s/%s/current/import/%s/%s";

  /** The Constant S3_PROVIDER_IMPORT_FILE_PATH: {@value}. */
  public static final String S3_PROVIDER_IMPORT_FILE_PATH = "%s/%s/%s/current/provider_import/%s";

  /** The Constant S3_IMPORT_QUERY_PATH: {@value}. */
  public static final String S3_IMPORT_QUERY_PATH = S3_DEFAULT_BUCKET + ".\"%s\".\"%s\".\"%s\".\"current\".\"import\".\"%s\"";

  /** The Constant S3_IMPORT_CSV_FILE_QUERY_PATH: {@value}. */
  public static final String S3_IMPORT_CSV_FILE_QUERY_PATH = S3_DEFAULT_BUCKET + ".\"%s\".\"%s\".\"%s\".\"current\".\"import\".\"%s\".\"%s\"";

  /** The Constant S3_VALIDATION_PATH: {@value}. */
  public static final String S3_VALIDATION_PATH = "%s/%s/%s/current/validation/%s/%s";

  /** The constant S3_VALIDATION_RULE_PATH: {@value}. */
  public static final String S3_VALIDATION_RULE_PATH = "%s/%s/%s/current/validation/%s";

  /** The Constant S3_VALIDATION_PATH: {@value}. */
  public static final String S3_VALIDATION_QUERY_PATH = S3_DEFAULT_BUCKET + ".\"%s\".\"%s\".\"%s\".\"current\".\"validation\".\"%s\".\"%s\"";

  /** The Constant S3_TABLE_NAME_VALIDATE_PATH: {@value}. */
  public static final String S3_TABLE_NAME_VALIDATE_PATH = "%s/%s/%s/current/%s_validate/%s";

  /** The Constant S3_TABLE_NAME_VALIDATE_QUERY_PATH: {@value}. */
  public static final String S3_TABLE_NAME_VALIDATE_QUERY_PATH = S3_DEFAULT_BUCKET + ".\"%s\".\"%s\".\"%s\".\"current\".\"%s_validate\".\"%s\"";

  /** The Constant S3_TABLE_NAME_PATH: {@value}. */
  public static final String S3_TABLE_NAME_PATH = "/%s/%s/%s/current/%s/%s";

  /** The Constant S3_TABLE_NAME_FOLDER_PATH: {@value}. */
  public static final String S3_TABLE_NAME_FOLDER_PATH = "/%s/%s/%s/current/%s";

  /** The Constant S3_CURRENT_PATH: {@value}. */
  public static final String S3_CURRENT_PATH = "/%s/%s/%s/current";

  /** The Constant S3_TABLE_NAME_QUERY_PATH: {@value}. */
  public static final String S3_TABLE_NAME_QUERY_PATH = S3_DEFAULT_BUCKET + ".\"%s\".\"%s\".\"%s\".\"current\".\"%s\".\"%s\"";

  /** The Constant S3_VALIDATION_DC_PATH: {@value}. */
  public static final String S3_VALIDATION_DC_PATH = "%s/%s/%s/current/validation/%s/%s/%s";

  /** The Constant S3_VALIDATION_TABLE_PATH: {@value}. */
  public static final String S3_VALIDATION_TABLE_PATH = "/%s/%s/%s/current/validation";

  /** The Constant S3_VALIDATION_DC_QUERY_PATH: {@value}. */
  public static final String S3_VALIDATION_DC_QUERY_PATH = S3_DEFAULT_BUCKET + ".\"%s\".\"%s\".\"%s\".\"current\".\"validation\".\"%s\".\"%s\".\"%s\"";

  /** The Constant S3_TABLE_NAME_VALIDATE_DC_PATH: {@value}. */
  public static final String S3_TABLE_NAME_VALIDATE_DC_PATH = "%s/%s/%s/current/%s_validate/%s/%s";

  /** The Constant S3_TABLE_NAME_VALIDATE_DC_QUERY_PATH: {@value}. */
  public static final String S3_TABLE_NAME_VALIDATE_DC_QUERY_PATH = S3_DEFAULT_BUCKET + ".\"%s\".\"%s\".\"%s\".\"current\".\"%s_validate\".\"%s\".\"%s\"";

  /** The Constant S3_TABLE_NAME_DC_PATH: {@value}. */
  public static final String S3_TABLE_NAME_DC_PATH = "%s/%s/%s/current/%s/%s/%s";

  /** The Constant S3_TABLE_NAME_DC_FOLDER_PATH: {@value}. */
  public static final String S3_TABLE_NAME_DC_FOLDER_PATH = "%s/%s/%s/current/%s/%s";

  /** The Constant S3_TABLE_NAME_ROOT_DC_FOLDER_PATH: {@value}. */
  public static final String S3_TABLE_NAME_ROOT_DC_FOLDER_PATH = "%s/%s/%s/current";

  /** The Constant S3_TABLE_NAME_DC_QUERY_PATH: {@value}. */
  public static final String S3_TABLE_NAME_DC_QUERY_PATH = S3_DEFAULT_BUCKET + ".\"%s\".\"%s\".\"%s\".\"current\".\"%s\".\"%s\".\"%s\"";

  public static final String S3_TABLE_AS_FOLDER_QUERY_PATH = S3_DEFAULT_BUCKET + ".\"%s\".\"%s\".\"%s\".\"current\".\"%s\"";

  /** The Constant S3_DATAFLOW_REFERENCE_PATH: {@value}. */
  public static final String S3_DATAFLOW_REFERENCE_PATH = "%s/reference/%s";

  /** The Constant S3_DATAFLOW_REFERENCE_QUERY_PATH: {@value}. */
  public static final String S3_DATAFLOW_REFERENCE_QUERY_PATH = S3_DEFAULT_BUCKET + ".\"%s\".\"reference\".\"%s\"";

  /** The Constant S3_EXPORT_PATH: {@value}. */
  public static final String S3_EXPORT_PATH = "%s/%s/%s/export/%s";

  /** The Constant S3_EXPORT_FOLDER_PATH: {@value}. */
  public static final String S3_EXPORT_FOLDER_PATH = "%s/%s/%s/export";

  /** The Constant S3_EXPORT_QUERY_PATH: {@value}. */
  public static final String S3_EXPORT_QUERY_PATH = S3_DEFAULT_BUCKET + ".\"%s\".\"%s\".\"%s\".\"export\".\"%s\"";

  /** The Constant S3_VALIDATION: {@value}. */
  public static final String S3_VALIDATION = "validation";

  /** The Constant S3_BUCKET_NAME: {@value}. */
  public static final String S3_BUCKET_NAME = "rn3-dataset";

  /** The Constant PARQUET_RECORD_ID_COLUMN_HEADER: {@value}. */
  public static final String PARQUET_RECORD_ID_COLUMN_HEADER = "record_id";

  /** The Constant PARQUET_PROVIDER_CODE_COLUMN_HEADER: {@value}. */
  public static final String PARQUET_PROVIDER_CODE_COLUMN_HEADER = "data_provider_code";

  /** The Constant JSON_TYPE: {@value}. */
  public static final String JSON_TYPE = ".json";

  /** The Constant CSV_TYPE: {@value}. */
  public static final String CSV_TYPE = ".csv";

  /** The Constant XML_TYPE: {@value}. */
  public static final String XML_TYPE = ".xml";

  /** The Constant XLSX_TYPE: {@value}. */
  public static final String XLSX_TYPE = ".xlsx";

  /** The Constant PARQUET_TYPE: {@value}. */
  public static final String PARQUET_TYPE = ".parquet";

  /** The Constant ZIP_TYPE: {@value}. */
  public static final String ZIP_TYPE = ".zip";


  /**
   * Instantiates a new literal constants.
   */
  private LiteralConstants() {
    throw new IllegalStateException("Utility class");
  }

}
