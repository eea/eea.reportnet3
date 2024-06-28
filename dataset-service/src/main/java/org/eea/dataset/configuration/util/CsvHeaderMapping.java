package org.eea.dataset.configuration.util;

import lombok.Getter;
import lombok.Setter;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.interfaces.vo.dataset.enums.DataType;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CsvHeaderMapping {
  private final List<FieldSchema> expectedHeaders;
  private final Map<String, DataType> fieldNameAndTypeMap;

  public CsvHeaderMapping(List<FieldSchema> expectedHeaders, Map<String, DataType> fieldNameAndTypeMap) {
    this.expectedHeaders = expectedHeaders;
    this.fieldNameAndTypeMap = fieldNameAndTypeMap;
  }
}
