package org.eea.ums.service.keycloak.service;

import org.eea.exception.EEAException;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.model.TokenInfo;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

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
   * Generate admin token token info.
   *
   * @param username the username
   * @param password the password
   *
   * @return the token info
   */
  TokenInfo generateAdminToken(String username, String password);

  /**
   * Gets groups by user.
   *
   * @param userId the user resourceId
   *
   * @return the groups by user
   */
  GroupInfo[] getGroupsByUser(String userId);

  /**
   * Generate token token info based on authorization code.
   *
   * @param code the code
   *
   * @return the token info
   */
  TokenInfo generateToken(String code);

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
   * Gets group detail.
   *
   * @param groupId the group resourceId
   *
   * @return the group detail
   */
  GroupInfo getGroupDetail(String groupId);


  /**
   * Create group detail.
   *
   * @param groupInfo the group info
   *
   * @throws EEAException the EEA exception
   */
  void createGroupDetail(GroupInfo groupInfo) throws EEAException;

  /**
   * Delete group detail.
   *
   * @param groupId the group id
   */
  void deleteGroupDetail(String groupId);

  /**
   * Add user to group.
   *
   * @param userId the user resourceId
   * @param groupId the group resourceId
   *
   * @throws EEAException the EEA exception
   */
  void addUserToGroup(String userId, String groupId) throws EEAException;

  /**
   * Adds the user.
   *
   * @param body the body
   */
  void addUser(String body);

  /**
   * Gets the users.
   *
   * @return the users
   */
  UserRepresentation[] getUsers();

  /**
   * Gets the users by email.
   *
   * @param email the email
   *
   * @return the users by email
   */
  UserRepresentation[] getUsersByEmail(String email);


  /**
   * Gets the role.
   *
   * @return the role
   */
  RoleRepresentation[] getRoles();

  /**
   * Adds the role.
   *
   * @param body the body
   * @param userId the user id
   */
  void addRole(String body, String userId);

  /**
   * Update user.
   *
   * @param user the user
   */
  void updateUser(UserRepresentation user);

  /**
   * Gets the user.
   *
   * @param userId the user id
   *
   * @return the user
   */
  UserRepresentation getUser(String userId);

  /**
   * Gets the user roles.
   *
   * @param userId the user id
   *
   * @return the user roles
   */
  RoleRepresentation[] getUserRoles(String userId);

  /**
   * Gets the groups with search.
   *
   * @param value the value
   * @return the group with search
   */
  GroupInfo[] getGroupsWithSearch(String value);

  /**
   * Gets the users by group id.
   *
   * @param groupId the group id
   * @return the user by group id
   */
  UserRepresentation[] getUsersByGroupId(String groupId);
}
