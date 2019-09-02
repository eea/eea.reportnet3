package org.eea.ums.service.keycloak.service;

import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.model.TokenInfo;

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
  TokenInfo generateToken(String username, String password);

  /**
   * Gets groups by user.
   *
   * @param userId the user id
   *
   * @return the groups by user
   */
  GroupInfo[] getGroupsByUser(String userId);

  /**
   * Refresh token token info.
   *
   * @param refreshToken the refresh token
   *
   * @return the token info
   */
  TokenInfo refreshToken(String refreshToken);

  /**
   * Logout.
   *
   * @param refreshToken the refresh token
   */
  void logout(String refreshToken);

  /**
   * Get groups group info [ ].
   *
   * @return the group info [ ]
   */
  GroupInfo[] getGroups();

  /**
   * Add user to group.
   *
   * @param userId the user id
   * @param groupId the group id
   */
  void addUserToGroup(String userId, String groupId);
}
