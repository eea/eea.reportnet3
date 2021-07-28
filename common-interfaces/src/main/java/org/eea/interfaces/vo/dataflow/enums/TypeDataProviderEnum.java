package org.eea.interfaces.vo.dataflow.enums;

/** The Enum TypeDataProviderEnum. */
public enum TypeDataProviderEnum {

  /** The design. */
  DESIGN("COUNTRY"),

  /** The draft. */
  DRAFT("COMPANY");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new type status enum.
   *
   * @param value the value
   */
  TypeDataProviderEnum(String value) {
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
