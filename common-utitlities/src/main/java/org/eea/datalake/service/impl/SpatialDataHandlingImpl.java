package org.eea.datalake.service.impl;

import mil.nga.sf.geojson.Feature;
import mil.nga.sf.geojson.FeatureConverter;
import org.eea.datalake.service.SpatialDataHandling;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.stereotype.Component;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.eea.utils.LiteralConstants.PARQUET_PROVIDER_CODE_COLUMN_HEADER;
import static org.eea.utils.LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER;


@Component
public class SpatialDataHandlingImpl implements SpatialDataHandling {
  private List<FieldSchemaVO> headerTypes = new ArrayList<>();

  private void init(TableSchemaVO tableSchemaVO) {
    List<FieldSchemaVO> fieldSchemas = new ArrayList<>();
    FieldSchemaVO recordId = new FieldSchemaVO();
    recordId.setName(PARQUET_RECORD_ID_COLUMN_HEADER);
    FieldSchemaVO providerCode = new FieldSchemaVO();
    providerCode.setName(PARQUET_PROVIDER_CODE_COLUMN_HEADER);
    fieldSchemas.add(recordId);
    fieldSchemas.add(providerCode);
    fieldSchemas.addAll(tableSchemaVO.getRecordSchema().getFieldSchema());

    this.headerTypes = fieldSchemas;
  }

  @Override
  public boolean geoJsonHeadersAreNotEmpty(TableSchemaVO tableSchemaVO, boolean isGeoJsonHeaders) {
    init(tableSchemaVO);
    return !getHeaders(isGeoJsonHeaders).isEmpty();
  }

  @Override
  public StringBuilder getHeadersConvertedToBinary() {
    List<String> geoJsonHeaders = getHeaders(true);

    return new StringBuilder(geoJsonHeaders.stream()
        .map(header -> ", FROM_HEX(" + header + ") as " + header)
        .collect(Collectors.joining()));
  }

  @Override
  public StringBuilder getSimpleHeaders() {
    List<String> geoJsonHeaders = getHeaders(false);

    return new StringBuilder(String.join(",", geoJsonHeaders));
  }

