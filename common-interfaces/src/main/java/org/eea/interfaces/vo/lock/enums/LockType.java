package org.eea.interfaces.vo.lock.enums;

/**
 * The Enum LockType.
 */
public enum LockType {

  /**
   * The method.
   */
  METHOD("METHOD"),

  /**
   * The entity.
   */
  ENTITY("ENTITY");

  /**
   * The value.
   */
  private final String value;

  /**
   * Instantiates a new lock type.
   *
   * @param value the value
   */
  LockType(String value) {
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
