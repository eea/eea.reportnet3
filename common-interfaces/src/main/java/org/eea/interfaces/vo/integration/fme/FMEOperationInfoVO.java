package org.eea.interfaces.vo.integration.fme;

import java.io.Serializable;
import org.eea.interfaces.vo.integration.enums.FMEOperation;
import lombok.Data;

/**
 * Instantiates a new FME operation info VO.
 */
@Data
public class FMEOperationInfoVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -2364231167941379128L;

  /** The dataset id. */
  private Long datasetId;

  /** The dataflow id. */
  private Long dataflowId;

  /** The provider id. */
  private Long providerId;

  /** The file name. */
  private String fileName;

  /** The fme operation. */
  private FMEOperation fmeOperation;
}
