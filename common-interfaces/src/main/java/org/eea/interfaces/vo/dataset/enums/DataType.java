package org.eea.interfaces.vo.dataset.enums;

import java.util.Arrays;
import org.eea.interfaces.vo.dataset.schemas.rule.enums.JavaType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The Enum TypeData.
 */
public enum DataType {

  /**
   * The Text.
   *
   * Cast in JPA: java.lang.String
   */
  TEXT("TEXT", JavaType.STRING),

  /**
   * The Text with more than 10000 characters.
   *
   * Cast in JPA: java.lang.String
   */
  LONG_TEXT("LONG_TEXT", JavaType.STRING),

  /**
   * The Number Integer.
   *
   * Cast in JPA: CAST(fv.value as java.math.BigDecimal)
   */
  NUMBER_INTEGER("NUMBER_INTEGER", JavaType.NUMBER),

  /**
   * The Number Decimal.
   *
   * Cast in JPA: CAST(fv.value as java.math.BigDecimal)
   */
  NUMBER_DECIMAL("NUMBER_DECIMAL", JavaType.NUMBER),
  /**
   * The Date.
   *
   * Cast in JPA. CAST(VALUE as java.sql.Date) Supports any date
   */
  DATE("DATE", JavaType.DATE),

  /**
   * The Boolean.
   *
   * Cast in JPA: java.lang.Boolean
   */
  BOOLEAN("BOOLEAN", JavaType.BOOLEAN),

  /**
   * The geometry.
   * 
   * A String representing a GeoJSON object. ObjectMapper.readTree(...) should be used to transform
   * into JSON.
   */
  GEOMETRY("GEOMETRY", JavaType.JSON),

  /**
   * The codelist.
   *
   * Cast in JPA: java.lang.String
   */
  CODELIST("CODELIST", JavaType.STRING),

  /**
   * The codelist with more than one value avaliable.
   *
   * Cast in JPA: java.lang.String
   */
  MULTISELECT_CODELIST("MULTISELECT_CODELIST", JavaType.STRING),

  /**
   * The link data with PK.
   *
   * Cast in JPA: java.lang.String
   */
  LINK("LINK", JavaType.STRING),

  /**
   * The link data with PK.
   *
   * Cast in JPA: java.lang.String
   */
  LINK_DATA("LINK_DATA", JavaType.STRING),

  /**
   * The url valid.
   *
   * Cast in JPA: java.lang.String
   */
  URL("URL", JavaType.STRING),

  /**
   * The phone.
   *
   * Cast in JPA: java.lang.String
   */
  PHONE("PHONE", JavaType.STRING),

  /**
   * The email.
   *
   * Cast in JPA: java.lang.String
   */
  EMAIL("EMAIL", JavaType.STRING),

  /**
   * The attachment.
   *
   * Cast in JPA: java.lang.String
   */
  ATTACHMENT("ATTACHMENT", JavaType.STRING);

  /** The value. */
  private final String value;

  /** The java type. */
  private final String javaType;

  /**
   * Instantiates a new type entity enum.
   *
   * @param value the value
   * @param javaType the java type
   */
  DataType(String value, String javaType) {
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
   * From value.
   *
   * @param value the value
   * @return the data type
   */
  @JsonCreator
  public static DataType fromValue(String value) {
    return Arrays.stream(DataType.values()).filter(e -> e.value.equals(value)).findFirst()
        .orElse(DataType.TEXT);
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
