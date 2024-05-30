package org.eea.datalake.service.impl;

import mil.nga.sf.geojson.Feature;
import mil.nga.sf.geojson.FeatureConverter;
import org.eea.datalake.service.SpatialDataHandling;
import org.eea.datalake.service.SpatialDataHelper;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.lock.service.impl.LockServiceImpl;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Component
public class SpatialDataHandlingImpl implements SpatialDataHandling {

  private static final Logger LOG = LoggerFactory.getLogger(LockServiceImpl.class);
  private static final String FROM_XEX = "FROM_HEX";
  private final SpatialDataHelper spatialDataHelper;

  public SpatialDataHandlingImpl(SpatialDataHelper spatialDataHelper) {
    this.spatialDataHelper = spatialDataHelper;
  }

  @Override
  public boolean geoJsonHeadersAreNotEmpty(TableSchemaVO tableSchemaVO, boolean isGeoJsonHeaders) {
    return !getHeaders(isGeoJsonHeaders, tableSchemaVO).isEmpty();
  }

  @Override
  public String convertToHEX(String value) {
    try {
      GeoJsonReader reader = new GeoJsonReader();
      Geometry geometry = reader.read(value);
      Feature feature = FeatureConverter.toFeature(value);
      String srid = feature.getProperties().get("srid").toString();
      if (!srid.isBlank()) {
        geometry.setSRID(Integer.parseInt(srid));
      }

      WKBWriter wkbWriter = new WKBWriter(2, true);
      byte[] bytes = wkbWriter.write(geometry);
      return spatialDataHelper.bytesToHex(bytes);
    } catch (ParseException e) {
      LOG.error("Invalid GeoJson!! Tried to convert this String : {} , to binary but failed", value, e);
      return "";
    }
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
  public String decodeSpatialData(byte[] byteArray) throws RuntimeException, IOException, ParseException {
    try {
      WKBReader reader = new WKBReader();
      Geometry geometry = reader.read(byteArray);
      if (geometry != null) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("srid", Integer.toString(geometry.getSRID()));

        GeoJSONWriter writer = new GeoJSONWriter();
        org.wololo.geojson.Geometry geoJsonGeometry = writer.write(geometry);

        org.wololo.geojson.Feature feature = new org.wololo.geojson.Feature(geoJsonGeometry, properties);
        return feature.toString();
      }
    } catch (ParseException e) {
      LOG.error("Invalid GeoJson!! Tried to decode from binary but failed", e);
    }
    return "";
  }

  @Override
  public StringBuilder getHeadersConvertedToBinary(TableSchemaVO tableSchemaVO) {
    List<String> geoJsonHeaders = getHeaders(true, tableSchemaVO);
    StringBuilder result = new StringBuilder();

    for (String header : geoJsonHeaders) {
      result.append(", ").append(FROM_XEX).append("(").append(header).append(") as ").append(header);
    }
    return result;
  }

  @Override
  public StringBuilder getSimpleHeaders(TableSchemaVO tableSchemaVO) {
    List<String> geoJsonHeaders = getHeaders(false, tableSchemaVO);
    return new StringBuilder(String.join(",", geoJsonHeaders));
  }

  private List<String> getHeaders(boolean isGeoJsonHeaders, TableSchemaVO tableSchemaVO) {
    List<DataType> geoJsonEnums = getGeoJsonEnums();

    return spatialDataHelper.getFieldSchemas(tableSchemaVO).stream()
        .filter(header -> isGeoJsonHeaders == geoJsonEnums.contains(header.getType()))
        .map(FieldSchemaVO::getName)
        .collect(Collectors.toList());
  }

  @Override
  public List<DataType> getGeoJsonEnums() {
    return spatialDataHelper.getGeoJsonEnums();
  }

  @Override
  public DataType getGeometryType(byte[] byteArray) throws ParseException {
    WKBReader reader = new WKBReader();
    Geometry geometry = reader.read(byteArray);
    return DataType.fromValue(geometry.getGeometryType().toUpperCase());
  }

