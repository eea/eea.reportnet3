package org.eea.interfaces.vo.dataset;

import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import lombok.Data;

/**
 * Instantiates a new status message VO.
 */
@Data
public class DatasetStatusMessageVO {

  /** The message. */
  private String message;

  /** The dataset id. */
  private Long datasetId;

  /** The state. */
  private DatasetStatusEnum status;

  /** The dataflow id. */
  private Long dataflowId;

}
