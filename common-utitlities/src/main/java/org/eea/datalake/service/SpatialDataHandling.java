package org.eea.datalake.service;


import org.eea.datalake.service.model.FieldMetaData;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.locationtech.jts.io.ParseException;

import java.io.IOException;
import java.util.List;

public interface SpatialDataHandling {
  boolean geoJsonHeadersAreNotEmpty(TableSchemaVO tableSchemaVO);

  String convertToHEX(String value, long lineNumber, List<FieldMetaData> listOfFieldMetaData);

  StringBuilder getHeaders(TableSchemaVO tableSchemaVO);

  void decodeSpatialData(List<RecordVO> recordVOS);

  String decodeSpatialData(byte[] byteArray) throws IOException, ParseException;

  List<DataType> getGeoJsonEnums();

  DataType getGeometryType(byte[] byteArray) throws ParseException;

  StringBuilder fixQueryForUpdateSpatialData(String inputQuery, boolean isGeoJsonHeaders, TableSchemaVO tableSchemaVO, long lineNumber);

  String refactorQuery(String geoJsonValue, long lineNumber);
}
