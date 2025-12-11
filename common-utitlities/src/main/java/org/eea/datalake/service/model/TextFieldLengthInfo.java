package org.eea.datalake.service.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TextFieldLengthInfo {
  private String fieldName;
  private String tableName;
  private List<Long> recordLines = new ArrayList<>();
}
