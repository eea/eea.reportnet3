package org.eea.interfaces.vo.dataset;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class FieldValidationVO.
 */
@Getter
@Setter
@ToString
public class FieldValidationVO {

  /** The id. */
  private Long id;


  /** The field value. */
  private FieldVO fieldValue;

  /** The validation. */
  private ValidationVO validation;
}
