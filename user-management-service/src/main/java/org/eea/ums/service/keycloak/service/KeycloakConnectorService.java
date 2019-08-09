package org.eea.ums.service.keycloak.service;

import java.util.List;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.ums.service.keycloak.model.GroupInfo;

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
  String checkUserPermision(String resourceName, AccessScopeEnum... scopes);

  /**
   * Generate token string.
   *
   * @param username the username
   * @param password the password
   *
   * @return the string
   */
  String generateToken(String username, String password);

  /**
   * Gets groups by user.
   *
   * @param userId the user id
   *
   * @return the groups by user
   */
  GroupInfo[] getGroupsByUser(String userId);
}
