package org.eea.validation.util.geojsonvalidator;

/**
 * The Class MultiPoint.
 */
public class MultiPoint extends Geometry<LngLatAlt> {


  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -4718764746818164507L;

  /**
   * Instantiates a new multi point.
   */
  public MultiPoint() {}

  /**
   * Instantiates a new multi point.
   *
   * @param points the points
   */
  public MultiPoint(LngLatAlt... points) {
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
    return "MultiPoint{} " + super.toString();
  }
}
