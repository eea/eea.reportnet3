package org.eea.interfaces.vo.integration.enums;

/**
 * The Enum FMIOperation.
 */
public enum FMEOperation {

  /** The import. */
  IMPORT("IMPORT"),

  /** The export. */
  EXPORT("EXPORT"),

  /** The export eu dataset. */
  EXPORT_EU_DATASET("EXPORT_EU_DATASET");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new FMI operation.
   *
   * @param value the value
   */
  FMEOperation(String value) {
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
