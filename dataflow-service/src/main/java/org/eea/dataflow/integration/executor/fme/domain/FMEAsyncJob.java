package org.eea.dataflow.integration.executor.fme.domain;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class FMEAsyncJob.
 */
@Getter
@Setter
@ToString
public class FMEAsyncJob {

  /** The nm directives. */
  private NMDirectives nmDirectives;

  /** The tm directives. */
  private TMDirectives tmDirectives;

  /** The published parameters. */
  private List<PublishedParameter> publishedParameters;

  /** The workspace path. */
  private String workspacePath;

}
