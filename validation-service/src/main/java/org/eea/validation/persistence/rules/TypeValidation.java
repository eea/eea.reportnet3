package org.eea.validation.persistence.rules;

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
  TABLE("TableValue"),
  /** The dataflowrule. */
  DATAFLOWRULE("DataFlowRule");

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

  public String getValue() {
    return value;
  }

}
