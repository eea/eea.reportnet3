package org.eea.validation.util.geojsonvalidator;

import java.io.Serializable;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * The Class GeoJsonObject.
 */
@JsonTypeInfo(property = "type", use = Id.NAME)
@JsonSubTypes({@Type(Feature.class), @Type(Polygon.class), @Type(MultiPolygon.class),
    @Type(FeatureCollection.class), @Type(Point.class), @Type(MultiPoint.class),
    @Type(MultiLineString.class), @Type(LineString.class), @Type(GeometryCollection.class)})
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class GeoJsonObject implements Serializable {


  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 2961913318830434580L;

  /** The crs. */
  private Crs crs;

  /** The bbox. */
  private double[] bbox;

  /**
   * Gets the crs.
   *
   * @return the crs
   */
  public Crs getCrs() {
    return crs;
  }

  /**
   * Sets the crs.
   *
   * @param crs the new crs
   */
  public void setCrs(Crs crs) {
    this.crs = crs;
  }

  /**
   * Gets the bbox.
   *
   * @return the bbox
   */
  public double[] getBbox() {
    return bbox;
  }

  /**
   * Sets the bbox.
   *
   * @param bbox the new bbox
   */
  public void setBbox(double[] bbox) {
    this.bbox = bbox;
  }

  /**
   * Accept.
   *
   * @param <T> the generic type
   * @param geoJsonObjectVisitor the geo json object visitor
   * @return the t
   */
  public abstract <T> T accept(GeoJsonObjectVisitor<T> geoJsonObjectVisitor);

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
    if (o == null || getClass() != o.getClass())
      return false;
    GeoJsonObject that = (GeoJsonObject) o;
    if (crs != null ? !crs.equals(that.crs) : that.crs != null)
      return false;
    return Arrays.equals(bbox, that.bbox);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    int result = crs != null ? crs.hashCode() : 0;
    result = 31 * result + (bbox != null ? Arrays.hashCode(bbox) : 0);
    return result;
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "GeoJsonObject{}";
  }
}
