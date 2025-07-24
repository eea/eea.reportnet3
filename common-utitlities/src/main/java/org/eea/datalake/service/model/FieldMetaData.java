package org.eea.datalake.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FieldMetaData {
  private long recordLine;
  private boolean fieldHasExceededSize;
}
