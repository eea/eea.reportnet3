package org.eea.validation.util.geojsonvalidator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Class GeometryCollection.
 */
public class GeometryCollection extends GeoJsonObject implements Iterable<GeoJsonObject> {


  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -6350987590172038841L;

  /** The geometries. */
  private List<GeoJsonObject> geometries = new ArrayList<>();

  /**
   * Gets the geometries.
   *
   * @return the geometries
   */
  public List<GeoJsonObject> getGeometries() {
    return geometries;
  }

  /**
   * Sets the geometries.
   *
   * @param geometries the new geometries
   */
  public void setGeometries(List<GeoJsonObject> geometries) {
    this.geometries = geometries;
  }

  /**
   * Iterator.
   *
   * @return the iterator
   */
  @Override
  public Iterator<GeoJsonObject> iterator() {
    return geometries.iterator();
  }

  /**
   * Adds the.
   *
   * @param geometry the geometry
   * @return the geometry collection
   */
  public GeometryCollection add(GeoJsonObject geometry) {
    geometries.add(geometry);
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
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof GeometryCollection))
      return false;
    if (!super.equals(o))
      return false;
    GeometryCollection that = (GeometryCollection) o;
    return !(geometries != null ? !geometries.equals(that.geometries) : that.geometries != null);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (geometries != null ? geometries.hashCode() : 0);
    return result;
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "GeometryCollection{" + "geometries=" + geometries + "} " + super.toString();
  }
}


