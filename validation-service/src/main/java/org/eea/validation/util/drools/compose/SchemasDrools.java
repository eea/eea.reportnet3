package org.eea.validation.util.drools.compose;

/**
 * The Enum ConditionsDrools.
 */
public enum SchemasDrools {

  /** The datasetSchema. */
  ID_DATASET_SCHEMA("idDatasetSchema"),
  /** The tableSchema. */
  ID_TABLE_SCHEMA("idTableSchema"),
  /** The recordSchema. */
  ID_RECORD_SCHEMA("idRecordSchema"),
  /** The fieldSchema. */
  ID_FIELD_SCHEMA("idFieldSchema");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new type validation.
   *
   * @param value the value
   */
  SchemasDrools(String value) {
    this.value = value;
  }

  /**
   * Gets the value.
   *
   * @return the value
   */
  public String getValue() {
    return value;
  }
}
