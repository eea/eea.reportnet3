package org.eea.ums.service.keycloak.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Resource.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "scopes"
})
@Getter
@Setter
public class Resource {

  /**
   * The Name.
   */
  @JsonProperty("name")
  private String name;
  /**
   * The Scopes.
   */
  @JsonProperty("scopes")
  private List<String> scopes = null;

}