package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class DatasetValidationVO.
 */
@Getter
@Setter
@ToString
public class DatasetValidationVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 7412301149515906080L;


  /** The id. */
  private Long id;


  /** The dataset value. */
  private DataSetVO datasetValue;

  /** The validation. */
  private ValidationVO validation;

}
