package org.eea.ums.service;


import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
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
  TokenVO doLogin(String username, String password, Object... extraParams);

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
   * Gets group detail.
   *
   * @param groupId the group id
   *
   * @return the group detail
   */
  ResourceInfoVO getGroupDetail(String groupId);
}
