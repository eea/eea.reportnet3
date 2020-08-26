package org.eea.interfaces.vo.integration.fme;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Instantiates a new FME operation info VO.
 */
@Data
public class FMEOperationInfoVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -2364231167941379128L;

  /** The api key. */
  private String apiKey;

  /** The fme job id. */
  @JsonProperty("fme_job_id")
  private Long fmeJobId;

  /** The fme job status. */
  @JsonProperty("StatusNumber")
  private long statusNumber;
}
