package org.eea.datalake.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.eea.datalake.service.SpatialDataHandling;
import org.eea.datalake.service.SpatialDataHelper;
import org.eea.datalake.service.model.SpatialDataDescriptor;
import org.eea.datalake.service.model.SpatialFieldInfo;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.utils.UtilityClass;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.wololo.geojson.Feature;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Component
public class SpatialDataHandlingImpl implements SpatialDataHandling {

  /**
   * Max field size in MB
   */
  @Value(value = "${maximum.spatial.field.size}")
  private Long maximumSpatialFieldSize;

  private static final Logger LOG = LoggerFactory.getLogger(SpatialDataHandlingImpl.class);
  private static final String FROM_XEX = "FROM_HEX";

  private final SpatialDataHelper spatialDataHelper;
  private final WKBReader wkbReader;
  private final GeoJsonReader geoJsonReader;
  private final GeoJSONWriter geoJSONWriter;
  private final ObjectMapper objectMapper;

  public SpatialDataHandlingImpl(SpatialDataHelper spatialDataHelper) {
    this.spatialDataHelper = spatialDataHelper;
    this.wkbReader = new WKBReader();
    this.geoJsonReader = new GeoJsonReader();
    this.geoJSONWriter = new GeoJSONWriter();
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public boolean geoJsonHeadersAreNotEmpty(TableSchemaVO tableSchemaVO) {
    return !getHeaders(true, tableSchemaVO).isEmpty();
  }

  @Override
  public String convertToHEX(String value, long lineNumber, SpatialFieldInfo spatialFieldInfo, String headerName) {
    try {
      if (!value.isBlank() && spatialDataHelper.isValidJSON(value)) {
        Geometry geometry = geoJsonReader.read(value);
        String srid = spatialDataHelper.extractSRID(value);
        if (!srid.isBlank()) {
          geometry.setSRID(Integer.parseInt(srid));
        }
        value = "";
        byte[] geomByteArray = new WKBWriter(2, true).write(geometry);
        geometry = null;
        String HexString = spatialDataHelper.bytesToHex(geomByteArray);
        if (spatialFieldInfo != null && fieldExceedsMaxSize(lineNumber, spatialFieldInfo, geomByteArray)) {
          spatialFieldInfo.setFieldName(headerName);
          return "";
        }
        geomByteArray = null;
        return HexString;
      }
    } catch (ParseException | IOException e) {
      LOG.error("SpatialDataHandlingImpl.convertToHEX() Invalid GeoJson!! Tried to convert the geoJson , to HEX but failed at line {}, with message: {}", lineNumber, e.getMessage());
    }
    return spatialDataHelper.bytesToHex(new byte[0]);
  }

  @Override
  public void decodeSpatialData(List<RecordVO> recordVOS) {
    recordVOS.stream()
        .flatMap(recordVO -> recordVO.getFields().stream())
        .filter(fieldVO -> fieldVO.getByteArrayValue() != null && fieldVO.getByteArrayValue().length > 0)
        .forEach(fieldVO -> {
          try {
            fieldVO.setValue(decodeSpatialData(fieldVO.getByteArrayValue()));
          } catch (IOException | ParseException e) {
            LOG.error("SpatialDataHandlingImpl.decodeSpatialData() Invalid byteArray!! Tried to decode from binary but failed, with message: {}", e.getMessage());
          }
        });
  }

  /**
   * Iterate to all spatial data and replace them with enums
   * Spatial Data are heavy for front-end and we had to strip them out of the initial table payload based on #299362
   *
   * @param recordVOS
   */
  @Override
  public void stripSpatialData(List<RecordVO> recordVOS) {
    recordVOS.stream()
        .flatMap(recordVO -> recordVO.getFields().stream())
        .filter(fieldVO -> fieldVO.getByteArrayValue() != null && fieldVO.getByteArrayValue().length > 0)
        .forEach(fieldVO -> {
            fieldVO.setValue(stripSpatialData(fieldVO.getByteArrayValue()));
        });
  }

  @Override
  public String decodeSpatialData(byte[] byteArray) throws RuntimeException, IOException, ParseException {
    try {
      if (byteArray.length > 0) {
        WKBReader reader = new WKBReader();
        Geometry geometry = reader.read(byteArray);
        if (geometry != null) {
          Map<String, Object> properties = new HashMap<>();
          properties.put("srid", Integer.toString(geometry.getSRID()));

          org.wololo.geojson.Geometry geoJsonGeometry = geoJSONWriter.write(geometry);
          Feature feature = new Feature(geoJsonGeometry, properties);

          return feature.toString();
        }
      }
    } catch (ParseException e) {
      LOG.error("SpatialDataHandlingImpl.decodeSpatialData() Invalid byteArray!! Tried to decode from binary but failed, with message: {}", e.getMessage());
    }
    return "";
  }

  @Override
  public String stripSpatialData(byte[] byteArray) {
    final String EMPTY_JSON = "{}";

    final int MIN_WKB_SIZE = 5;
    final int SRID_OFFSET = 5;

    final int TYPE_MASK = 0xFF;

    final int FLAG_Z = 0x80000000;
    final int FLAG_M = 0x40000000;
    final int FLAG_SRID = 0x20000000;

    final double BYTES_IN_MB = 1024.0 * 1024.0;
    final int INTEGER_BYTES = 4;

    if (byteArray == null || byteArray.length < MIN_WKB_SIZE) {
      return EMPTY_JSON;
    }

    final int sizeBytes = byteArray.length;
    final double sizeMB = sizeBytes / BYTES_IN_MB;

    try {

      final ByteBuffer buffer = (byteArray[0] == 0)
              ? ByteBuffer.wrap(byteArray).order(ByteOrder.BIG_ENDIAN)
              : ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN);

      final int typeInt = buffer.getInt(1);

      // EWKB flag extraction
      final boolean hasZ = (typeInt & FLAG_Z) != 0;
      final boolean hasM = (typeInt & FLAG_M) != 0;
      final boolean hasSRID = (typeInt & FLAG_SRID) != 0;

      final int baseType = typeInt & TYPE_MASK;

      final String type = mapGeometryType(baseType);
      final String dimension = resolveDimension(hasZ, hasM);

      Integer srid = null;

      if (hasSRID && byteArray.length >= SRID_OFFSET + INTEGER_BYTES) {
        srid = buffer.getInt(SRID_OFFSET);
      }

      return buildJson(srid, type, sizeMB, dimension);

    } catch (Exception e) {
      LOG.error("Failed to extract spatial metadata. sizeBytes={}", sizeBytes, e);
      return buildJson(null, null, sizeMB, null);
    }
  }

