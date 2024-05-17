package org.eea.datalake.service.impl;

import org.eea.datalake.service.SpatialDataHandling;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.utils.LiteralConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class SpatialDataHandlingImpl implements SpatialDataHandling {
  private final List<FieldSchemaVO> headerTypes;

  public SpatialDataHandlingImpl(TableSchemaVO tableSchemaVO) {
    List<FieldSchemaVO> fieldSchemas = new ArrayList<>();
    FieldSchemaVO recordId = new FieldSchemaVO();
    recordId.setName(LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER);
    FieldSchemaVO providerCode = new FieldSchemaVO();
    providerCode.setName(LiteralConstants.PARQUET_PROVIDER_CODE_COLUMN_HEADER);
    fieldSchemas.add(recordId);
    fieldSchemas.add(providerCode);
    fieldSchemas.addAll(tableSchemaVO.getRecordSchema().getFieldSchema());

    this.headerTypes = fieldSchemas;
  }

  @Override
  public boolean geoJsonHeadersAreNotEmpty(boolean isGeoJsonHeaders) {
    return !getHeaders(isGeoJsonHeaders).isEmpty();
  }

  @Override
  public StringBuilder getHeadersConvertedToBinary() {
    List<String> geoJsonHeaders = getHeaders(true);

    return new StringBuilder(geoJsonHeaders.stream()
        .map(header -> ", ST_AsBinary(ST_GeomFromGeoJson(" + header + ")) as " + header)
        .collect(Collectors.joining()));
  }

  @Override
  public StringBuilder getGeoJsonHeaders() {
    List<String> geoJsonHeaders = getHeaders(true);

    return new StringBuilder(geoJsonHeaders.stream()
        .map(header -> "concat('{\"type\":\"Feature\",\"geometry\":',replace(st_asgeojson(" + header + "),',\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:0\"}}}','},\"properties\": {\"srid\": \"4326\"}}')) as " + header)
        .collect(Collectors.joining(",")));
  }

  @Override
  public StringBuilder getGeoJsonHeader(String fieldName) {
    List<String> geoJsonHeaders = getHeaderByFieldName(fieldName);

    return new StringBuilder(geoJsonHeaders.stream()
        .map(header -> "concat('{\"type\":\"Feature\",\"geometry\":',replace(st_asgeojson(" + header + "),',\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:0\"}}}','},\"properties\": {\"srid\": \"4326\"}}')) as " + header)
        .collect(Collectors.joining(",")));
  }

  @Override
  public StringBuilder getSimpleHeaders() {
    List<String> geoJsonHeaders = getHeaders(false);

    return new StringBuilder(String.join(",", geoJsonHeaders));
  }

  private List<String> getHeaders(boolean isGeoJsonHeaders) {
    List<DataType> geoJsonEnums = getGeoJsonEnums();

    return headerTypes.stream()
        .filter(header -> isGeoJsonHeaders == geoJsonEnums.contains(header.getType()))
        .map(FieldSchemaVO::getName)
        .collect(Collectors.toList());
  }

  private List<String> getHeaderByFieldName(String fieldName) {
    List<DataType> geoJsonEnums = getGeoJsonEnums();

    return headerTypes.stream()
        .filter(header -> geoJsonEnums.contains(header.getType()) && header.getName().equalsIgnoreCase(fieldName))
        .map(FieldSchemaVO::getName)
        .collect(Collectors.toList());
  }

  private static List<DataType> getGeoJsonEnums() {
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
}
