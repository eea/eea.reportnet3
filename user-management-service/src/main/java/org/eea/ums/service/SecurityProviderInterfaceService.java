package org.eea.ums.service;


import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
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
   * Gets user group info.
   *
   * @param securityToken the security token
   *
   * @return the user group info
   */
  List<UserGroupVO> getUserGroupInfo(String securityToken);

  /**
   * Gets users.
   *
   * @param userId the user id
   * @param securityToken the security token
   *
   * @return the users
   */
  List<UserVO> getUsers(@Nullable String userId, String securityToken);

  /**
   * Create user group.
   *
   * @param userGroupName the user group name
   * @param securityToken the security token
   * @param attributes the attributes
   */
  void createUserGroup(String userGroupName, String securityToken, Map<String, String> attributes);

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


}