  /**
   * Builds lightweight JSON descriptor.
   */
  private String buildJson(Integer srid, String type, Double sizeMB, String dimension) {

    SpatialDataDescriptor descriptor = new SpatialDataDescriptor();
    descriptor.setSrid(srid);
    descriptor.setType(type != null ? type.toUpperCase() : null);
    descriptor.setSizeMB(sizeMB);
    descriptor.setDimension(dimension);

    try {
      return objectMapper.writeValueAsString(descriptor);
    } catch (Exception e) {
      LOG.error("Failed to serialize spatial descriptor JSON", e);
      return "{}";
    }
  }

  /**
   * Maps WKB base geometry types.
   */
  private String mapGeometryType(int type) {
    switch (type) {
      case 1: return DataType.POINT.getValue();
      case 2: return DataType.LINESTRING.getValue();
      case 3: return DataType.POLYGON.getValue();
      case 4: return DataType.MULTIPOINT.getValue();
      case 5: return DataType.MULTILINESTRING.getValue();
      case 6: return DataType.MULTIPOLYGON.getValue();
      case 7: return "GEOMETRYCOLLECTION";
      default: return "UNKNOWN";
    }
  }


  /**
   * Resolves dimensionality from EWKB flags.
   */
  private String resolveDimension(boolean hasZ, boolean hasM) {
    if (hasZ && hasM) return "4D";
    if (hasZ) return "3D";
    if (hasM) return "2D+M";
    return "2D";
  }
  public StringBuilder getHeaders(TableSchemaVO tableSchemaVO) {
    List<DataType> geoJsonEnums = getGeoJsonEnums();
    StringBuilder result = new StringBuilder();
    boolean firstColumn = true;

    for (FieldSchemaVO fieldSchemaVO : spatialDataHelper.getFieldSchemas(tableSchemaVO)) {
      if (firstColumn) {
        firstColumn = false;
      } else {
        result.append(", ");
      }

      String fieldName = UtilityClass.addQuotesToFieldNames(fieldSchemaVO.getName());

      if (geoJsonEnums.contains(fieldSchemaVO.getType())) {
        result.append(FROM_XEX).append("(").append(fieldName).append(") AS ").append(fieldName);
      } else {
        result.append(fieldName);
      }
    }
    return result;
  }


