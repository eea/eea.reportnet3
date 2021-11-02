package org.eea.validation.util;

import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.util.geojsonvalidator.GeometryCollection;
import org.eea.validation.util.geojsonvalidator.LineString;
import org.eea.validation.util.geojsonvalidator.MultiLineString;
import org.eea.validation.util.geojsonvalidator.MultiPoint;
import org.eea.validation.util.geojsonvalidator.MultiPolygon;
import org.eea.validation.util.geojsonvalidator.Point;
import org.eea.validation.util.geojsonvalidator.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class GeoJsonValidationUtils.
 */
@Component
public class GeoJsonValidationUtils {


  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(SQLValidationUtils.class);

  /**
   * Check geo json.
   *
   * @param fieldValue the field value
   * @return the string
   */
  public static String checkGeoJson(FieldValue fieldValue) {
    String result = "";
    try {
      String aux = fieldValue.getValue();
      if (!aux.isEmpty()) {
        switch (fieldValue.getType()) {
          case POINT:
            Point point = new ObjectMapper().readValue(aux, Point.class);
            if (point instanceof Point) {
              result = "";
            }
            break;
          case LINESTRING:
            LineString lineString = new ObjectMapper().readValue(aux, LineString.class);
            if (lineString instanceof LineString) {
              result = "";
            }
            break;
          case POLYGON:
            Polygon polygon = new ObjectMapper().readValue(aux, Polygon.class);
            if (polygon instanceof Polygon) {
              result = "";
            }
            break;
          case MULTIPOINT:
            MultiPoint multiPoint = new ObjectMapper().readValue(aux, MultiPoint.class);
            if (multiPoint instanceof MultiPoint) {
              result = "";
            }
            break;
          case MULTILINESTRING:
            MultiLineString multiLineString =
                new ObjectMapper().readValue(aux, MultiLineString.class);
            if (multiLineString instanceof MultiLineString) {
              result = "";
            }
            break;
          case MULTIPOLYGON:
            MultiPolygon multiPolygon = new ObjectMapper().readValue(aux, MultiPolygon.class);
            if (multiPolygon instanceof MultiPolygon) {
              result = "";
            }
            break;
          case GEOMETRYCOLLECTION:
            GeometryCollection geometryCollection =
                new ObjectMapper().readValue(aux, GeometryCollection.class);
            if (geometryCollection instanceof GeometryCollection) {
              result = "";
            }
            break;
          default:
            result = "";
        }
      }
    } catch (JsonProcessingException e) {
      LOG_ERROR.error("This field: {} is not a valid geometry. Reason: {}", fieldValue.getId(),
          e.getMessage());
      result = e.getMessage();
    }
    return result;
  }

}
