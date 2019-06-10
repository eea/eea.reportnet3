package org.eea.interfaces.vo.dataset;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FieldValidationVO {

  private Long id;


  private FieldVO fieldValue;

  private ValidationVO validation;
}
