package org.eea.dataflow.integration.executor.fme.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@JsonPropertyOrder({"name"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Setter
public class FileSubmitResult {

  @JsonProperty("date")
  private String date;
  @JsonProperty("name")
  private String name;
  @JsonProperty("path")
  private String path;
  @JsonProperty("size")
  private Integer size;
  @JsonProperty("type")
  private String type;

}
