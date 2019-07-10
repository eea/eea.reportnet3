package org.eea.ums.service;


import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.eea.ums.service.vo.UserGroupVO;
import org.eea.ums.service.vo.UserVO;

public interface SecurityProviderInterfaceService {


  String doLogin(String username, String password, Object... extraParams);

  List<UserGroupVO> getUserGroupInfo(String securityToken);

  List<UserVO> getUsers(@Nullable String userId, String securityToken);

  void createUserGroup(String userGroupName, String securityToken, Map<String, String> attributes);

  void addUserToUserGroup(String userId, String groupId, String securityToken);

  void removeUserFromUserGroup(String userId, String groupId, String securityToken);


}
