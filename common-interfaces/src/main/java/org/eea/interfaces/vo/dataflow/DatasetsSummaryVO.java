package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import lombok.Data;
import lombok.ToString;

/**
 * The Class DatasetsSummaryVO
 */
@Data
@ToString
public class DatasetsSummaryVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The id. */
  private Long id;

  /** The data set name. */
  private String dataSetName;

  /** The dataset type enum. */
  private DatasetTypeEnum datasetTypeEnum;

  /** The data provider code. */
  private String dataProviderCode;

  /** The data provider name. */
  private String dataProviderName;

}
