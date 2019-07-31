package org.eea.ums.service.keycloak.service;

/**
 * The interface Keycloak connector service.
 */
public interface KeycloakConnectorService {

  /**
   * Check user permision boolean.
   *
   * @param resourceName the resource name
   * @param scopes the scopes
   *
   * @return the boolean
   */
  String checkUserPermision(String resourceName, String... scopes);

  /**
   * Generate token string.
   *
   * @param username the username
   * @param password the password
   *
   * @return the string
   */
  String generateToken(String username, String password);
}
