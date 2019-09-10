package org.eea.ums.service.keycloak.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;


/**
 * The Class ClientInfo.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "clientId", "surrogateAuthRequired", "enabled", "clientAuthenticatorType",
    "redirectUris", "webOrigins", "notBefore", "bearerOnly", "consentRequired",
    "standardFlowEnabled", "implicitFlowEnabled", "directAccessGrantsEnabled",
    "serviceAccountsEnabled", "authorizationServicesEnabled", "publicClient", "frontchannelLogout",
    "protocol", "fullScopeAllowed", "nodeReRegistrationTimeout", "defaultClientScopes",
    "optionalClientScopes", "name", "baseUrl", "defaultRoles"})


@Getter
@Setter
public class ClientInfo {

  /** The id. */
  @JsonProperty("id")
  private String id;

  /** The client id. */
  @JsonProperty("clientId")
  private String clientId;

  /** The surrogate auth required. */
  @JsonProperty("surrogateAuthRequired")
  private Boolean surrogateAuthRequired;

  /** The enabled. */
  @JsonProperty("enabled")
  private Boolean enabled;

  /** The client authenticator type. */
  @JsonProperty("clientAuthenticatorType")
  private String clientAuthenticatorType;

  /** The redirect uris. */
  @JsonProperty("redirectUris")
  private List<String> redirectUris = null;

  /** The web origins. */
  @JsonProperty("webOrigins")
  private List<Object> webOrigins = null;

  /** The not before. */
  @JsonProperty("notBefore")
  private Long notBefore;

  /** The bearer only. */
  @JsonProperty("bearerOnly")
  private Boolean bearerOnly;

  /** The consent required. */
  @JsonProperty("consentRequired")
  private Boolean consentRequired;

  /** The standard flow enabled. */
  @JsonProperty("standardFlowEnabled")
  private Boolean standardFlowEnabled;

  /** The implicit flow enabled. */
  @JsonProperty("implicitFlowEnabled")
  private Boolean implicitFlowEnabled;

  /** The direct access grants enabled. */
  @JsonProperty("directAccessGrantsEnabled")
  private Boolean directAccessGrantsEnabled;

  /** The service accounts enabled. */
  @JsonProperty("serviceAccountsEnabled")
  private Boolean serviceAccountsEnabled;

  /** The authorization services enabled. */
  @JsonProperty("authorizationServicesEnabled")
  private Boolean authorizationServicesEnabled;

  /** The public client. */
  @JsonProperty("publicClient")
  private Boolean publicClient;

  /** The frontchannel logout. */
  @JsonProperty("frontchannelLogout")
  private Boolean frontchannelLogout;

  /** The protocol. */
  @JsonProperty("protocol")
  private String protocol;

  /** The full scope allowed. */
  @JsonProperty("fullScopeAllowed")
  private Boolean fullScopeAllowed;

  /** The node re registration timeout. */
  @JsonProperty("nodeReRegistrationTimeout")
  private Long nodeReRegistrationTimeout;

  /** The default client scopes. */
  @JsonProperty("defaultClientScopes")
  private List<String> defaultClientScopes = null;

  /** The optional client scopes. */
  @JsonProperty("optionalClientScopes")
  private List<String> optionalClientScopes = null;

  /** The name. */
  @JsonProperty("name")
  private String name;

  /** The base url. */
  @JsonProperty("baseUrl")
  private String baseUrl;

  /** The default roles. */
  @JsonProperty("defaultRoles")
  private List<String> defaultRoles = null;

}
