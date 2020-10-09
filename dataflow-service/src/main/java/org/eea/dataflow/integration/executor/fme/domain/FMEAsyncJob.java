package org.eea.dataflow.integration.executor.fme.domain;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class FMEAsyncJob.
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FMEAsyncJob {

  /** The nm directives. */
  @JsonProperty("NMDirectives")
  private NMDirectives nmDirectives;

  /** The tm directives. */
  @JsonProperty("TMDirectives")
  private TMDirectives tmDirectives;

  /** The published parameters. */
  private List<PublishedParameter> publishedParameters;

  /** The workspace path. */
  private String workspacePath;

}
