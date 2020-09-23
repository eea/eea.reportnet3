package org.eea.interfaces.vo.dataflow.integration;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class ExecutionResultVO.
 */
@Getter
@Setter
@ToString
public class ExecutionResultVO {

  /** The invocation executed. */
  private Boolean invocationExecuted;

  /** The execution result params. */
  private Map<String, Object> executionResultParams;

  /** The error. */
  private Throwable error;

}
