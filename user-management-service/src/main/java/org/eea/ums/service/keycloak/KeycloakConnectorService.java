package org.eea.ums.service.keycloak;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eea.ums.service.keycloak.model.TokenInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class KeycloakConnectorService {

  @Value("${eea.keycloak.realmName}")
  private String realmName;

  @Value("${eea.keycloak.secret}")
  private String secret;

  @Value("${eea.keycloak.clientId}")
  private String clientId;

  @Value("${eea.keycloak.host}")
  private String keycloakHost;

  @Value("${eea.keycloak.scheme}")
  private String keycloakScheme;

  private RestTemplate restTemplate;

  private static final String GENERATE_TOKEN_URL = "/auth/realms/{realm}/protocol/openid-connect/token";
  private static final String LIST_USERS_URL = "";
  private static final String LIST_USER_GROUPS_URL = "";
  private static final String CREATE_USER_GROUP_URL = "";
  private static final String ADD_USER_TO_USER_GROUP_URL = "";

  public KeycloakConnectorService() {
    this.restTemplate = new RestTemplate();
  }


  public String generateToken(String username, String password) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put("realm", realmName);

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("username", username);
    map.add("grant_type", "password");
    map.add("password", password);
    map.add("client_secret", secret);
    map.add("client_id", clientId);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(
        map, headers);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    ResponseEntity<TokenInfo> tokenInfo = this.restTemplate
        .postForEntity(
            uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost).path(GENERATE_TOKEN_URL)
                .buildAndExpand(uriParams).toString(),
            request,
            TokenInfo.class);
    return tokenInfo.getBody().getAccessToken();

  }

}