  private Optional<String> getHeaderName(boolean isGeoJsonHeaders, String headerInput, TableSchemaVO tableSchemaVO) {
    List<DataType> geoJsonEnums = getGeoJsonEnums();

    return spatialDataHelper.getFieldSchemas(tableSchemaVO).stream()
        .filter(header -> isGeoJsonHeaders == geoJsonEnums.contains(header.getType()))
        .map(FieldSchemaVO::getName)
        .filter(name -> name.equalsIgnoreCase(headerInput)).findAny();
  }

  private Optional<String> getHeaderType(boolean isGeoJsonHeaders, String headerInput, TableSchemaVO tableSchemaVO) {
    List<DataType> geoJsonEnums = getGeoJsonEnums();

    return spatialDataHelper.getFieldSchemas(tableSchemaVO).stream()
        .filter(header -> isGeoJsonHeaders == geoJsonEnums.contains(header.getType()))
        .filter(name -> name.getName().equalsIgnoreCase(headerInput)).findAny().map(fieldSchemaVO -> fieldSchemaVO.getType().getValue());
  }

  @Override
  public String fixQueryIncludeSpatialDataForSearch(String inputQuery, boolean isGeoJsonHeaders, TableSchemaVO tableSchemaVO) {
    String regex = "\\b([a-zA-Z0-9_]+)\\b\\s*(=|!=|>|<|>=|<=|LIKE|IN|IS|BETWEEN)\\s*\\S+";
    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(inputQuery);
    Map<String, String> replacements = new LinkedHashMap<>();

    while (matcher.find()) {
      String columnName = matcher.group(1);
      Optional<String> header = getHeaderName(isGeoJsonHeaders, columnName, tableSchemaVO);
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
  public String fixQueryExcludeSpatialDataFromSearch(String inputQuery, boolean isGeoJsonHeaders, TableSchemaVO tableSchemaVO) {
    String columnRegex = "\\b([a-zA-Z0-9_]+)\\b";
    Pattern columnPattern = Pattern.compile(columnRegex);
    Matcher columnMatcher = columnPattern.matcher(inputQuery);

    Set<String> columnsToExclude = new HashSet<>();

    while (columnMatcher.find()) {
      String columnName = columnMatcher.group(1);
      Optional<String> header = getHeaderName(isGeoJsonHeaders, columnName, tableSchemaVO);
      if (header.isPresent()) {
        columnsToExclude.add(columnName);
      }
    }

    if (columnsToExclude.isEmpty()) {
      return inputQuery;
    }

    String exclusionRegex = "\\b(" + String.join("|", columnsToExclude) + ")\\b\\s*(=|!=|>|<|>=|<=|LIKE|IN|IS|BETWEEN)\\s*\\S+\\s*(AND|OR)?";
    Pattern exclusionPattern = Pattern.compile(exclusionRegex, Pattern.CASE_INSENSITIVE);
    Matcher exclusionMatcher = exclusionPattern.matcher(inputQuery);

    StringBuilder sb = new StringBuilder();
    while (exclusionMatcher.find()) {
      exclusionMatcher.appendReplacement(sb, "");
    }
    exclusionMatcher.appendTail(sb);

    StringBuilder finalQuery = new StringBuilder(sb.toString().replaceAll("\\s*(AND|OR)\\s*$", "").replaceAll("\\s*(AND|OR)\\s*(\\))", " $2"));

    int openParens = spatialDataHelper.countOccurrences(finalQuery.toString(), '(');
    int closeParens = spatialDataHelper.countOccurrences(finalQuery.toString(), ')');
    while (openParens > closeParens) {
      finalQuery.append(")");
      closeParens++;
    }

    return (finalQuery.length() == 0) ? inputQuery : finalQuery.toString();
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
        String binaryStr = convertToHEX(escValue);
        String hexBinaryStr = FROM_XEX + "('" + binaryStr + "')";
        int valueStart = matcher.start(3);
        int valueEnd = matcher.end(3);
        resultQuery.replace(valueStart, valueEnd, hexBinaryStr);
      }
    }
    return resultQuery;
  }
}
