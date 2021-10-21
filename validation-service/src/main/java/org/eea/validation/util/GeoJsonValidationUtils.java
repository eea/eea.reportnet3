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
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class GeoJsonValidationUtils {

  public static boolean checkGeoJson(FieldValue fieldValue) {
    Boolean result = null;
    int errorcount = 0;
    try {
      String aux = fieldValue.getValue();
      if (!aux.isEmpty()) {
        GeoJsonObject object = new ObjectMapper().readValue(aux, GeoJsonObject.class);
        if (object instanceof Point) {
          result = true;
        } else if (object instanceof Polygon) {
          result = true;
        } else if (object instanceof MultiPoint) {
          result = true;
        } else if (object instanceof MultiPolygon) {
          result = true;
        } else if (object instanceof LineString) {
          result = true;
        } else if (object instanceof MultiLineString) {
          result = true;
        } else if (object instanceof GeometryCollection) {
          result = false;
        } else if (object instanceof Feature) {
          result = true;
        } else if (object instanceof FeatureCollection) {
          result = true;
        }
      } else {
        result = true;
      }
    } catch (JsonProcessingException e) {
      errorcount++;
      System.out.println(e.getMessage());
      System.out.println(errorcount);
      result = false;
    }
    System.out.println("===========================");
    System.out.println(result);
    return result;
  }

}
