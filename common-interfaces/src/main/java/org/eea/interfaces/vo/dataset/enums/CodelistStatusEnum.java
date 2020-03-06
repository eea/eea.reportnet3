package org.eea.interfaces.vo.dataset.enums;

/**
 * The Enum CodelistStatusEnum.
 */
@Deprecated
public enum CodelistStatusEnum {

  /** The ready. */
  READY("READY"),

  /** The deprecated. */
  DEPRECATED("DEPRECATED"),

  /** The design. */
  DESIGN("DESIGN");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new codelist status enum.
   *
   * @param value the value
   */
  CodelistStatusEnum(String value) {
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
