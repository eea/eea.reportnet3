package org.eea.interfaces.vo.dataset.enums;


/**
 * The Enum TypeEntityEnum.
 */
public enum TypeEntityEnum {


  /** The table. */
  TABLE("TABLE"),

  /** The dataset. */
  DATASET("DATASET"),

  /** The field. */
  FIELD("FIELD"),

  /** The record. */
  RECORD("RECORD");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new type entity enum.
   *
   * @param value the value
   */
  private TypeEntityEnum(String value) {
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
