package org.eea.ums.service;


import java.util.List;
import javax.annotation.Nullable;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.ums.service.vo.UserVO;

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
   */
  void createResourceInstance(ResourceInfoVO resourceInfoVO);

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
   */
  void addUserToUserGroup(String userId, String groupName);

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
   * @param refreshToken the refresh token
   */
  void doLogout(String refreshToken);


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
   * Add contributor to user group.
   *
   * @param userMail the user mail
   * @param groupName the group name
   */
  void addContributorToUserGroup(String userMail, String groupName) throws EEAException;
}
