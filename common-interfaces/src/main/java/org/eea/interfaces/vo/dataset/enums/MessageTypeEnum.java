package org.eea.interfaces.vo.dataset.enums;

import org.eea.interfaces.vo.dataset.schemas.rule.enums.JavaType;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageTypeEnum {

  /** The text. */
  TEXT("TEXT", JavaType.STRING),

  /** The attachment. */
  ATTACHMENT("ATTACHMENT", JavaType.STRING);

  /** The value. */
  private final String value;

  /** The java type. */
  private final String javaType;

  /**
   * Instantiates a new message type enum.
   *
   * @param value the value
   * @param javaType the java type
   */
  MessageTypeEnum(String value, String javaType) {
    this.value = value;
    this.javaType = javaType;
  }

  /**
   * Gets the value.
   *
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * Gets the java type.
   *
   * @return the java type
   */
  public String getJavaType() {
    return javaType;
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  @JsonValue
  public String toString() {
    return this.value;
  }
}
