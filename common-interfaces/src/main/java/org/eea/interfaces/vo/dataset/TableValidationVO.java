package org.eea.interfaces.vo.dataset;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class TableValidationVO.
 */
@Getter
@Setter
@ToString
public class TableValidationVO {

  /** The id. */
  private Long id;


  /** The table vlaue. */
  private TableVO tableValue;

  /** The validation. */
  private ValidationVO validation;
}
