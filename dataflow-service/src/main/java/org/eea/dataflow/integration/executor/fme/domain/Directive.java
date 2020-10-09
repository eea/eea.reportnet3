package org.eea.dataflow.integration.executor.fme.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class Directive.
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Directive {

  /** The name. */
  private String name;

  /** The value. */
  private String value;
}
