package org.eea.datalake.service;


import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.locationtech.jts.io.ParseException;

import java.io.IOException;
import java.util.List;

public interface SpatialDataHandling {
  boolean geoJsonHeadersAreNotEmpty(TableSchemaVO tableSchemaVO, boolean isGeoJsonHeaders);

  StringBuilder getHeadersConvertedToBinary();

  String convertToBinary(String value);

  StringBuilder getSimpleHeaders();

  void decodeSpatialData(List<RecordVO> recordVOS);

  String decodeSpatialData(byte[] byteArray) throws IOException, ParseException;

  List<DataType> getGeoJsonEnums();

  DataType getGeometryType(byte[] byteArray) throws ParseException;

  String fixQueryForSearchData(String inputQuery, boolean isGeoJsonHeaders);

  StringBuilder fixQueryForUpdateData(String inputQuery, boolean isGeoJsonHeaders);
}
