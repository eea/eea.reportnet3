package org.eea.ums.service.keycloak.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "type",
    "owner",
    "ownerManagedAccess",
    "displayName",
    "_id",
    "uris"
})
@Getter
@Setter
public class ResourceInfo {

  @JsonProperty("name")
  private String name;
  @JsonProperty("type")
  private String type;
  @JsonProperty("ownerManagedAccess")
  private Boolean ownerManagedAccess;
  @JsonProperty("displayName")
  private String displayName;
  @JsonProperty("_id")
  private String id;
  @JsonProperty("uris")
  private List<String> uris = null;

}