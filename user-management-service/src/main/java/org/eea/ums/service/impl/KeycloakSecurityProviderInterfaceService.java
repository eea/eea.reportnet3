package org.eea.ums.service.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.ums.mapper.GroupInfoMapper;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.model.TokenInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.eea.ums.service.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KeycloakSecurityProviderInterfaceService implements SecurityProviderInterfaceService {


  @Autowired
  private KeycloakConnectorService keycloakConnectorService;

  @Autowired
  private GroupInfoMapper groupInfoMapper;

  @Override
  public TokenVO doLogin(String username, String password, Object... extraParams) {
    TokenInfo tokenInfo = keycloakConnectorService.generateToken(username, password);
    TokenVO tokenVO = new TokenVO();
    if (null != tokenInfo) {
      tokenVO.setAccessToken(tokenInfo.getAccessToken());
      tokenVO.setRefreshToken(tokenInfo.getRefreshToken());
    }
    return tokenVO;
  }

  @Override
  public TokenVO refreshToken(String refreshToken) {
    TokenInfo tokenInfo = keycloakConnectorService.refreshToken(refreshToken);
    TokenVO tokenVO = new TokenVO();
    if (null != tokenInfo) {
      tokenVO.setAccessToken(tokenInfo.getAccessToken());
      tokenVO.setRefreshToken(tokenInfo.getRefreshToken());
    }
    return tokenVO;
  }

  @Override
  public void doLogout(String refreshToken) {
    keycloakConnectorService.logout(refreshToken);
  }

  @Override
  public ResourceInfoVO getGroupDetail(String groupName) {
    GroupInfo[] groups = keycloakConnectorService.getGroups();
    String groupId = "";
    ResourceInfoVO result = new ResourceInfoVO();
    if (null != groups && groups.length > 0) {
      groupId = Arrays.asList(groups).stream()
          .filter(groupInfo -> groupName.toUpperCase().equals(groupInfo.getName().toUpperCase()))
          .map(GroupInfo::getId)
          .findFirst().orElse("");
    }
    if (StringUtils.isNotBlank(groupId)) {
      result = this.groupInfoMapper.entityToClass(keycloakConnectorService.getGroupDetail(groupId));
    }
    return result;
  }

  @Override
  public Boolean checkAccessPermission(String resource, AccessScopeEnum... scopes) {
    return !keycloakConnectorService.checkUserPermision(resource, scopes).equals("DENY");
  }

  @Override
  public List<UserVO> getUsers(@Nullable String userId) {
    throw new UnsupportedOperationException("Method Not implemented yet");
  }

  @Override
  public void createResourceInstance(String userGroupName, Map<String, String> attributes) {
    throw new UnsupportedOperationException("Method Not implemented yet");
  }


  @Override
  public void addUserToUserGroup(String userId, String groupName) {
    GroupInfo[] groups = keycloakConnectorService.getGroups();
    if (null != groups && groups.length > 0) {
      String groupId = Arrays.asList(groups).stream()
          .filter(groupInfo -> groupName.toUpperCase().equals(groupInfo.getName().toUpperCase()))
          .map(GroupInfo::getId)
          .findFirst().orElse("");
      if (StringUtils.isNotBlank(groupId)) {
        keycloakConnectorService.addUserToGroup(userId, groupId);
      }
    }

  }

  @Override
  public void removeUserFromUserGroup(String userId, String groupId) {
    throw new UnsupportedOperationException("Method Not implemented yet");
  }

  @Override
  public List<ResourceAccessVO> getResourcesByUser(String userId) {
    GroupInfo[] groupInfos = keycloakConnectorService.getGroupsByUser(userId);
    List<ResourceAccessVO> result = new ArrayList<>();
    if (null != groupInfos && groupInfos.length > 0) {
      for (GroupInfo group : groupInfos) {
        //name has the format <ResourceName>-<ResourceId>-<RoleName>
        if (!StringUtils.isBlank(group.getName())) {
          String name = group.getName();
          String[] splittedName = name.split("-");
          ResourceAccessVO resourceAccessVO = new ResourceAccessVO();
          resourceAccessVO.setResource(ResourceEnum.fromValue(splittedName[0]));
          resourceAccessVO.setId(Long.valueOf(splittedName[1]));
          resourceAccessVO.setRole(SecurityRoleEnum.fromValue(splittedName[2]));
          result.add(resourceAccessVO);
        }
      }
    }
    return result;
  }


}
