package org.eea.interfaces.vo.dataset;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class RecordValidationVO.
 */
@Getter
@Setter
@ToString
public class RecordValidationVO {

  /** The id. */
  private Long id;

  /** The record value. */
  private RecordVO recordValue;

  /** The validation. */
  private ValidationVO validation;
}
