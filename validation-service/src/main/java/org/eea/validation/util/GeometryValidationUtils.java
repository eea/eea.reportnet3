package org.eea.validation.util;

import org.eea.validation.persistence.data.domain.FieldValue;
import org.geolatte.geom.GeometryCollection;
import org.geolatte.geom.LineString;
import org.geolatte.geom.MultiLineString;
import org.geolatte.geom.MultiPoint;
import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.Point;
import org.geolatte.geom.Polygon;
import org.springframework.stereotype.Component;

/**
 * The Class GeometryValidationUtils.
 */
@Component
public class GeometryValidationUtils {

  /**
   * Checks if is geometry.
   *
   * @param fieldValue the field value
   * @return true, if is geometry
   */
  public static boolean isGeometry(FieldValue fieldValue) {
    boolean rtn = GeoJsonValidationUtils.checkGeoJson(fieldValue);
    if (rtn) {
      switch (fieldValue.getType()) {
        case POINT:
          rtn = fieldValue.getValue().isEmpty() || fieldValue.getGeometry() instanceof Point;
          break;
        case LINESTRING:
          rtn = fieldValue.getValue().isEmpty() || fieldValue.getGeometry() instanceof LineString;
          break;
        case POLYGON:
          rtn = fieldValue.getValue().isEmpty() || fieldValue.getGeometry() instanceof Polygon;
          break;
        case MULTIPOINT:
          rtn = fieldValue.getValue().isEmpty() || fieldValue.getGeometry() instanceof MultiPoint;
          break;
        case MULTILINESTRING:
          rtn = fieldValue.getValue().isEmpty()
              || fieldValue.getGeometry() instanceof MultiLineString;
          break;
        case MULTIPOLYGON:
          rtn = fieldValue.getValue().isEmpty() || fieldValue.getGeometry() instanceof MultiPolygon;
          break;
        case GEOMETRYCOLLECTION:
          rtn = fieldValue.getValue().isEmpty()
              || fieldValue.getGeometry() instanceof GeometryCollection;
          break;
        default:
          rtn = false;
      }
    }
    return rtn;
  }

}