  @Override
  public void decodeSpatialData(List<RecordVO> recordVOS) {
    recordVOS.stream()
        .flatMap(recordVO -> recordVO.getFields().stream())
        .filter(fieldVO -> fieldVO.getByteArrayValue() != null)
        .forEach(fieldVO -> {
          try {
            fieldVO.setValue(decodeSpatialData(fieldVO.getByteArrayValue()));
          } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
          }
        });
  }

  @Override
  public String convertToBinary(String value) {
    GeoJsonReader reader = new GeoJsonReader();
    Geometry geometry;
    try {
      geometry = reader.read(value);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }

    Feature feature = FeatureConverter.toFeature(value);
    String t = feature.getProperties().get("srid").toString();
    if (!t.isBlank()) {
      geometry.setSRID(Integer.parseInt(t));
    }

    byte[] bytes;

    int order = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)
        ? ByteOrderValues.BIG_ENDIAN
        : ByteOrderValues.LITTLE_ENDIAN;

    WKBWriter wkbWriter = new WKBWriter(2, order, true);
    bytes = wkbWriter.write(geometry);

    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02X", b));
    }

    return sb.toString();
  }

  @Override
  public String decodeSpatialData(byte[] byteArray) throws RuntimeException, IOException, ParseException {
    WKBReader r = new WKBReader();

    org.locationtech.jts.geom.Geometry t;
    try {
      t = r.read(byteArray);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
    if (t != null) {
      Map<String, Object> properties = new HashMap<>();
      properties.put("srid", Integer.toString(t.getSRID()));

      org.wololo.jts2geojson.GeoJSONWriter writ = new GeoJSONWriter();
      org.wololo.geojson.Geometry geometry = writ.write(t);

      org.wololo.geojson.Feature feature = new org.wololo.geojson.Feature(geometry, properties);
      return feature.toString();
    }
    return "";
  }

  private List<String> getHeaders(boolean isGeoJsonHeaders) {
    List<DataType> geoJsonEnums = getGeoJsonEnums();

    return headerTypes.stream()
        .filter(header -> isGeoJsonHeaders == geoJsonEnums.contains(header.getType()))
        .map(FieldSchemaVO::getName)
        .collect(Collectors.toList());
  }

  @Override
  public List<DataType> getGeoJsonEnums() {
    List<DataType> geoJsonEnums = new ArrayList<>();
    geoJsonEnums.add(DataType.GEOMETRYCOLLECTION);
    geoJsonEnums.add(DataType.MULTIPOINT);
    geoJsonEnums.add(DataType.MULTIPOLYGON);
    geoJsonEnums.add(DataType.POINT);
    geoJsonEnums.add(DataType.POLYGON);
    geoJsonEnums.add(DataType.LINESTRING);
    geoJsonEnums.add(DataType.MULTILINESTRING);
    return geoJsonEnums;
  }

  @Override
  public DataType getGeometryType(byte[] byteArray) throws ParseException {
    WKBReader reader = new WKBReader();
    Geometry geometry = reader.read(byteArray);
    return DataType.fromValue(geometry.getGeometryType().toUpperCase());
  }

  private Optional<String> getHeader(boolean isGeoJsonHeaders, String headerInput) {
    List<DataType> geoJsonEnums = getGeoJsonEnums();

    return headerTypes.stream()
        .filter(header -> isGeoJsonHeaders == geoJsonEnums.contains(header.getType()))
        .map(FieldSchemaVO::getName)
        .filter(name -> name.equalsIgnoreCase(headerInput)).findAny();
  }

  @Override
  public String fixQueryForSearchData(String inputQuery, boolean isGeoJsonHeaders) {
    String regex = "\\b([a-zA-Z0-9_]+)\\b\\s*(=|!=|>|<|>=|<=|LIKE|IN|IS|BETWEEN)\\s*[^\\s]+";
    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(inputQuery);
    Map<String, String> replacements = new LinkedHashMap<>();

    while (matcher.find()) {
      String columnName = matcher.group(1);
      Optional<String> header = getHeader(isGeoJsonHeaders, columnName);
      if (header.isPresent() && !replacements.containsKey(columnName)) {
        replacements.put(columnName, "st_asgeojson(" + columnName + ")");
      }
    }

    String finalQuery = inputQuery;
    for (Map.Entry<String, String> entry : replacements.entrySet()) {
      finalQuery = finalQuery.replaceAll("\\b" + Pattern.quote(entry.getKey()) + "\\b", entry.getValue());
    }

    return !finalQuery.isEmpty() ? finalQuery : inputQuery;
  }

  @Override
  public String fixQueryForUpdateData(String inputQuery, boolean isGeoJsonHeaders) {
    String regex = "\\b([a-zA-Z0-9_]+)\\b\\s*(=|!=|>|<|>=|<=|LIKE|IN|IS|BETWEEN)\\s*('[^']*')";
    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(inputQuery);

    StringBuilder resultQuery = new StringBuilder(inputQuery);

    while (matcher.find()) {
      String columnName = matcher.group(1);
      String value = matcher.group(3);

      Optional<String> header = getHeader(isGeoJsonHeaders, columnName);
      if (header.isPresent() && header.get().equalsIgnoreCase(DataType.POINT.getValue())) {
        String escValue = escapeJsonString(value);
        String binaryStr = convertToBinary(escValue);
        String hexBinaryStr = "FROM_HEX('" + binaryStr + "')";
        int valueStart = matcher.start(3);
        int valueEnd = matcher.end(3);
        resultQuery.replace(valueStart, valueEnd, hexBinaryStr);
      }
    }

    return !resultQuery.toString().isBlank() ? resultQuery.toString() : inputQuery;
  }

  public static String escapeJsonString(String str) {
    if (str.startsWith("'") && str.endsWith("'")) {
      return str.substring(1, str.length() - 1);
    }
    return str;
  }
}
