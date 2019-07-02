package org.eea.interfaces.vo.dataflow.enums;

/**
 * The Enum TypeRequestEnum.
 */
public enum TypeRequestEnum {

  /** The mandatory. */
  PENDING("PENDING"),

  /** The optional. */
  ACCEPTED("ACCEPTED"),

  /** The rejected. */
  REJECTED("REJECTED");

  /** The value. */
  private final String value;


  /**
   * Instantiates a new type request enum.
   *
   * @param value the value
   */
  TypeRequestEnum(String value) {
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
