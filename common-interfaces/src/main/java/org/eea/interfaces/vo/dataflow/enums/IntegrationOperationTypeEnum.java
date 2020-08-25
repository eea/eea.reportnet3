package org.eea.interfaces.vo.dataflow.enums;

/**
 * The Enum IntegrationOperationTypeEnum.
 */
public enum IntegrationOperationTypeEnum {

  /** The import. */
  IMPORT("IMPORT"),

  /** The export. */
  EXPORT("EXPORT"),

  /** The export eu dataset. */
  EXPORT_EU_DATASET("EXPORT_EU_DATASET"),

  /** The import with prefill data. */
  IMPORT_FROM_OTHER_SYSTEM("IMPORT_FROM_OTHER_SYSTEM");
  /** The value. */
  private final String value;

  /**
   * Instantiates a new integration operation type enum.
   *
   * @param value the value
   */
  IntegrationOperationTypeEnum(String value) {
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
