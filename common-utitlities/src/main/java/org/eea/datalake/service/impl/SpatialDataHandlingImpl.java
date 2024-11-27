package org.eea.datalake.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.eea.datalake.service.SpatialDataHandling;
import org.eea.datalake.service.SpatialDataHelper;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.wololo.geojson.Feature;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Component
public class SpatialDataHandlingImpl implements SpatialDataHandling {

  private static final Logger LOG = LoggerFactory.getLogger(SpatialDataHandlingImpl.class);
  private static final String FROM_XEX = "FROM_HEX";

  private final SpatialDataHelper spatialDataHelper;
  private final WKBReader wkbReader;
  private final GeoJsonReader geoJsonReader;
  private final GeoJSONWriter geoJSONWriter;

  public SpatialDataHandlingImpl(SpatialDataHelper spatialDataHelper) {
    this.spatialDataHelper = spatialDataHelper;
    this.wkbReader = new WKBReader();
    this.geoJsonReader = new GeoJsonReader();
    this.geoJSONWriter = new GeoJSONWriter();
  }

  @Override
  public boolean geoJsonHeadersAreNotEmpty(TableSchemaVO tableSchemaVO) {
    return !getHeaders(true, tableSchemaVO).isEmpty();
  }

  @Override
  public String convertToHEX(String value) {
    try {
      if (!value.isBlank()) {
        Geometry geometry = geoJsonReader.read(value);
        String srid = spatialDataHelper.extractSRID(value);
        if (!srid.isBlank()) {
          geometry.setSRID(Integer.parseInt(srid));
        }
        value = "";
        byte[] geomByteArray = new WKBWriter(2, true).write(geometry);
        geometry = null;
        String HexString = spatialDataHelper.bytesToHex(geomByteArray);
        geomByteArray = null;
        return HexString;
      }
    } catch (ParseException | IOException e) {
      LOG.error("Invalid GeoJson!! Tried to convert the geoJson , to HEX but failed, with message: {}", e.getMessage());
    }
    return spatialDataHelper.bytesToHex(new byte[0]);
  }

  @Override
  public void decodeSpatialData(List<RecordVO> recordVOS) {
    recordVOS.stream()
        .flatMap(recordVO -> recordVO.getFields().stream())
        .filter(fieldVO -> fieldVO.getByteArrayValue() != null && fieldVO.getByteArrayValue().length > 0)
        .forEach(fieldVO -> {
          try {
            fieldVO.setValue(decodeSpatialData(fieldVO.getByteArrayValue()));
          } catch (IOException | ParseException e) {
            LOG.error("Invalid byteArray!! Tried to decode from binary but failed, with message: {}", e.getMessage());
          }
        });
  }

  @Override
  public String decodeSpatialData(byte[] byteArray) throws RuntimeException, IOException, ParseException {
    try {
      if (byteArray.length > 0) {
        WKBReader reader = new WKBReader();
        Geometry geometry = reader.read(byteArray);
        if (geometry != null) {
          Map<String, Object> properties = new HashMap<>();
          properties.put("srid", Integer.toString(geometry.getSRID()));

          org.wololo.geojson.Geometry geoJsonGeometry = geoJSONWriter.write(geometry);
          Feature feature = new Feature(geoJsonGeometry, properties);

          return feature.toString();
        }
      }
    } catch (ParseException e) {
      LOG.error("Invalid byteArray!! Tried to decode from binary but failed, with message: {}", e.getMessage());
    }
    return "";
  }

  public StringBuilder getHeaders(TableSchemaVO tableSchemaVO) {
    List<DataType> geoJsonEnums = getGeoJsonEnums();
    StringBuilder result = new StringBuilder();
    boolean firstColumn = true;

    for (FieldSchemaVO fieldSchemaVO : spatialDataHelper.getFieldSchemas(tableSchemaVO)) {
      if (firstColumn) {
        firstColumn = false;
      } else {
        result.append(", ");
      }

      if (geoJsonEnums.contains(fieldSchemaVO.getType())) {
        result.append(FROM_XEX).append("(").append(fieldSchemaVO.getName()).append(") as ").append(fieldSchemaVO.getName());
      } else {
        result.append(fieldSchemaVO.getName());
      }
    }
    return result;
  }

  private List<String> getHeaders(boolean includeGeoJsonHeaders, TableSchemaVO tableSchemaVO) {
    List<DataType> geoJsonEnums = getGeoJsonEnums();

    return spatialDataHelper.getFieldSchemas(tableSchemaVO).stream()
        .filter(header -> includeGeoJsonHeaders == geoJsonEnums.contains(header.getType()))
        .map(FieldSchemaVO::getName)
        .collect(Collectors.toList());
  }

  @Override
  public List<DataType> getGeoJsonEnums() {
    return spatialDataHelper.getGeoJsonEnums();
  }

  @Override
  public DataType getGeometryType(byte[] byteArray) throws ParseException {
    if (byteArray.length > 0) {
      Geometry geometry = wkbReader.read(byteArray);
      return DataType.fromValue(geometry.getGeometryType().toUpperCase());
    }
    return DataType.fromValue("");
  }

  private Optional<String> getHeaderType(boolean isGeoJsonHeaders, String headerInput, TableSchemaVO tableSchemaVO) {
    List<DataType> geoJsonEnums = getGeoJsonEnums();

    return spatialDataHelper.getFieldSchemas(tableSchemaVO).stream()
        .filter(header -> isGeoJsonHeaders == geoJsonEnums.contains(header.getType()))
        .filter(name -> name.getName().equalsIgnoreCase(headerInput)).findAny().map(fieldSchemaVO -> fieldSchemaVO.getType().getValue());
  }

  @Override
  public StringBuilder fixQueryForUpdateSpatialData(String inputQuery, boolean isGeoJsonHeaders, TableSchemaVO tableSchemaVO) {
    String regex = "\\b([a-zA-Z0-9_]+)\\b\\s*(=|!=|>|<|>=|<=|LIKE|IN|IS|BETWEEN)\\s*('[^']*')";
    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(inputQuery);

    StringBuilder resultQuery = new StringBuilder(inputQuery);

    while (matcher.find()) {
      String columnName = matcher.group(1);
      String value = matcher.group(3);

      Optional<String> header = getHeaderType(isGeoJsonHeaders, columnName, tableSchemaVO);
      if (header.isPresent() && getGeoJsonEnums().contains(DataType.valueOf(header.get().toUpperCase()))) {
        String escValue = spatialDataHelper.escapeJsonString(value);
        if (StringUtils.isNotBlank(escValue) && spatialDataHelper.coordinatesAreNotEmpty(escValue)) {
          String hexStr = convertToHEX(escValue);
          String binaryStr = FROM_XEX + "('" + hexStr + "')";
          int valueStart = matcher.start(3);
          int valueEnd = matcher.end(3);
          resultQuery.replace(valueStart, valueEnd, binaryStr);
        }
      }
    }
    return resultQuery;
  }

  @Override
  public String refactorQuery(String geoJsonValue) {
    if (!geoJsonValue.isEmpty() ) {
      String escValue = spatialDataHelper.escapeJsonString(geoJsonValue);
      if (StringUtils.isNotBlank(escValue) && spatialDataHelper.coordinatesAreNotEmpty(escValue)) {
        String hexStr = convertToHEX(escValue);
        return FROM_XEX + "('" + hexStr + "')";
      }
    }
    return "''";
  }

}
