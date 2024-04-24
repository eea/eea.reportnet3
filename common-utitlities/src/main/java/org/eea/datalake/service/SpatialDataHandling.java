package org.eea.datalake.service;

public interface SpatialDataHandling {
  boolean geoJsonHeadersIsNotEmpty(boolean isGeoJsonHeaders);

  StringBuilder getHeadersConvertedToBinary();

  StringBuilder getSimpleHeaders();

  StringBuilder convertToJson();
}
