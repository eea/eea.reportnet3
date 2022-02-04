package org.eea.interfaces.vo.dataset.enums;

import org.eea.interfaces.vo.dataset.schemas.rule.enums.JavaType;

/**
 * The Enum WebformTypeEnum.
 */
public enum WebformTypeEnum {


  /** The pams. */
  PAMS("PAMS", JavaType.STRING),


  /** The qa. */
  QA("QA", JavaType.STRING),


  /** The tables. */
  TABLES("TABLES", JavaType.STRING);

  /** The value. */
  private final String value;

  /** The java type. */
  private final String javaType;

  /**
   * Instantiates a new type entity enum.
   *
   * @param value the value
   */
  WebformTypeEnum(String value, String javaType) {
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

}
