package org.eea.ums.service.keycloak.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

/**
 * The Class ResourceInfo.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "type", "owner", "ownerManagedAccess", "displayName", "_id", "uris"})


@Getter
@Setter
public class ResourceInfo {

  /** The name. */
  @JsonProperty("name")
  private String name;

  /** The type. */
  @JsonProperty("type")
  private String type;

  /** The owner managed access. */
  @JsonProperty("ownerManagedAccess")
  private Boolean ownerManagedAccess;

  /** The display name. */
  @JsonProperty("displayName")
  private String displayName;

  /** The id. */
  @JsonProperty("_id")
  private String id;

  /** The uris. */
  @JsonProperty("uris")
  private List<String> uris = null;

}
