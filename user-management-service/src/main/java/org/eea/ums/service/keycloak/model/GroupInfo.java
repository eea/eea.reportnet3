package org.eea.ums.service.keycloak.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class GroupInfo.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "name", "path"})


@Getter
@Setter
@ToString
public class GroupInfo {

  /** The id. */
  @JsonProperty("id")
  private String id;

  /** The name. */
  @JsonProperty("name")
  private String name;

  /** The path. */
  @JsonProperty("path")
  private String path;

}
