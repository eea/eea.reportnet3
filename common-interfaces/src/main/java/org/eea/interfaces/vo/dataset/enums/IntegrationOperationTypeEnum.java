package org.eea.interfaces.vo.dataset.enums;



/**
 * The Enum IntegrationOperationTypeEnum.
 */
public enum IntegrationOperationTypeEnum {

  /** The import. */
  IMPORT("IMPORT"),

  /** The export. */
  EXPORT("EXPORT");

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
