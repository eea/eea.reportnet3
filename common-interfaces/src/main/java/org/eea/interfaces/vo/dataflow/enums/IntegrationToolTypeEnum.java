package org.eea.interfaces.vo.dataflow.enums;



/**
 * The Enum IntegrationToolTypeEnum.
 */
public enum IntegrationToolTypeEnum {


  /** The fme. */
  FME("FME");


  /** The value. */
  private final String value;



  /**
   * Instantiates a new integration tool type enum.
   *
   * @param value the value
   */
  IntegrationToolTypeEnum(String value) {
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
