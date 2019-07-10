package org.eea.ums.service.impl;


import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.eea.ums.service.keycloak.KeycloakConnectorService;
import org.eea.ums.service.vo.UserGroupVO;
import org.eea.ums.service.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
