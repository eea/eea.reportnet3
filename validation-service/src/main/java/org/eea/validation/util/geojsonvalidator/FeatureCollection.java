package org.eea.validation.util.geojsonvalidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * The Class FeatureCollection.
 */
public class FeatureCollection extends GeoJsonObject implements Iterable<Feature> {


  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 121382103700730285L;

  /** The features. */
  private List<Feature> features = new ArrayList<Feature>();

  /**
   * Gets the features.
   *
   * @return the features
   */
  public List<Feature> getFeatures() {
    return features;
  }

  /**
   * Sets the features.
   *
   * @param features the new features
   */
  public void setFeatures(List<Feature> features) {
    this.features = features;
  }

  /**
   * Adds the.
   *
   * @param feature the feature
   * @return the feature collection
   */
  public FeatureCollection add(Feature feature) {
    features.add(feature);
    return this;
  }

  /**
   * Adds the all.
   *
   * @param features the features
   */
  public void addAll(Collection<Feature> features) {
    this.features.addAll(features);
  }

  /**
   * Iterator.
   *
   * @return the iterator
   */
  @Override
  public Iterator<Feature> iterator() {
    return features.iterator();
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
    if (!(o instanceof FeatureCollection))
      return false;
    FeatureCollection features1 = (FeatureCollection) o;
    return features.equals(features1.features);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return features.hashCode();
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "FeatureCollection{" + "features=" + features + '}';
  }
}
