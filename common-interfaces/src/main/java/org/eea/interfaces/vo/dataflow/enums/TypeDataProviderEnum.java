package org.eea.interfaces.vo.dataflow.enums;

/** The Enum TypeDataProviderEnum. */
public enum TypeDataProviderEnum {

  /** The country. */
  COUNTRY("COUNTRY"),

  /** The company. */
  COMPANY("COMPANY"),

  /** The organization. */
  ORGANIZATION("ORGANIZATION");

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
