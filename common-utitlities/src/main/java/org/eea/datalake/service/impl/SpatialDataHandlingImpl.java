package org.eea.datalake.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eea.datalake.service.SpatialDataHandling;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
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
        .map(header -> ", St_asEwkb(ST_Transform(ST_GeomFromGeoJson(" + header + "), CAST(REPLACE(REGEXP_EXTRACT(" + header + ", '\"[0-9]{4}\"', 0 ), '\"', '') AS integer))) as " + header)
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
  public String decodeSpatialData(byte[] byteArray) throws RuntimeException, IOException, ParseException {
    WKBReader reader = new WKBReader();
    Geometry geometry = reader.read(byteArray);

    GeoJsonWriter geoJsonWriter = new GeoJsonWriter();
    String input = geoJsonWriter.write(geometry);

    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(input);

    String type = root.path("type").asText();
    JsonNode coordinates = root.path("coordinates");
    String srid = root.path("crs").path("properties").path("name").asText().replace("EPSG:", "");

    ObjectNode geometryNode = mapper.createObjectNode();
    geometryNode.put("type", type);
    geometryNode.set("coordinates", coordinates);

    ObjectNode propertiesNode = mapper.createObjectNode();
    propertiesNode.put("srid", srid);

    ObjectNode featureNode = mapper.createObjectNode();
    featureNode.put("type", "Feature");
    featureNode.set("geometry", geometryNode);
    featureNode.set("properties", propertiesNode);

    return mapper.writeValueAsString(featureNode);
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
  public String fixQueryForSpatialData(String inputQuery, boolean isGeoJsonHeaders) {
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
}
