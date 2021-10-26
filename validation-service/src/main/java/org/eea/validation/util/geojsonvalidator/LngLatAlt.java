package org.eea.validation.util.geojsonvalidator;

import java.io.Serializable;
import java.util.Arrays;
import org.eea.validation.util.geojsonvalidator.jackson.LngLatAltDeserializer;
import org.eea.validation.util.geojsonvalidator.jackson.LngLatAltSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * The Class LngLatAlt.
 */
@JsonDeserialize(using = LngLatAltDeserializer.class)
@JsonSerialize(using = LngLatAltSerializer.class)
public class LngLatAlt implements Serializable {


  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -2644212917492968856L;

  /** The longitude. */
  private double longitude;

  /** The latitude. */
  private double latitude;

  /** The altitude. */
  private double altitude = Double.NaN;

  /** The additional elements. */
  private double[] additionalElements = new double[0];

  /**
   * Instantiates a new lng lat alt.
   */
  public LngLatAlt() {}

  /**
   * Instantiates a new lng lat alt.
   *
   * @param longitude the longitude
   * @param latitude the latitude
   */
  public LngLatAlt(double longitude, double latitude) {
    this.longitude = longitude;
    this.latitude = latitude;
  }

  /**
   * Instantiates a new lng lat alt.
   *
   * @param longitude the longitude
   * @param latitude the latitude
   * @param altitude the altitude
   */
  public LngLatAlt(double longitude, double latitude, double altitude) {
    this.longitude = longitude;
    this.latitude = latitude;
    this.altitude = altitude;
  }

  /**
   * Construct a LngLatAlt with additional elements. The specification allows for any number of
   * additional elements in a position, after lng, lat, alt.
   * http://geojson.org/geojson-spec.html#positions
   * 
   * @param longitude The longitude.
   * @param latitude The latitude.
   * @param altitude The altitude.
   * @param additionalElements The additional elements.
   */
  public LngLatAlt(double longitude, double latitude, double altitude,
      double... additionalElements) {
    this.longitude = longitude;
    this.latitude = latitude;
    this.altitude = altitude;

    setAdditionalElements(additionalElements);
    checkAltitudeAndAdditionalElements();
  }

  /**
   * Checks for altitude.
   *
   * @return true, if successful
   */
  public boolean hasAltitude() {
    return !Double.isNaN(altitude);
  }

  /**
   * Checks for additional elements.
   *
   * @return true, if successful
   */
  public boolean hasAdditionalElements() {
    return additionalElements.length > 0;
  }

  /**
   * Gets the longitude.
   *
   * @return the longitude
   */
  public double getLongitude() {
    return longitude;
  }

  /**
   * Sets the longitude.
   *
   * @param longitude the new longitude
   */
  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  /**
   * Gets the latitude.
   *
   * @return the latitude
   */
  public double getLatitude() {
    return latitude;
  }

  /**
   * Sets the latitude.
   *
   * @param latitude the new latitude
   */
  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  /**
   * Gets the altitude.
   *
   * @return the altitude
   */
  public double getAltitude() {
    return altitude;
  }

  /**
   * Sets the altitude.
   *
   * @param altitude the new altitude
   */
  public void setAltitude(double altitude) {
    this.altitude = altitude;
    checkAltitudeAndAdditionalElements();
  }

  /**
   * Gets the additional elements.
   *
   * @return the additional elements
   */
  public double[] getAdditionalElements() {
    return additionalElements;
  }

  /**
   * Sets the additional elements.
   *
   * @param additionalElements the new additional elements
   */
  public void setAdditionalElements(double... additionalElements) {
    if (additionalElements != null) {
      this.additionalElements = additionalElements;
    } else {
      this.additionalElements = new double[0];
    }

    for (double element : this.additionalElements) {
      if (Double.isNaN(element)) {
        throw new IllegalArgumentException("No additional elements may be NaN.");
      }
      if (Double.isInfinite(element)) {
        throw new IllegalArgumentException("No additional elements may be infinite.");
      }
    }

    checkAltitudeAndAdditionalElements();
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
    if (!(o instanceof LngLatAlt)) {
      return false;
    }
    LngLatAlt lngLatAlt = (LngLatAlt) o;
    return Double.compare(lngLatAlt.latitude, latitude) == 0
        && Double.compare(lngLatAlt.longitude, longitude) == 0
        && Double.compare(lngLatAlt.altitude, altitude) == 0
        && Arrays.equals(lngLatAlt.getAdditionalElements(), additionalElements);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    long temp = Double.doubleToLongBits(longitude);
    int result = (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(latitude);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(altitude);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    for (double element : additionalElements) {
      temp = Double.doubleToLongBits(element);
      result = 31 * result + (int) (temp ^ (temp >>> 32));
    }
    return result;
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    String s = "LngLatAlt{" + "longitude=" + longitude + ", latitude=" + latitude + ", altitude="
        + altitude;

    if (hasAdditionalElements()) {
      s += ", additionalElements=[";

      String suffix = "";
      for (Double element : additionalElements) {
        if (element != null) {
          s += suffix + element;
          suffix = ", ";
        }
      }
      s += ']';
    }

    s += '}';

    return s;
  }

  /**
   * Check altitude and additional elements.
   */
  private void checkAltitudeAndAdditionalElements() {
    if (!hasAltitude() && hasAdditionalElements()) {
      throw new IllegalArgumentException(
          "Additional Elements are only valid if Altitude is also provided.");
    }
  }
}
