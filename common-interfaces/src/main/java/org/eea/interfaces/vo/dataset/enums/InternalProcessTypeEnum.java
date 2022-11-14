package org.eea.interfaces.vo.dataset.enums;

/**
 * The Enum InternalProcessTypeEnum.
 */
public enum InternalProcessTypeEnum {

  DELETE("DELETE"),

  RESTORE("RESTORE");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new type error enum.
   *
   * @param value the value
   */
  InternalProcessTypeEnum(String value) {
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
