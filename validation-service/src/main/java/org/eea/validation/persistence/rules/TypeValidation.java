package org.eea.validation.persistence.rules;

/**
 * The Enum TypeValidation.
 */
public enum TypeValidation {

  /** The datasetvo. */
  DATASETVO("DataSetVO"),
  /** The fieldvo. */
  FIELDVO("FieldVO"),
  /** The recordvo. */
  RECORDVO("RecordVO"),
  /** The tablevo. */
  TABLEVO("TableVO"),
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
