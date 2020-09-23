package org.eea.dataflow.integration.executor.fme.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

/**
 * The Class FileSubmitResult.
 */
@JsonPropertyOrder({"name"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Setter
public class FileSubmitResult {

  /** The date. */
  @JsonProperty("date")
  private String date;

  /** The name. */
  @JsonProperty("name")
  private String name;

  /** The path. */
  @JsonProperty("path")
  private String path;

  /** The size. */
  @JsonProperty("size")
  private Integer size;

  /** The type. */
  @JsonProperty("type")
  private String type;

}
