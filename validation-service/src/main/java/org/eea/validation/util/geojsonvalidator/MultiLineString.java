package org.eea.validation.util.geojsonvalidator;

import java.util.List;

/**
 * The Class MultiLineString.
 */
public class MultiLineString extends Geometry<List<LngLatAlt>> {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -760888579418629910L;

  /**
   * Instantiates a new multi line string.
   */
  public MultiLineString() {}

  /**
   * Instantiates a new multi line string.
   *
   * @param line the line
   */
  public MultiLineString(List<LngLatAlt> line) {
    add(line);
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
    return "MultiLineString{} " + super.toString();
  }
}


