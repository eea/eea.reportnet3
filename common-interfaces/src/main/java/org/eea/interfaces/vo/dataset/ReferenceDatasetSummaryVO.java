package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class ReferenceDatasetSummaryVO.
 */
@Getter
@Setter
@ToString
public class ReferenceDatasetSummaryVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -1348263779137653665L;

  /** The id. */
  private Long id;

  /** The dataset name. */
  private String datasetName;

  /** The dataset type enum. */
  private DatasetTypeEnum datasetTypeEnum;
}
