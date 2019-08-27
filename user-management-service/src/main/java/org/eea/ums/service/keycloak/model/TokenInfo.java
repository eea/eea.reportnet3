package org.eea.ums.service.keycloak.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "access_token",
    "expires_in",
    "refresh_expires_in",
    "refresh_token",
    "token_type",
    "not-before-policy",
    "session_state",
    "scope"
})
@Getter
@Setter
@ToString
public class TokenInfo {

  @JsonProperty("access_token")
  private String accessToken;
  @JsonProperty("expires_in")
  private Long expiresIn;
  @JsonProperty("refresh_expires_in")
  private Long refreshExpiresIn;
  @JsonProperty("refresh_token")
  private String refreshToken;
  @JsonProperty("token_type")
  private String tokenType;
  @JsonProperty("not-before-policy")
  private Long notBeforePolicy;
  @JsonProperty("session_state")
  private String sessionState;
  @JsonProperty("scope")
  private String scope;

}