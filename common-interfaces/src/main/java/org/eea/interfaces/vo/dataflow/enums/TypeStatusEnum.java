package org.eea.interfaces.vo.dataflow.enums;

/** The Enum TypeStatusEnum. */
public enum TypeStatusEnum {

  /** The design. */
  DESIGN("DESIGN"),

  /** The draft. */
  DRAFT("DRAFT");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new type status enum.
   *
   * @param value the value
   */
  TypeStatusEnum(String value) {
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
