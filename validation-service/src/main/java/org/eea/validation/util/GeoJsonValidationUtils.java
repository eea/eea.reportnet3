package org.eea.validation.util;

import java.util.List;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.util.geojsonvalidator.GeoJsonObject;
import org.eea.validation.util.geojsonvalidator.GeometryCollection;
import org.eea.validation.util.geojsonvalidator.LineString;
import org.eea.validation.util.geojsonvalidator.LngLatAlt;
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
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The Class GeoJsonValidationUtils.
 */
@Component
public class GeoJsonValidationUtils {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(GeoJsonValidationUtils.class);

  /** The Constant ERROR_MESSAGE: {@value}. */
  private static final String ERROR_MESSAGE = "This geometry contains more values than expected: ";

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
            result = validatePoint(point);
            break;
          case LINESTRING:
            LineString lineString = new ObjectMapper().readValue(auxGEometry, LineString.class);
            result = validateLinestring(lineString);
            break;
          case POLYGON:
            Polygon polygon = new ObjectMapper().readValue(auxGEometry, Polygon.class);
            result = validatePolygon(polygon);
            break;
          case MULTIPOINT:
            MultiPoint multiPoint = new ObjectMapper().readValue(auxGEometry, MultiPoint.class);
            result = validateMultipoint(multiPoint);
            break;
          case MULTILINESTRING:
            MultiLineString multiLineString =
                new ObjectMapper().readValue(auxGEometry, MultiLineString.class);
            result = validateMultiLineString(multiLineString);
            break;
          case MULTIPOLYGON:
            MultiPolygon multiPolygon =
                new ObjectMapper().readValue(auxGEometry, MultiPolygon.class);
            result = validateMultipolygon(multiPolygon);
            break;
          case GEOMETRYCOLLECTION:
            GeometryCollection geometryCollection =
                new ObjectMapper().readValue(auxGEometry, GeometryCollection.class);
            result = validateGeometryCollection(geometryCollection);
            break;
          default:
            result = "";
        }
      }
      if (result.length() == 0) {
        verifyJson(aux);
      }
    } catch (JsonProcessingException | JSONException | JsonSyntaxException
        | IllegalArgumentException e) {
      LOG.info("This field: {} is not a valid geometry. Reason: {}", fieldValue.getId(),
          e.getMessage());
      if (e.getMessage().contains("VALUE_NULL")) {
        result = String.format("This Field constains null tokens on column: %s value: %s",
            fieldValue.getValue().indexOf("null"), fieldValue.getValue());
      } else {
        result = e.getMessage();
      }
    }
    return result;
  }

  /**
   * Validate point.
   *
   * @param point the point
   * @return the string
   */
  private static String validatePoint(Point point) {
    String result;
    if (point instanceof Point && (!Double.isNaN(point.getCoordinates().getAltitude())
        || point.getCoordinates().getAdditionalElements().length > 0)) {
      StringBuilder errorList = new StringBuilder();
      if (!Double.isNaN(point.getCoordinates().getAltitude())) {
        errorList.append(point.getCoordinates().getAltitude());
      }
      for (double a : point.getCoordinates().getAdditionalElements()) {
        errorList.append(String.valueOf(" " + a));
      }
      result = getError(errorList);
    } else {
      result = "";
    }
    return result;
  }

  /**
   * Validate linestring.
   *
   * @param lineString the line string
   * @return the string
   */
  private static String validateLinestring(LineString lineString) {
    String result;
    if (lineString instanceof LineString && !lineString.getCoordinates().isEmpty()) {
      StringBuilder errorList = new StringBuilder();
      for (LngLatAlt lineStringAux : lineString.getCoordinates()) {
        if (!Double.isNaN(lineStringAux.getAltitude())) {
          errorList.append(lineStringAux.getAltitude());
        }
        for (double a : lineStringAux.getAdditionalElements()) {
          errorList.append(String.valueOf(" " + a));
        }
      }
      result = getError(errorList);
    } else {
      result = "";
    }
    return result;
  }

  /**
   * Validate polygon.
   *
   * @param polygon the polygon
   * @return the string
   */
  private static String validatePolygon(Polygon polygon) {
    String result;
    if (polygon instanceof Polygon && !polygon.getCoordinates().isEmpty()) {
      StringBuilder errorList = new StringBuilder();
      for (List<LngLatAlt> poligonCoordinatesAux : polygon.getCoordinates()) {
        errorList.append(" " + getLngLatAltErrors(poligonCoordinatesAux));
      }
      result = getError(errorList);
    } else {
      result = "";
    }
    return result;
  }

  /**
   * Validate multipoint.
   *
   * @param multiPoint the multi point
   * @return the string
   */
  private static String validateMultipoint(MultiPoint multiPoint) {
    String result;
    if (multiPoint instanceof MultiPoint && !multiPoint.getCoordinates().isEmpty()) {
      StringBuilder errorList = new StringBuilder();
      for (LngLatAlt multiPointAux : multiPoint.getCoordinates()) {
        if (!Double.isNaN(multiPointAux.getAltitude())) {
          errorList.append(multiPointAux.getAltitude());
        }
        for (double a : multiPointAux.getAdditionalElements()) {
          errorList.append(String.valueOf(" " + a));
        }
      }
      result = getError(errorList);
    } else {
      result = "";
    }
    return result;
  }

  /**
   * Validate multi line string.
   *
   * @param multiLineString the multi line string
   * @return the string
   */
  private static String validateMultiLineString(MultiLineString multiLineString) {
    String result;
    if (multiLineString instanceof MultiLineString && !multiLineString.getCoordinates().isEmpty()) {
      StringBuilder errorList = new StringBuilder();
      for (List<LngLatAlt> multiLineStringAux : multiLineString.getCoordinates()) {
        errorList.append(" " + getLngLatAltErrors(multiLineStringAux));
      }
      result = getError(errorList);
    } else {
      result = "";
    }
    return result;
  }

  /**
   * Validate multipolygon.
   *
   * @param multiPolygon the multi polygon
   * @return the string
   */
  private static String validateMultipolygon(MultiPolygon multiPolygon) {
    String result;
    if (multiPolygon instanceof MultiPolygon && !multiPolygon.getCoordinates().isEmpty()) {
      StringBuilder errorList = new StringBuilder();
      for (List<List<LngLatAlt>> multipoligonAux : multiPolygon.getCoordinates()) {
        for (List<LngLatAlt> multipoligonAux2 : multipoligonAux) {
          errorList.append(" " + getLngLatAltErrors(multipoligonAux2));
        }
      }
      result = getError(errorList);
    } else {
      result = "";
    }
    return result;
  }

  /**
   * Validate geometry collection.
   *
   * @param result the result
   * @param geometryCollection the geometry collection
   * @return the string
   * @throws JsonProcessingException the json processing exception
   * @throws JsonMappingException the json mapping exception
   */
  private static String validateGeometryCollection(GeometryCollection geometryCollection)
      throws JsonProcessingException {
    String result;
    if (geometryCollection instanceof GeometryCollection
        && !geometryCollection.getGeometries().isEmpty()) {
      StringBuilder errorList = new StringBuilder();
      for (GeoJsonObject geoJson : geometryCollection.getGeometries()) {
        if (geoJson instanceof Point) {
          errorList
              .append(validatePoint(new ObjectMapper().readValue(geoJson.toString(), Point.class)));
        } else if (geoJson instanceof LineString) {
          errorList.append(validateLinestring(
              new ObjectMapper().readValue(geoJson.toString(), LineString.class)));
        } else if (geoJson instanceof Polygon) {
          errorList.append(
              validatePolygon(new ObjectMapper().readValue(geoJson.toString(), Polygon.class)));
        } else if (geoJson instanceof MultiPoint) {
          errorList.append(validateMultipoint(
              new ObjectMapper().readValue(geoJson.toString(), MultiPoint.class)));
        } else if (geoJson instanceof MultiLineString) {
          errorList.append(validateMultiLineString(
              new ObjectMapper().readValue(geoJson.toString(), MultiLineString.class)));
        } else if (geoJson instanceof MultiPolygon) {
          errorList.append(validateMultipolygon(
              new ObjectMapper().readValue(geoJson.toString(), MultiPolygon.class)));
        }
      }
      result = getError(errorList);
    } else {
      result = "";
    }
    return result;
  }

  /**
   * Gets the error.
   *
   * @param errorList the error list
   * @return the error
   */
  private static String getError(StringBuilder errorList) {
    String result;
    String errorlistAux = errorList.toString();
    if (errorlistAux.trim().length() > 0) {
      result = ERROR_MESSAGE + errorlistAux;
    } else {
      result = "";
    }
    return result;
  }

  /**
   * Gets the lng lat alt errors.
   *
   * @param coordinates the coordinates
   * @return the lng lat alt errors
   */
  private static String getLngLatAltErrors(List<LngLatAlt> coordinates) {
    StringBuilder errorList = new StringBuilder();
    for (LngLatAlt aux : coordinates) {
      if (!Double.isNaN(aux.getAltitude())) {
        errorList.append(aux.getAltitude());
      }
      for (double a : aux.getAdditionalElements()) {
        errorList.append(String.valueOf(" " + a));
      }
    }
    return errorList.toString();
  }

  /**
   * Verify json.
   *
   * @param aux the aux
   */
  private static void verifyJson(String aux) {
    try {
      Gson gson = new Gson();
      gson.fromJson(aux, Object.class);
    } catch (com.google.gson.JsonSyntaxException ex) {
      try {
        String error = ex.getMessage().substring(ex.getMessage().indexOf("column ") + 7,
            ex.getMessage().indexOf("path $"));
        Integer indexOfError = Integer.valueOf(error.trim());
        throw new JsonSyntaxException(aux.substring(indexOfError));
      } catch (NumberFormatException | StringIndexOutOfBoundsException ex2) {
        throw new JsonSyntaxException("Unable to recover the error");
      }
    }
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
