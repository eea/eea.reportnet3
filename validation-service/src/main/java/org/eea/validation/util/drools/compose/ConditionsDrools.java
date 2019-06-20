package org.eea.validation.util.drools.compose;

/**
 * The Enum ConditionsDrools.
 */
public enum ConditionsDrools {

  /** The datasetvo. */
  TYPE_VALIDATION("typevalidation"),
  /** The fieldvo. */
  WHEN_CONDITION("whencondition"),
  /** The recordvo. */
  MESSAGE_FAIL_VALIDATION("messageFailValidation"),
  /** The tablevo. */
  TYPE_FAIL_VALIDATION("typeFailValidation"),
  /** The dataflowrule. */
  RULE_ID("ruleid"),

  SCHEMA_NAME("schemaName"),
  /** The dataschema id. */
  DATASCHEMA_ID("dataSchemaId");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new type validation.
   *
   * @param value the value
   */
  ConditionsDrools(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
