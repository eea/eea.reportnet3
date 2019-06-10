package org.eea.interfaces.vo.dataset.enums;


/**
 * The Enum TypeEntityEnum.
 */
public enum TypeEntityEnum {


  /** The table. */
  TABLE(1),

  /** The dataset. */
  DATASET(2),

  /** The field. */
  FIELD(3),

  /** The record. */
  RECORD(4);

  /** The value. */
  private final int value;

  /**
   * Instantiates a new type entity enum.
   *
   * @param value the value
   */
  private TypeEntityEnum(int value) {
    this.value = value;
  }

  /**
   * Gets the value.
   *
   * @return the value
   */
  public int getValue() {
    return value;
  }

}
