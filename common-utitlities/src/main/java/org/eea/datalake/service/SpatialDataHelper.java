package org.eea.datalake.service;

import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;

import java.io.IOException;
import java.util.List;

public interface SpatialDataHelper {
  int countOccurrences(String str, char ch);
  String bytesToHex(byte[] bytes);
  String escapeJsonString(String str);
  List<FieldSchemaVO> getFieldSchemas(TableSchemaVO tableSchemaVO);
  List<DataType> getGeoJsonEnums();
  String extractSRID(String value) throws IOException;
}
