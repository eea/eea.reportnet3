package org.eea.interfaces.vo.dataset.enums;

/**
 * The Enum TypeData.
 */
public enum DataType {

  /**
   * The Text.
   * 
   * Cast in JPA: java.lang.String
   * 
   */
  TEXT("TEXT"),

  /**
   * The Number.
   * 
   * Cast in JPA: CAST(fv.value as java.math.BigDecimal)
   */
  NUMBER("NUMBER"),

  /**
   * The Date.
   * 
   * Cast in JPA. CAST(VALUE as java.sql.Date) Supports any date
   */
  DATE("DATE"),

  /**
   * The Boolean.
   * 
   * Cast in JPA: java.lang.Boolean
   * 
   */
  BOOLEAN("BOOLEAN"),

  /**
   * The coordinate lat. that value is a float
   * 
   * Cast in JPA. CAST(fv.value as java.lang.Double)
   */
  COORDINATE_LAT("COORDINATE_LAT"),

  /**
   * The coordinate long. that value is a float
   * 
   * Cast in JPA. CAST(fv.value as java.lang.Double)
   */
  COORDINATE_LONG("COORDINATE_LONG"),

  /**
   * The point.
   * 
   * Cast in JPA: org.postgresql.geometric.PGpoint
   * 
   * select point(1.0,1.0); select ST_GeomFromText('point(1.0 1.0)');
   * 
   */
  POINT("POINT"),

  /**
   * The circle.
   * 
   * Cast in JPA: java.lang.Object
   * 
   * in this function the point is the center of the circle, and the other is the radius. select
   * circle(point(10,10),5); select ST_GeomFromText('CIRCULARSTRING(1 1 , 10 1 , 10 30)');
   * 
   */
  CIRCLE("CIRCLE"),

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
   * 
   */
  POLYGON("POLYGON"),


  /** The codelist. */
  CODELIST("CODELIST");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new type entity enum.
   *
   * @param value the value
   */
  DataType(String value) {
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
