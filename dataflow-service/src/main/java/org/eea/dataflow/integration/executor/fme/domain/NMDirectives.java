package org.eea.dataflow.integration.executor.fme.domain;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NMDirectives {
  // Additional NM Directives passed to the notification service for customized usage of
  // subscribers,
  private List<Directive> directives;
  // Topics to notify when the job fails,
  private List<String> failureTopics;
  // Topics to notify when the job succeeds
  private List<String> successTopics;
}
