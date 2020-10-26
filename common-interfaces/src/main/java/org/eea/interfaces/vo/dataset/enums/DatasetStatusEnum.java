package org.eea.interfaces.vo.dataset.enums;

import org.eea.interfaces.vo.dataset.schemas.rule.enums.JavaType;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The Enum DatasetStatusEnum.
 */
public enum DatasetStatusEnum {

  /**
   * The Final Feedback.
   *
   * Cast in JPA: java.lang.String
   */
  FINAL_FEEDBACK("Final feedback", JavaType.STRING),

  /**
   * The Technically accept.
   *
   * Cast in JPA: java.lang.String
   */
  TECHNICALLY_ACCEPT("Technically accept", JavaType.STRING),

  /**
   * The pending release.
   *
   * Cast in JPA: java.lang.String
   */
  PENDING_RELEASE("pending release", JavaType.STRING),


  /**
   * The correction requested.
   *
   * Cast in JPA: java.lang.String
   */
  CORRECTION_REQUESTED("Correction requested", JavaType.STRING);

  /** The value. */
  private final String value;

  /** The java type. */
  private final String javaType;

  /**
   * Instantiates a new dataset status enum.
   *
   * @param value the value
   * @param javaType the java type
   */
  DatasetStatusEnum(String value, String javaType) {
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
