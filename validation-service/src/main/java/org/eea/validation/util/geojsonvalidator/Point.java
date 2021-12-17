package org.eea.validation.util.geojsonvalidator;

/**
 * The Class Point.
 */
public class Point extends GeoJsonObject {


  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 5558960526250040727L;

  /** The coordinates. */
  private LngLatAlt coordinates;

  /**
   * Instantiates a new point.
   */
  public Point() {}

  /**
   * Instantiates a new point.
   *
   * @param coordinates the coordinates
   */
  public Point(LngLatAlt coordinates) {
    this.coordinates = coordinates;
  }

  /**
   * Instantiates a new point.
   *
   * @param longitude the longitude
   * @param latitude the latitude
   */
  public Point(double longitude, double latitude) {
    coordinates = new LngLatAlt(longitude, latitude);
  }

  /**
   * Instantiates a new point.
   *
   * @param longitude the longitude
   * @param latitude the latitude
   * @param altitude the altitude
   */
  public Point(double longitude, double latitude, double altitude) {
    coordinates = new LngLatAlt(longitude, latitude, altitude);
  }

  /**
   * Instantiates a new point.
   *
   * @param longitude the longitude
   * @param latitude the latitude
   * @param altitude the altitude
   * @param additionalElements the additional elements
   */
  public Point(double longitude, double latitude, double altitude, double... additionalElements) {
    coordinates = new LngLatAlt(longitude, latitude, altitude, additionalElements);
  }

  /**
   * Gets the coordinates.
   *
   * @return the coordinates
   */
  public LngLatAlt getCoordinates() {
    return coordinates;
  }

  /**
   * Sets the coordinates.
   *
   * @param coordinates the new coordinates
   */
  public void setCoordinates(LngLatAlt coordinates) {
    this.coordinates = coordinates;
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
    if (this == o) {
      return true;
    }
    if (!(o instanceof Point)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Point point = (Point) o;
    return !(coordinates != null ? !coordinates.equals(point.coordinates)
        : point.coordinates != null);
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
    return "Point{" + "coordinates=" + coordinates + "} " + super.toString();
  }
}
