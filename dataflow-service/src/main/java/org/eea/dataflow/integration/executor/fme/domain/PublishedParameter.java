package org.eea.dataflow.integration.executor.fme.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PublishedParameter {
  private String name;
  private Object value;

}
