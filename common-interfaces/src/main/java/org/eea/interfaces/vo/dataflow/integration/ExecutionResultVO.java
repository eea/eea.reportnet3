package org.eea.interfaces.vo.dataflow.integration;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ExecutionResultVO {

  private Boolean invocationExecuted;

  private Map<String, Object> executionResultParams;

  private Throwable error;

}
