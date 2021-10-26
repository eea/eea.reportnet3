package org.eea.validation.util.geojsonvalidator;

/**
 * The Class LineString.
 */
public class LineString extends MultiPoint {


  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -2260953457499977782L;

  /**
   * Instantiates a new line string.
   */
  public LineString() {}

  /**
   * Instantiates a new line string.
   *
   * @param points the points
   */
  public LineString(LngLatAlt... points) {
    super(points);
  }

  /**
   * Accept.
   *
   * @param <T> the generic type
   * @param geoJsonObjectVisitor the geo json object visitor
   * @return the t
   */
  @Override
  public <T> T accept(GeoJsonObjectVisitor<T> geoJsonObjectVisitor) {
    return geoJsonObjectVisitor.visit(this);
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "LineString{} " + super.toString();
  }
}
