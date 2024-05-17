package org.eea.dataset.service.impl;

import org.eea.dataset.service.SpatialDataHandling;
import org.eea.interfaces.vo.dataset.enums.DataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class SpatialDataHandlingImpl implements SpatialDataHandling {
  private final List<String> csvHeaders;

  public SpatialDataHandlingImpl(List<String> csvHeaders) {
    this.csvHeaders = csvHeaders;
  }

  @Override
  public boolean geoJsonHeadersIsNotEmpty(boolean isGeoJsonHeaders) {
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
  public StringBuilder convertToJson() {
    List<String> geoJsonHeaders = getHeaders(true);

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
    List<String> geoJsonEnums = new ArrayList<>();
    geoJsonEnums.add(DataType.GEOMETRYCOLLECTION.getValue());
    geoJsonEnums.add(DataType.MULTIPOINT.getValue());
    geoJsonEnums.add(DataType.MULTIPOLYGON.getValue());
    geoJsonEnums.add(DataType.POINT.getValue());
    geoJsonEnums.add(DataType.POLYGON.getValue());
    geoJsonEnums.add(DataType.LINESTRING.getValue());
    geoJsonEnums.add(DataType.MULTILINESTRING.getValue());

    return csvHeaders.stream()
        .filter(header -> isGeoJsonHeaders == geoJsonEnums.contains(header.toUpperCase()))
        .collect(Collectors.toList());
  }
}
