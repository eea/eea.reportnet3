package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class TableValidationVO.
 */
@Getter
@Setter
@ToString
public class TableValidationVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -6619907416427612802L;


  /** The id. */
  private Long id;


  /** The table vlaue. */
  private TableVO tableValue;

  /** The validation. */
  private ValidationVO validation;
}
