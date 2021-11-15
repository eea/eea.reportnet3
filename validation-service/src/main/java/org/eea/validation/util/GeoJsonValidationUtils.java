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
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonSyntaxException;

/**
 * The Class GeoJsonValidationUtils.
 */
@Component
public class GeoJsonValidationUtils {


  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(GeoJsonValidationUtils.class);

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
      if (!aux.isEmpty() && !locateSpecialChar(aux)) {
        JSONObject jsonObject = new JSONObject(aux);
        String auxGEometry = jsonObject.get("geometry").toString();
        switch (fieldValue.getType()) {
          case POINT:
            Point point = new ObjectMapper().readValue(auxGEometry, Point.class);
            if (point instanceof Point
                && point.getCoordinates().getAdditionalElements().length > 0) {
              StringBuilder errorList = new StringBuilder();
              for (double a : point.getCoordinates().getAdditionalElements()) {
                errorList.append(String.valueOf(" " + a));
              }
              result = "This geometry contains more values than expected: " + errorList.toString();
            } else {
              result = "";
            }
            break;
          case LINESTRING:
            LineString lineString = new ObjectMapper().readValue(auxGEometry, LineString.class);
            if (lineString instanceof LineString) {
              result = "";
            }
            break;
          case POLYGON:
            Polygon polygon = new ObjectMapper().readValue(auxGEometry, Polygon.class);
            if (polygon instanceof Polygon) {
              result = "";
            }
            break;
          case MULTIPOINT:
            MultiPoint multiPoint = new ObjectMapper().readValue(auxGEometry, MultiPoint.class);
            if (multiPoint instanceof MultiPoint) {
              result = "";
            }
            break;
          case MULTILINESTRING:
            MultiLineString multiLineString =
                new ObjectMapper().readValue(auxGEometry, MultiLineString.class);
            if (multiLineString instanceof MultiLineString) {
              result = "";
            }
            break;
          case MULTIPOLYGON:
            MultiPolygon multiPolygon =
                new ObjectMapper().readValue(auxGEometry, MultiPolygon.class);
            if (multiPolygon instanceof MultiPolygon) {
              result = "";
            }
            break;
          case GEOMETRYCOLLECTION:
            GeometryCollection geometryCollection =
                new ObjectMapper().readValue(auxGEometry, GeometryCollection.class);
            if (geometryCollection instanceof GeometryCollection) {
              result = "";
            }
            break;
          default:
            result = "";
        }
      }
    } catch (JsonProcessingException | JSONException | JsonSyntaxException e) {
      LOG.info("This field: {} is not a valid geometry. Reason: {}", fieldValue.getId(),
          e.getMessage());
      result = e.getMessage();
    }
    return result;
  }



  /**
   * Locate special char.
   *
   * @param geoJson the geo json
   * @return true, if successful
   * @throws JSONException the JSON exception
   */
  private static boolean locateSpecialChar(String geoJson) throws JSONException {
    boolean rtn = false;
    char c = 0;
    int i;
    int len = geoJson.length();
    StringBuilder sb = new StringBuilder(len + 4);
    for (i = 0; i < len; i += 1) {
      c = geoJson.charAt(i);
      switch (c) {
        case '\b':
        case '\t':
        case '\n':
        case '\f':
        case '\r':
          throw new JSONException(
              "This GeoJson contains forbidden characters: '\\b', '\\t', '\\n', '\\f', '\\r'.");
        default:
          sb.append(c);
          break;
      }
    }
    if (sb.length() == 0) {
      rtn = true;
    }
    return rtn;
  }

}
