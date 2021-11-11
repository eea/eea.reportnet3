package org.eea.interfaces.vo.communication.enums;

/**
 * The Enum NotificationLevelEnum.
 */
public enum NotificationLevelEnum {

  /** The success. */
  SUCCESS("SUCCESS"),

  /** The info. */
  INFO("INFO"),

  /** The error. */
  ERROR("ERROR");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new notification level enum.
   *
   * @param value the value
   */
  NotificationLevelEnum(String value) {
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
