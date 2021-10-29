package org.eea.validation.util;

import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.util.geojsonvalidator.Feature;
import org.eea.validation.util.geojsonvalidator.FeatureCollection;
import org.eea.validation.util.geojsonvalidator.GeoJsonObject;
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
        GeoJsonObject object = new ObjectMapper().readValue(aux, GeoJsonObject.class);
        if (object instanceof Point || object instanceof Polygon || object instanceof MultiPoint
            || object instanceof MultiPolygon || object instanceof LineString
            || object instanceof MultiLineString || object instanceof GeometryCollection
            || object instanceof Feature || object instanceof FeatureCollection) {
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
