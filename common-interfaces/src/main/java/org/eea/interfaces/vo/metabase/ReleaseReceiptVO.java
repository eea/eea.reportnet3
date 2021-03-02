package org.eea.interfaces.vo.metabase;

import java.io.Serializable;
import java.util.List;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class ReleaseReceiptVO.
 */
@Getter
@Setter
@ToString
public class ReleaseReceiptVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -1860518284289153708L;

  /** The id dataflow. */
  private Long idDataflow;

  /** The dataflow name. */
  private String dataflowName;

  /** The datasets. */
  private List<ReportingDatasetVO> datasets;

  /** The provider email. */
  private String providerEmail;

  /** The provider assignation. */
  private String providerAssignation;

  /** The obligation title. */
  private String obligationTitle;

  /** The obligation id. */
  private Integer obligationId;

  /** The user name. */
  private String email;
}
