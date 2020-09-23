package org.eea.interfaces.vo.dataflow.enums;

/**
 * The Enum FMEItemEnum.
 */
public enum FMEItemEnum {


  /** The unknown. */
  UNKNOWN("UNKNOWN"),

  /** The workspace. */
  WORKSPACE("WORKSPACE"),

  /** The customformat. */
  CUSTOMFORMAT("CUSTOMFORMAT"),

  /** The customtransform. */
  CUSTOMTRANSFORM("CUSTOMTRANSFORM"),

  /** The template. */
  TEMPLATE("TEMPLATE");

  /** The value. */
  private final String value;


  /**
   * Instantiates a new integration operation type enum.
   *
   * @param value the value
   */
  FMEItemEnum(String value) {
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

