package org.eea.datalake.service;

public interface SpatialDataHandling {
  boolean geoJsonHeadersAreNotEmpty(boolean isGeoJsonHeaders);

  StringBuilder getHeadersConvertedToBinary();

  StringBuilder getSimpleHeaders();

  StringBuilder getGeoJsonHeaders();

  StringBuilder getGeoJsonHeader(String fieldName);
}
