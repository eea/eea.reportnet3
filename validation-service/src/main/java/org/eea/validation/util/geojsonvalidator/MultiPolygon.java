package org.eea.validation.util.geojsonvalidator;

import java.util.List;

/**
 * The Class MultiPolygon.
 */
public class MultiPolygon extends Geometry<List<List<LngLatAlt>>> {


  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -82747375703089667L;

  /**
   * Instantiates a new multi polygon.
   */
  public MultiPolygon() {}

  /**
   * Instantiates a new multi polygon.
   *
   * @param polygon the polygon
   */
  public MultiPolygon(Polygon polygon) {
    add(polygon);
  }

  /**
   * Adds the.
   *
   * @param polygon the polygon
   * @return the multi polygon
   */
  public MultiPolygon add(Polygon polygon) {
    coordinates.add(polygon.getCoordinates());
    return this;
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
    return "MultiPolygon{} " + super.toString();
  }
}
