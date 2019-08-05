package org.eea.ums.service.keycloak.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "clientId",
    "surrogateAuthRequired",
    "enabled",
    "clientAuthenticatorType",
    "redirectUris",
    "webOrigins",
    "notBefore",
    "bearerOnly",
    "consentRequired",
    "standardFlowEnabled",
    "implicitFlowEnabled",
    "directAccessGrantsEnabled",
    "serviceAccountsEnabled",
    "authorizationServicesEnabled",
    "publicClient",
    "frontchannelLogout",
    "protocol",
    "fullScopeAllowed",
    "nodeReRegistrationTimeout",
    "defaultClientScopes",
    "optionalClientScopes",
    "name",
    "baseUrl",
    "defaultRoles"
})
@Getter
@Setter
public class ClientInfo {

  @JsonProperty("id")
  private String id;
  @JsonProperty("clientId")
  private String clientId;
  @JsonProperty("surrogateAuthRequired")
  private Boolean surrogateAuthRequired;
  @JsonProperty("enabled")
  private Boolean enabled;
  @JsonProperty("clientAuthenticatorType")
  private String clientAuthenticatorType;
  @JsonProperty("redirectUris")
  private List<String> redirectUris = null;
  @JsonProperty("webOrigins")
  private List<Object> webOrigins = null;
  @JsonProperty("notBefore")
  private Long notBefore;
  @JsonProperty("bearerOnly")
  private Boolean bearerOnly;
  @JsonProperty("consentRequired")
  private Boolean consentRequired;
  @JsonProperty("standardFlowEnabled")
  private Boolean standardFlowEnabled;
  @JsonProperty("implicitFlowEnabled")
  private Boolean implicitFlowEnabled;
  @JsonProperty("directAccessGrantsEnabled")
  private Boolean directAccessGrantsEnabled;
  @JsonProperty("serviceAccountsEnabled")
  private Boolean serviceAccountsEnabled;
  @JsonProperty("authorizationServicesEnabled")
  private Boolean authorizationServicesEnabled;
  @JsonProperty("publicClient")
  private Boolean publicClient;
  @JsonProperty("frontchannelLogout")
  private Boolean frontchannelLogout;
  @JsonProperty("protocol")
  private String protocol;
  @JsonProperty("fullScopeAllowed")
  private Boolean fullScopeAllowed;
  @JsonProperty("nodeReRegistrationTimeout")
  private Long nodeReRegistrationTimeout;
  @JsonProperty("defaultClientScopes")
  private List<String> defaultClientScopes = null;
  @JsonProperty("optionalClientScopes")
  private List<String> optionalClientScopes = null;
  @JsonProperty("name")
  private String name;
  @JsonProperty("baseUrl")
  private String baseUrl;
  @JsonProperty("defaultRoles")
  private List<String> defaultRoles = null;

}
