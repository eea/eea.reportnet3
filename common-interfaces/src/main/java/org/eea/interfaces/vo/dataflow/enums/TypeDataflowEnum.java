package org.eea.interfaces.vo.dataflow.enums;


/**
 * The Enum TypeDataflowEnum.
 */
public enum TypeDataflowEnum {


  /** The regular. */
  REGULAR("REGULAR"),


  /** The reference. */
  REFERENCE("REFERENCE"),

  /** The business. */
  BUSINESS("BUSINESS"),

  /** The citizen science. */
  CITIZEN_SCIENCE("CITIZEN_SCIENCE");

  /** The value. */
  private final String value;


  /**
   * Instantiates a new type dataflow enum.
   *
   * @param value the value
   */
  TypeDataflowEnum(String value) {
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
