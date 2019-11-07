package org.eea.interfaces.vo.dataset.enums;



/**
 * The Enum TypeErrorEnum.
 */
public enum TypeErrorEnum {

  /** The correct. */
  CORRECT("CORRECT"),

  /** The warning. */
  WARNING("WARNING"),

  /** The error. */
  ERROR("ERROR"),

  INFO("INFO"),

  BLOCKER("BLOCKER");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new type error enum.
   *
   * @param value the value
   */
  TypeErrorEnum(String value) {
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
