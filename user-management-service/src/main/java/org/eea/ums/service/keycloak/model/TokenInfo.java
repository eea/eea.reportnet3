package org.eea.ums.service.keycloak.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class TokenInfo.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"access_token", "expires_in", "refresh_expires_in", "refresh_token",
    "token_type", "not-before-policy", "session_state", "scope"})

@Getter
@Setter
@ToString
public class TokenInfo {

  /** The access token. */
  @JsonProperty("access_token")
  private String accessToken;

  /** The expires in. */
  @JsonProperty("expires_in")
  private Long expiresIn;

  /** The refresh expires in. */
  @JsonProperty("refresh_expires_in")
  private Long refreshExpiresIn;

  /** The refresh token. */
  @JsonProperty("refresh_token")
  private String refreshToken;

  /** The token type. */
  @JsonProperty("token_type")
  private String tokenType;

  /** The not before policy. */
  @JsonProperty("not-before-policy")
  private Long notBeforePolicy;

  /** The session state. */
  @JsonProperty("session_state")
  private String sessionState;

  /** The scope. */
  @JsonProperty("scope")
  private String scope;

}
