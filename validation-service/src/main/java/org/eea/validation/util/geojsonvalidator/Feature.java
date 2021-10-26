package org.eea.validation.util.geojsonvalidator;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The Class Feature.
 */
public class Feature extends GeoJsonObject {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 8961979092749948527L;

  /** The properties. */
  @JsonInclude(JsonInclude.Include.ALWAYS)
  private Map<String, Object> properties = new HashMap<>();

  /** The geometry. */
  @JsonInclude(JsonInclude.Include.ALWAYS)
  private GeoJsonObject geometry;

  /** The id. */
  private String id;

  /**
   * Sets the property.
   *
   * @param key the key
   * @param value the value
   */
  public void setProperty(String key, Object value) {
    properties.put(key, value);
  }

  /**
   * Gets the property.
   *
   * @param <T> the generic type
   * @param key the key
   * @return the property
   */
  @SuppressWarnings("unchecked")
  public <T> T getProperty(String key) {
    return (T) properties.get(key);
  }

  /**
   * Gets the properties.
   *
   * @return the properties
   */
  public Map<String, Object> getProperties() {
    return properties;
  }

  /**
   * Sets the properties.
   *
   * @param properties the properties
   */
  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  /**
   * Gets the geometry.
   *
   * @return the geometry
   */
  public GeoJsonObject getGeometry() {
    return geometry;
  }

  /**
   * Sets the geometry.
   *
   * @param geometry the new geometry
   */
  public void setGeometry(GeoJsonObject geometry) {
    this.geometry = geometry;
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
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
    if (o == null || getClass() != o.getClass())
      return false;
    if (!super.equals(o))
      return false;
    Feature feature = (Feature) o;
    if (properties != null ? !properties.equals(feature.properties) : feature.properties != null)
      return false;
    if (geometry != null ? !geometry.equals(feature.geometry) : feature.geometry != null)
      return false;
    return !(id != null ? !id.equals(feature.id) : feature.id != null);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (properties != null ? properties.hashCode() : 0);
    result = 31 * result + (geometry != null ? geometry.hashCode() : 0);
    result = 31 * result + (id != null ? id.hashCode() : 0);
    return result;
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "Feature{properties=" + properties + ", geometry=" + geometry + ", id='" + id + "'}";
  }
}
