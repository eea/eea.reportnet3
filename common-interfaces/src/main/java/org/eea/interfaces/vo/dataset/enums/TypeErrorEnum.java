package org.eea.interfaces.vo.dataset.enums;



/**
 * The Enum TypeErrorEnum.
 */
public enum TypeErrorEnum {


  /** The warning. */
  WARNING(1),

  /** The error. */
  ERROR(2);


  /** The value. */
  private final int value;

  /**
   * Instantiates a new type error enum.
   *
   * @param value the value
   */
  private TypeErrorEnum(int value) {
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
