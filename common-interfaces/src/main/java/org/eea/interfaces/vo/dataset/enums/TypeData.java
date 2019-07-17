package org.eea.interfaces.vo.dataset.enums;

/**
 * The Enum TypeData.
 */
public enum TypeData {


  /** The string. */
  TEXT("TEXT"),
  /** The integer. */
  NUMBER("NUMBER"),
  /** The long. */
  DATE("DATE"),
  /** The boolean. */
  BOOLEAN("BOOLEAN"),
  /**
   * The coordinate lat. that value is a float
   */
  COORDINATE_LAT("COORDINATE_LAT"),
  /**
   * The coordinate long. that value is a float
   */
  COORDINATE_LONG("COORDINATE_LONG"),
  /**
   * The geometry. polyngons
   */
  GEOMETRY("GEOMETRY"),
  /**
   * The goegraphy. when field value has a (Latitude and Longitude)
   */
  GOEGRAPHY("GOEGRAPHY");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new type entity enum.
   *
   * @param value the value
   */
  TypeData(String value) {
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
