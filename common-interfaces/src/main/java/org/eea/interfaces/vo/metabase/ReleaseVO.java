package org.eea.interfaces.vo.metabase;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class ReleaseVO.
 */
@Getter
@Setter
@ToString
public class ReleaseVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -8809908503554688225L;

  /** The id. */
  private Long id;

  /** The date released. */
  private Date dateReleased;

  /** The data collection release. */
  private Boolean dcrelease;

  /** The eu dataset release. */
  private Boolean eurelease;

  /** The dataset id. */
  private Long datasetId;

  /** The dataset name. */
  private String datasetName;

  /** The country code. */
  private String datasetProviderCode;
}
