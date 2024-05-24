package org.eea.datalake.service.impl;

import mil.nga.sf.geojson.Feature;
import mil.nga.sf.geojson.FeatureConverter;
import org.eea.datalake.service.SpatialDataHandling;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.lock.service.impl.LockServiceImpl;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger LOG = LoggerFactory.getLogger(LockServiceImpl.class);
  private static final String FROM_XEX = "FROM_HEX";

  private List<FieldSchemaVO> init(TableSchemaVO tableSchemaVO) {
    List<FieldSchemaVO> fieldSchemas = new ArrayList<>();
    FieldSchemaVO recordId = new FieldSchemaVO();
    recordId.setName(PARQUET_RECORD_ID_COLUMN_HEADER);
    FieldSchemaVO providerCode = new FieldSchemaVO();
    providerCode.setName(PARQUET_PROVIDER_CODE_COLUMN_HEADER);
    fieldSchemas.add(recordId);
    fieldSchemas.add(providerCode);
    fieldSchemas.addAll(tableSchemaVO.getRecordSchema().getFieldSchema());

    return fieldSchemas;
  }

  @Override
  public boolean geoJsonHeadersAreNotEmpty(TableSchemaVO tableSchemaVO, boolean isGeoJsonHeaders) {
    return !getHeaders(isGeoJsonHeaders, tableSchemaVO).isEmpty();
  }

  @Override
  public StringBuilder getHeadersConvertedToBinary(TableSchemaVO tableSchemaVO) {
    List<String> geoJsonHeaders = getHeaders(true, tableSchemaVO);

    return new StringBuilder(geoJsonHeaders.stream()
        .map(header -> ", " + FROM_XEX + "(" + header + ") as " + header)
        .collect(Collectors.joining()));
  }

  @Override
  public StringBuilder getSimpleHeaders(TableSchemaVO tableSchemaVO) {
    List<String> geoJsonHeaders = getHeaders(false, tableSchemaVO);
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
            LOG.error("Invalid GeoJson!! Tried to decode from binary but failed", e);
          }
        });
  }

  @Override
  public String convertToBinary(String value) {
    StringBuilder sb = new StringBuilder();
    try {
      GeoJsonReader reader = new GeoJsonReader();
      Geometry geometry;
      geometry = reader.read(value);
      Feature feature = FeatureConverter.toFeature(value);
      String srid = feature.getProperties().get("srid").toString();
      if (!srid.isBlank()) {
        geometry.setSRID(Integer.parseInt(srid));
      }

      int order = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)
          ? ByteOrderValues.BIG_ENDIAN
          : ByteOrderValues.LITTLE_ENDIAN;

      WKBWriter wkbWriter = new WKBWriter(2, order, true);
      byte[] bytes = wkbWriter.write(geometry);
      for (byte b : bytes) {
        sb.append(String.format("%02X", b));
      }
    } catch (ParseException e) {
      LOG.error("Invalid GeoJson!! Tried to convert this String : {} , to binary but failed", value, e);
    }
    return sb.toString();
  }

  @Override
  public String decodeSpatialData(byte[] byteArray) throws RuntimeException, IOException, ParseException {
    try {
      WKBReader r = new WKBReader();
      org.locationtech.jts.geom.Geometry t = r.read(byteArray);
      if (t != null) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("srid", Integer.toString(t.getSRID()));

        org.wololo.jts2geojson.GeoJSONWriter writ = new GeoJSONWriter();
        org.wololo.geojson.Geometry geometry = writ.write(t);

        org.wololo.geojson.Feature feature = new org.wololo.geojson.Feature(geometry, properties);
        return feature.toString();
      }
    } catch (ParseException e) {
      LOG.error("Invalid GeoJson!! Tried to decode from binary but failed", e);
    }
    return "";
  }

  private List<String> getHeaders(boolean isGeoJsonHeaders, TableSchemaVO tableSchemaVO) {
    List<DataType> geoJsonEnums = getGeoJsonEnums();

    return init(tableSchemaVO).stream()
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

  private Optional<String> getHeader(boolean isGeoJsonHeaders, String headerInput, TableSchemaVO tableSchemaVO) {
    List<DataType> geoJsonEnums = getGeoJsonEnums();

    return init(tableSchemaVO).stream()
        .filter(header -> isGeoJsonHeaders == geoJsonEnums.contains(header.getType()))
        .map(FieldSchemaVO::getName)
        .filter(name -> name.equalsIgnoreCase(headerInput)).findAny();
  }

  @Override
  public String fixQueryForSearchSpatialData(String inputQuery, boolean isGeoJsonHeaders, TableSchemaVO tableSchemaVO) {
    String regex = "\\b([a-zA-Z0-9_]+)\\b\\s*(=|!=|>|<|>=|<=|LIKE|IN|IS|BETWEEN)\\s*\\S+";
    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(inputQuery);
    Map<String, String> replacements = new LinkedHashMap<>();

    while (matcher.find()) {
      String columnName = matcher.group(1);
      Optional<String> header = getHeader(isGeoJsonHeaders, columnName, tableSchemaVO);
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
  public StringBuilder fixQueryForUpdateSpatialData(String inputQuery, boolean isGeoJsonHeaders, TableSchemaVO tableSchemaVO) {
    String regex = "\\b([a-zA-Z0-9_]+)\\b\\s*(=|!=|>|<|>=|<=|LIKE|IN|IS|BETWEEN)\\s*('[^']*')";
    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(inputQuery);

    StringBuilder resultQuery = new StringBuilder(inputQuery);

    while (matcher.find()) {
      String columnName = matcher.group(1);
      String value = matcher.group(3);

      Optional<String> header = getHeader(isGeoJsonHeaders, columnName, tableSchemaVO);
      if (header.isPresent() && getGeoJsonEnums().contains(DataType.valueOf(header.get().toUpperCase()))) {
        String escValue = escapeJsonString(value);
        LOG.info("Value before converted to binary: {} ", escValue);
        String binaryStr = convertToBinary(escValue);
        String hexBinaryStr = FROM_XEX + "('" + binaryStr + "')";
        int valueStart = matcher.start(3);
        int valueEnd = matcher.end(3);
        resultQuery.replace(valueStart, valueEnd, hexBinaryStr);
      }
    }
    return resultQuery;
  }

  public static String escapeJsonString(String str) {
    if (str.startsWith("'") && str.endsWith("'")) {
      return str.substring(1, str.length() - 1);
    }
    return str;
  }
}
