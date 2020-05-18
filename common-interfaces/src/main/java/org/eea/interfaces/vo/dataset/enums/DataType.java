package org.eea.interfaces.vo.dataset.enums;

import java.util.Arrays;
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
  TEXT("TEXT", "String"),

  /**
   * The Text with more than 10000 characters.
   *
   * Cast in JPA: java.lang.String
   */
  LONG_TEXT("LONG_TEXT", "String"),

  /**
   * The Number Integer.
   *
   * Cast in JPA: CAST(fv.value as java.math.BigDecimal)
   */
  NUMBER_INTEGER("NUMBER_INTEGER", "Number"),


  /**
   * The Number Decimal.
   *
   * Cast in JPA: CAST(fv.value as java.math.BigDecimal)
   */
  NUMBER_DECIMAL("NUMBER_DECIMAL", "Number"),
  /**
   * The Date.
   *
   * Cast in JPA. CAST(VALUE as java.sql.Date) Supports any date
   */
  DATE("DATE", "Date"),

  /**
   * The Boolean.
   *
   * Cast in JPA: java.lang.Boolean
   */
  BOOLEAN("BOOLEAN", "Boolean"),

  /**
   * The coordinate lat. that value is a float
   *
   * Cast in JPA. CAST(fv.value as java.lang.Double)
   */
  COORDINATE_LAT("COORDINATE_LAT", "Number"),

  /**
   * The coordinate long. that value is a float
   *
   * Cast in JPA. CAST(fv.value as java.lang.Double)
   */
  COORDINATE_LONG("COORDINATE_LONG", "Number"),

  /**
   * The point.
   *
   * Cast in JPA: org.postgresql.geometric.PGpoint
   *
   * select point(1.0,1.0); select ST_GeomFromText('point(1.0 1.0)');
   */
  POINT("POINT", "Unsupported"),

  /**
   * The circle.
   *
   * Cast in JPA: java.lang.Object
   *
   * in this function the point is the center of the circle, and the other is the radius. select
   * circle(point(10,10),5); select ST_GeomFromText('CIRCULARSTRING(1 1 , 10 1 , 10 30)');
   */
  CIRCLE("CIRCLE", "Unsupported"),

  /**
   * The polygon.
   *
   * Cast in JPA: org.postgresql.geometric.PGpolygon
   *
   * In this function the first and the last value(point) must be the same. select
   * ST_GeomFromText('POLYGON((17.0 30.0 , 15.0 12.0 , -15.0 -30.0 , 17.0 30.0))');
   *
   *
   * select POLYGON(path'((17.0,30.0) , (15.0,12.0) , (-15.0,-30.0) , (17.0,30.0))');
   */
  POLYGON("POLYGON", "Unsupported"),

  /**
   * The codelist.
   *
   * Cast in JPA: java.lang.String
   */
  CODELIST("CODELIST", "String"),

  /**
   * The codelist with more than one value avaliable.
   *
   * Cast in JPA: java.lang.String
   */
  MULTISELECT_CODELIST("MULTISELECT_CODELIST", "String"),
  /**
   * The link data with PK.
   *
   * Cast in JPA: java.lang.String
   */
  LINK("LINK", "String"),

  /**
   * The link data with PK.
   *
   * Cast in JPA: java.lang.String
   */
  LINK_DATA("LINK_DATA", "String"),

  /**
   * The url valid.
   *
   * Cast in JPA: java.lang.String
   */
  URL("URL", "String"),

  /**
   * The phone.
   *
   * Cast in JPA: java.lang.String
   */
  PHONE("PHONE", "String"),

  /**
   * The email.
   *
   * Cast in JPA: java.lang.String
   */
  EMAIL("EMAIL", "String");


  /**
   * The value.
   */
  private final String value;

  private final String javaType;

  /**
   * Instantiates a new type entity enum.
   *
   * @param value the value
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

  public String getJavaType() {
    return javaType;
  }

  /**
   * From value data type.
   *
   * @param value the value
   *
   * @return the data type
   */
  @JsonCreator
  public static DataType fromValue(String value) {
    return Arrays.stream(DataType.values()).filter(e -> e.value.equals(value)).findFirst().get();
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
