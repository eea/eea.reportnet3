package org.eea.ums.service.keycloak.model;


import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "status",
})
@Getter
@Setter
public class CheckResourcePermissionResult {


  @JsonProperty("status")
  private String status;

}