package org.eea.dataflow.integration.executor.fme.domain;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class NMDirectives.
 */
@Getter
@Setter
@ToString
public class NMDirectives {

  /**
   * The directives. Aditional NM Directives passed to the notification service for customized usage
   * of subscribers
   */
  private List<Directive> directives;

  /** The failure topics. Topics to notify when the job fails. */
  private List<String> failureTopics;

  /** The success topics. Topics to notify when the job succeeds */
  private List<String> successTopics;
}
