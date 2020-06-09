package org.eea.dataflow.integration.executor.fme.domain;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FMEAsyncJob {

  // Notification Manager directives
  private NMDirectives nmDirectives;
  // Transformation Manager directives
  private TMDirectives tmDirectives;
  // Workspace published parameters defined for this job
  private List<PublishedParameter> publishedParameters;

  private String workspacePath;

}
