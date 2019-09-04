package org.eea.ums.service.keycloak.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

/**
 * The Class CheckResourcePermissionResult.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"status",})


@Getter
@Setter
public class CheckResourcePermissionResult {


  /** The status. */
  @JsonProperty("status")
  private String status;

}
