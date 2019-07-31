package org.eea.ums.service.impl;


import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.eea.ums.service.keycloak.service.impl.KeycloakConnectorServiceImpl;
import org.eea.ums.service.vo.UserGroupVO;
import org.eea.ums.service.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KeycloakSecurityProviderInterfaceService implements SecurityProviderInterfaceService {


  @Autowired
  private KeycloakConnectorService keycloakConnectorService;

  public String generateToken(String username, String password, Object... extraParams) {
    String token = "";
    if (verifyUserExist(username, password)) {
      token = generateToken(username, password);
    }

    return token;
  }

  private Boolean verifyUserExist(String username, String password) {
    Boolean exists = false;
    if (username.equals("eea")) {
      exists = true;
    }
    return exists;
  }


  @Override
  public String doLogin(String username, String password, Object... extraParams) {
    return keycloakConnectorService.generateToken(username, password);
  }

  @Override
  public Boolean checkAccessPermission(String resource, String... scopes) {
    return !keycloakConnectorService.checkUserPermision(resource, scopes).equals("DENY");
  }

  @Override
  public List<UserGroupVO> getUserGroupInfo(String securityToken) {
    throw new UnsupportedOperationException("Method Not implemented yet");
  }

  @Override
  public List<UserVO> getUsers(@Nullable String userId, String securityToken) {
    throw new UnsupportedOperationException("Method Not implemented yet");
  }

  @Override
  public void createUserGroup(String userGroupName, String securityToken,
      Map<String, String> attributes) {
    throw new UnsupportedOperationException("Method Not implemented yet");
  }

  @Override
  public void addUserToUserGroup(String userId, String groupId, String securityToken) {
    throw new UnsupportedOperationException("Method Not implemented yet");
  }

  @Override
  public void removeUserFromUserGroup(String userId, String groupId, String securityToken) {
    throw new UnsupportedOperationException("Method Not implemented yet");
  }
}
