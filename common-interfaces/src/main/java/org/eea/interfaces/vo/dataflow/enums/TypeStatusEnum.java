package org.eea.interfaces.vo.dataflow.enums;

/**
 * The Enum TypeStatusEnum.
 */
public enum TypeStatusEnum {

  /** The draft. */
  DRAFT("DRAFT"),


  /** The accepted. */
  ACCEPTED("ACCEPTED"),


  /** The completed. */
  COMPLETED("COMPLETED");


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
