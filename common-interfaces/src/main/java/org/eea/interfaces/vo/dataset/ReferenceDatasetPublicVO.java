package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



/**
 * The Class ReferenceDatasetPublicVO.
 */
@Getter
@Setter
@ToString
public class ReferenceDatasetPublicVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 9025687907004150134L;

  /** The data set name. */
  private String dataSetName;

  /** The public file name. */
  private String publicFileName;

  /** The updatable. */
  private Boolean updatable;

}
