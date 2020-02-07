package org.eea.ums.service;


import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.ums.service.vo.UserVO;
import org.keycloak.representations.idm.UserRepresentation;

/**
 * The interface Security provider interface service.
 */
public interface SecurityProviderInterfaceService {


  /**
   * Do login token vo.
   *
   * @param username the username
   * @param password the password
   * @param extraParams the extra params
   *
   * @return the token vo
   */
  TokenVO doLogin(String username, String password, Object... extraParams);

  /**
   * Do login token vo.
   *
   * @param code the code
   *
   * @return the token vo
   */
  TokenVO doLogin(String code);

  /**
   * Check access permission boolean.
   *
   * @param resource the resource
   * @param scopes the scopes
   *
   * @return the boolean
   */
  Boolean checkAccessPermission(String resource, AccessScopeEnum... scopes);

  /**
   * Gets users.
   *
   * @param userId the user id
   *
   * @return the users
   */
  List<UserVO> getUsers(@Nullable String userId);

  /**
   * Create resource instance.
   *
   * @param resourceInfoVO the resource info vo
   * @throws EEAException
   */
  void createResourceInstance(ResourceInfoVO resourceInfoVO) throws EEAException;

  /**
   * Delete resource instances.
   *
   * @param resourceInfoVO the resource info vo
   */
  void deleteResourceInstances(List<ResourceInfoVO> resourceInfoVO);

  /**
   * Delete resource instances by name.
   *
   * @param resourceName the resource name
   */
  void deleteResourceInstancesByName(List<String> resourceName);


  /**
   * Add user to user group.
   *
   * @param userId the user id
   * @param groupName the group name
   * @throws EEAException
   */
  void addUserToUserGroup(String userId, String groupName) throws EEAException;

  /**
   * Remove user from user group.
   *
   * @param userId the user id
   * @param groupId the group id
   */
  void removeUserFromUserGroup(String userId, String groupId);

  /**
   * Gets resources by user.
   *
   * @param userId the user id
   *
   * @return the resources by user
   */
  List<ResourceAccessVO> getResourcesByUser(String userId);


  /**
   * Refresh token token vo.
   *
   * @param refreshToken the refresh token
   *
   * @return the token vo
   */
  TokenVO refreshToken(String refreshToken);

  /**
   * Do logout.
   *
   * @param authToken the auth token
   */
  void doLogout(String authToken);


  /**
   * Gets resource details.
   *
   * @param groupId the group id
   *
   * @return the resource details
   */
  ResourceInfoVO getResourceDetails(String groupId);


  /**
   * Gets the groups by id resource type.
   *
   * @param idResource the id resource
   * @param resourceType the resource type
   *
   * @return the groups by id resource type
   */
  List<ResourceInfoVO> getGroupsByIdResourceType(Long idResource, ResourceTypeEnum resourceType);


  /**
   * Creates the resource instance.
   *
   * @param resourceInfoVOs the resource info V os
   * @throws EEAException
   */
  void createResourceInstance(List<ResourceInfoVO> resourceInfoVOs) throws EEAException;

  /**
   * Adds the contributosr to user group.
   *
   * @param resources the resources
   * @throws EEAException
   */
  void addContributorsToUserGroup(List<ResourceAssignationVO> resources) throws EEAException;


  /**
   * Adds the contributor to user group.
   *
   * @param contributor the contributor
   * @param userMail the user mail
   * @param groupName the group name
   * @throws EEAException the EEA exception
   */
  void addContributorToUserGroup(Optional<UserRepresentation> contributor, String userMail,
      String groupName) throws EEAException;


}
