package org.eea.utils;

/** The Class LiteralConstants. */
public final class LiteralConstants {

  /** The Constant DATA_REPORTING_TOPIC: {@value}. */
  public static final String DATA_REPORTING_TOPIC = "DATA_REPORTING_TOPIC";

  /** The Constant COMMAND_TOPIC: {@value}. */
  public static final String COMMAND_TOPIC = "COMMAND_TOPIC";

  /** The Constant BROADCAST_TOPIC: {@value}. */
  public static final String BROADCAST_TOPIC = "BROADCAST_TOPIC";

  /** The Constant DATASET_PREFIX: {@value}. */
  public static final String DATASET_PREFIX = "dataset_";

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

  /**
   * Instantiates a new literal constants.
   */
  private LiteralConstants() {
    throw new IllegalStateException("Utility class");
  }
}
