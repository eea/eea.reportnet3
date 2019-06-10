package org.eea.interfaces.vo.dataset;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RecordValidationVO {

  private Long id;

  private RecordVO recordValue;

  private ValidationVO validation;
}
