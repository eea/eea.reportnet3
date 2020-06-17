package org.eea.interfaces.vo.dataflow.enums;

public enum FMEItemEnum {


  UNKNOWN("UNKNOWN"),

  WORKSPACE("WORKSPACE"),

  CUSTOMFORMAT("CUSTOMFORMAT"),

  CUSTOMTRANSFORM("CUSTOMTRANSFORM"),

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

