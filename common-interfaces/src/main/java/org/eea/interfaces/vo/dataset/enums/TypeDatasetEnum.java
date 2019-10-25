package org.eea.interfaces.vo.dataset.enums;



/**
 * The Enum TypeDatasetEnum.
 */
public enum TypeDatasetEnum {



  /** The reporting. */
  REPORTING("REPORTING"),


  /** The design. */
  DESIGN("DESIGN");


  /** The value. */
  private final String value;

  /**
   * Instantiates a new type error enum.
   *
   * @param value the value
   */
  TypeDatasetEnum(String value) {
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
