package org.eea.interfaces.vo.dataset;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class DatasetValidationVO.
 */
@Getter
@Setter
@ToString
public class DatasetValidationVO {

  /** The id. */
  private Long id;


  /** The dataset value. */
  private DataSetVO datasetValue;

  /** The validation. */
  private ValidationVO validation;

}
