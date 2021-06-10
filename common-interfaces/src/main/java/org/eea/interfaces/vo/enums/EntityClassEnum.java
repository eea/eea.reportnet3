package org.eea.interfaces.vo.enums;


/**
 * The Enum EntityClassEnum.
 */
public enum EntityClassEnum {


  /** The dataflow. */
  DATAFLOW("DATAFLOW"),


  /** The dataset. */
  DATASET("DATASET");



  /** The value. */
  private String value;


  /**
   * Instantiates a new entity class enum.
   *
   * @param value the value
   */
  private EntityClassEnum(String value) {
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
