package org.eea.interfaces.vo.dataset.enums;



/**
 * The Enum TypeErrorEnum.
 */
public enum ErrorTypeEnum {

  /** The correct. */
  CORRECT("CORRECT"),

  /** The warning. */
  WARNING("WARNING"),

  /** The error. */
  ERROR("ERROR"),

  /** The info. */
  INFO("INFO"),

  /** The blocker. */
  BLOCKER("BLOCKER");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new type error enum.
   *
   * @param value the value
   */
  ErrorTypeEnum(String value) {
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
