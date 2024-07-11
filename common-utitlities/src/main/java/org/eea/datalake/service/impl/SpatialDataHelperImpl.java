package org.eea.datalake.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eea.datalake.service.SpatialDataHelper;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.eea.utils.LiteralConstants.PARQUET_PROVIDER_CODE_COLUMN_HEADER;
import static org.eea.utils.LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER;

@Component
public class SpatialDataHelperImpl implements SpatialDataHelper {
  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
  private static final String SRIDPath = "/properties/srid";

  @Override
  public int countOccurrences(String str, char ch) {
    int count = 0;
    for (char c : str.toCharArray()) {
      if (c == ch) {
        count++;
      }
    }
    return count;
  }

  @Override
  public String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }

  @Override
  public String escapeJsonString(String str) {
    if (str.startsWith("'") && str.endsWith("'")) {
      return str.substring(1, str.length() - 1);
    }
    return str;
  }

  @Override
  public List<FieldSchemaVO> getFieldSchemas(TableSchemaVO tableSchemaVO) {
    List<FieldSchemaVO> fieldSchemas = new ArrayList<>();
    FieldSchemaVO recordId = new FieldSchemaVO();
    recordId.setName(PARQUET_RECORD_ID_COLUMN_HEADER);
    FieldSchemaVO providerCode = new FieldSchemaVO();
    providerCode.setName(PARQUET_PROVIDER_CODE_COLUMN_HEADER);
    fieldSchemas.add(recordId);
    fieldSchemas.add(providerCode);
    fieldSchemas.addAll(tableSchemaVO.getRecordSchema().getFieldSchema());

    return fieldSchemas;
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

  public String extractSRID(String geoJson) throws IOException {
    JsonNode SRIDNode = new ObjectMapper().readTree(geoJson).at(SRIDPath);
    return SRIDNode.isTextual() ? SRIDNode.asText() : "";
  }

  @Override
  public boolean coordinatesAreNotEmpty(String value) {
    JSONObject geoJson = new JSONObject(value);
    JSONObject geometry = geoJson.getJSONObject("geometry");
    JSONArray coordinates = geometry.getJSONArray("coordinates");
    return !coordinates.isEmpty();
  }
}
