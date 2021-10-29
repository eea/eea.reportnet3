package org.eea.validation.util.geojsonvalidator;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class Geometry.
 *
 * @param <T> the generic type
 */
public abstract class Geometry<T> extends GeoJsonObject {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -3654018653678454032L;

  /** The coordinates. */
  protected List<T> coordinates = new ArrayList<T>();

  /**
   * Instantiates a new geometry.
   */
  public Geometry() {}

  /**
   * Instantiates a new geometry.
   *
   * @param elements the elements
   */
  public Geometry(T... elements) {
    for (T coordinate : elements) {
      coordinates.add(coordinate);
    }
  }

  /**
   * Adds the.
   *
   * @param elements the elements
   * @return the geometry
   */
  public Geometry<T> add(T elements) {
    coordinates.add(elements);
    return this;
  }

  /**
   * Gets the coordinates.
   *
   * @return the coordinates
   */
  public List<T> getCoordinates() {
    return coordinates;
  }

  /**
   * Sets the coordinates.
   *
   * @param coordinates the new coordinates
   */
  public void setCoordinates(List<T> coordinates) {
    this.coordinates = coordinates;
  }

  /**
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Geometry)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Geometry geometry = (Geometry) o;
    return !(coordinates != null ? !coordinates.equals(geometry.coordinates)
        : geometry.coordinates != null);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (coordinates != null ? coordinates.hashCode() : 0);
    return result;
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "Geometry{" + "coordinates=" + coordinates + "} " + super.toString();
  }
}


