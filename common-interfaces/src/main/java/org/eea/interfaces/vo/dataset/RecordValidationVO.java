package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class RecordValidationVO.
 */
@Getter
@Setter
@ToString
public class RecordValidationVO implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 918058325040983759L;

  /** The id. */
  private Long id;

  /** The record value. */
  private RecordVO recordValue;

  /** The validation. */
  private ValidationVO validation;
}
