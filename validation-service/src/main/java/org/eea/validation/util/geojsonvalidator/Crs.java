package org.eea.validation.util.geojsonvalidator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.eea.validation.util.geojsonvalidator.jackson.CrsType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Crs implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -4885013148789896631L;

  /** The type. */
  private CrsType type = CrsType.name;

  /** The properties. */
  private Map<String, Object> properties = new HashMap<>();



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
    if (!(o instanceof Crs)) {
      return false;
    }
    Crs crs = (Crs) o;
    if (properties != null ? !properties.equals(crs.properties) : crs.properties != null) {
      return false;
    }
    return !(type != null ? !type.equals(crs.type) : crs.type != null);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    int result = type != null ? type.hashCode() : 0;
    result = 31 * result + (properties != null ? properties.hashCode() : 0);
    return result;
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "Crs{" + "type='" + type + '\'' + ", properties=" + properties + '}';
  }
}
