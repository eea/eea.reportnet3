package org.eea.validation.util.drools.compose;

/**
 * The Enum TypeValidation.
 */
public enum TypeValidation {

  /** The datasetvo. */
  DATASET("DatasetValue"),
  /** The fieldvo. */
  FIELD("FieldValue"),
  /** The recordvo. */
  RECORD("RecordValue"),
  /** The tablevo. */
  TABLE("TableValue");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new type validation.
   *
   * @param value the value
   */
  TypeValidation(String value) {
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
