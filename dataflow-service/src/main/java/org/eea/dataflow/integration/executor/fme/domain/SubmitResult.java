package org.eea.dataflow.integration.executor.fme.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id"})
@Getter
@Setter
public class SubmitResult {
  @JsonProperty("id")
  private Integer id;
}
