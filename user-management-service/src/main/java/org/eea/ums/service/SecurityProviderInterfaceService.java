package org.eea.ums.service;


import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.ums.service.vo.UserGroupVO;
import org.eea.ums.service.vo.UserVO;

/**
 * The interface Security provider interface service.
 */
public interface SecurityProviderInterfaceService {


  /**
   * Do login string.
   *
   * @param username the username
   * @param password the password
   * @param extraParams the extra params
   *
   * @return the string
   */
  String doLogin(String username, String password, Object... extraParams);

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
   * @param userGroupName the user group name
   * @param attributes the attributes
   */
  void createResourceInstance(String userGroupName, Map<String, String> attributes);

  /**
   * Add user to user group.
   *
   * @param userId the user id
   * @param groupId the group id
   * @param securityToken the security token
   */
  void addUserToUserGroup(String userId, String groupId, String securityToken);

  /**
   * Remove user from user group.
   *
   * @param userId the user id
   * @param groupId the group id
   * @param securityToken the security token
   */
  void removeUserFromUserGroup(String userId, String groupId, String securityToken);

  /**
   * Gets resources by user.
   *
   * @param userId the user id
   *
   * @return the resources by user
   */
  List<ResourceAccessVO> getResourcesByUser(String userId);

}