  private List<String> getHeaders(boolean includeGeoJsonHeaders, TableSchemaVO tableSchemaVO) {
    List<DataType> geoJsonEnums = getGeoJsonEnums();

    return spatialDataHelper.getFieldSchemas(tableSchemaVO).stream()
        .filter(header -> includeGeoJsonHeaders == geoJsonEnums.contains(header.getType()))
        .map(FieldSchemaVO::getName)
        .collect(Collectors.toList());
  }

  @Override
  public List<DataType> getGeoJsonEnums() {
    return spatialDataHelper.getGeoJsonEnums();
  }

  @Override
  public DataType getGeometryType(byte[] byteArray) throws ParseException {
    if (byteArray.length > 0) {
      Geometry geometry = wkbReader.read(byteArray);
      return DataType.fromValue(geometry.getGeometryType().toUpperCase());
    }
    return DataType.fromValue("");
  }

  private Optional<String> getHeaderType(boolean isGeoJsonHeaders, String headerInput, TableSchemaVO tableSchemaVO) {
    List<DataType> geoJsonEnums = getGeoJsonEnums();

    return spatialDataHelper.getFieldSchemas(tableSchemaVO).stream()
        .filter(header -> isGeoJsonHeaders == geoJsonEnums.contains(header.getType()))
        .filter(name -> name.getName().equalsIgnoreCase(headerInput))
        .findAny()
        .map(fieldSchemaVO -> fieldSchemaVO.getType().getValue());
  }

  @Override
  public StringBuilder fixQueryForUpdateSpatialData(String inputQuery, boolean isGeoJsonHeaders, TableSchemaVO tableSchemaVO, long lineNumber) {
    String regex = "(\"[^\"]+\"|[a-zA-Z0-9_]+)\\s*(=|!=|>|<|>=|<=|LIKE|IN|IS|BETWEEN)\\s*('[^']*')";
    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(inputQuery);

    StringBuilder resultQuery = new StringBuilder(inputQuery);

    while (matcher.find()) {
      String columnName = spatialDataHelper.unEscapeJsonString(matcher.group(1));
      String value = matcher.group(3);

      Optional<String> header = getHeaderType(isGeoJsonHeaders, columnName, tableSchemaVO);
      if (header.isPresent() && getGeoJsonEnums().contains(DataType.valueOf(header.get().toUpperCase()))) {
        String escValue = spatialDataHelper.escapeJsonString(value);
        if (StringUtils.isNotBlank(escValue) && spatialDataHelper.coordinatesAreNotEmpty(escValue)) {
          String hexStr = convertToHEX(escValue, lineNumber, null, null);
          String binaryStr = FROM_XEX + "('" + hexStr + "')";
          int valueStart = matcher.start(3);
          int valueEnd = matcher.end(3);
          resultQuery.replace(valueStart, valueEnd, binaryStr);
        }
      }
    }
    return resultQuery;
  }

  @Override
  public String refactorQuery(String geoJsonValue, long lineNumber) {
    if (!geoJsonValue.isEmpty() ) {
      String escValue = spatialDataHelper.escapeJsonString(geoJsonValue);
      if (StringUtils.isNotBlank(escValue) && spatialDataHelper.coordinatesAreNotEmpty(escValue)) {
        String hexStr = convertToHEX(escValue, lineNumber, null, null);
        return FROM_XEX + "('" + hexStr + "')";
      }
    }
    return "''";
  }

  /**
   * Checks if the field of spatial data size exceeds consul property
   *
   * @param lineNumber The line number of record
   * @param spatialFieldInfo The list of field metadata
   * @param geomByteArray The WKB to calculate
   */
  private boolean fieldExceedsMaxSize(long lineNumber, SpatialFieldInfo spatialFieldInfo, byte[] geomByteArray) {
    if (UtilityClass.spatialFieldExceedsMaxSize(geomByteArray, maximumSpatialFieldSize)) {
      List<Long> rl = spatialFieldInfo.getRecordLines();
      rl.add(++lineNumber);
      spatialFieldInfo.setRecordLines(rl);
      return true;
    }
    return false;
  }

}
