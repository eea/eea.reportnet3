package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class FieldValidationVO.
 */
@Getter
@Setter
@ToString
public class FieldValidationVO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;


  /** The id. */
  private Long id;


  /** The field value. */
  private FieldVO fieldValue;

  /** The validation. */
  private ValidationVO validation;
}
