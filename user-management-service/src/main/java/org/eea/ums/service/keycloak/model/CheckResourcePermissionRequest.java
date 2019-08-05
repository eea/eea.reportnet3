package org.eea.ums.service.keycloak.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "resources",
    "roleIds",
    "userId"
})
@Getter
@Setter
public class CheckResourcePermissionRequest {

  @JsonProperty("resources")
  private List<Resource> resources = null;
  @JsonProperty("roleIds")
  private List<String> roleIds = null;
  @JsonProperty("userId")
  private String userId;

}
