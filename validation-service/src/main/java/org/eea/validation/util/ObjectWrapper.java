package org.eea.validation.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ObjectWrapper {
  private String message;
  private String recordId;
}
