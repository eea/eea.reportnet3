package org.eea.validation.util.geojsonvalidator;

import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class Polygon.
 */
public class Polygon extends Geometry<List<LngLatAlt>> {


  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 4496755859699809882L;

  /**
   * Instantiates a new polygon.
   */
  public Polygon() {}

  /**
   * Instantiates a new polygon.
   *
   * @param polygon the polygon
   */
  public Polygon(List<LngLatAlt> polygon) {
    add(polygon);
  }

  /**
   * Instantiates a new polygon.
   *
   * @param polygon the polygon
   */
  public Polygon(LngLatAlt... polygon) {
    add(Arrays.asList(polygon));
  }

  /**
   * Sets the exterior ring.
   *
   * @param points the new exterior ring
   */
  public void setExteriorRing(List<LngLatAlt> points) {
    if (coordinates.isEmpty()) {
      coordinates.add(0, points);
    } else {
      coordinates.set(0, points);
    }
  }

  /**
   * Gets the exterior ring.
   *
   * @return the exterior ring
   */
  @JsonIgnore
  public List<LngLatAlt> getExteriorRing() {
    assertExteriorRing();
    return coordinates.get(0);
  }

  /**
   * Gets the interior rings.
   *
   * @return the interior rings
   */
  @JsonIgnore
  public List<List<LngLatAlt>> getInteriorRings() {
    assertExteriorRing();
    return coordinates.subList(1, coordinates.size());
  }

  /**
   * Gets the interior ring.
   *
   * @param index the index
   * @return the interior ring
   */
  public List<LngLatAlt> getInteriorRing(int index) {
    assertExteriorRing();
    return coordinates.get(1 + index);
  }

  /**
   * Adds the interior ring.
   *
   * @param points the points
   */
  public void addInteriorRing(List<LngLatAlt> points) {
    assertExteriorRing();
    coordinates.add(points);
  }

  /**
   * Adds the interior ring.
   *
   * @param points the points
   */
  public void addInteriorRing(LngLatAlt... points) {
    assertExteriorRing();
    coordinates.add(Arrays.asList(points));
  }

  /**
   * Assert exterior ring.
   */
  private void assertExteriorRing() {
    if (coordinates.isEmpty())
      throw new RuntimeException("No exterior ring definied");
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
    return "Polygon{} " + super.toString();
  }
}
