package org.eea.dataset.service;

public interface SpatialDataHandling {
  boolean geoJsonHeadersIsNotEmpty(boolean isGeoJsonHeaders);

  StringBuilder getHeadersConvertedToBinary();

  StringBuilder getSimpleHeaders();

  StringBuilder convertToJson();
}
