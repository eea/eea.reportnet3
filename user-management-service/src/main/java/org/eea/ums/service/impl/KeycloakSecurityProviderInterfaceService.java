package org.eea.ums.service.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.ums.mapper.GroupInfoMapper;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.model.TokenInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.eea.ums.service.vo.UserVO;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class KeycloakSecurityProviderInterfaceService.
 */
@Service
public class KeycloakSecurityProviderInterfaceService implements SecurityProviderInterfaceService {


  /**
   * The keycloak connector service.
   */
  @Autowired
  private KeycloakConnectorService keycloakConnectorService;

  /**
   * The group info mapper.
   */
  @Autowired
  private GroupInfoMapper groupInfoMapper;

  /**
   * Do login.
   *
   * @param username the username
   * @param password the password
   * @param extraParams the extra params
   *
   * @return the token VO
   */
  @Override
  public TokenVO doLogin(String username, String password, Object... extraParams) {
    TokenInfo tokenInfo = null;
    Boolean isAdminToken = null != extraParams && extraParams.length > 0 && null != extraParams[0]
        && extraParams[0] instanceof Boolean ? (Boolean) extraParams[0] : Boolean.FALSE;
    if (isAdminToken) {
      tokenInfo = keycloakConnectorService.generateAdminToken(username, password);
    } else {
      tokenInfo = keycloakConnectorService.generateToken(username, password);
    }

    TokenVO tokenVO = null;
    if (null != tokenInfo) {
      tokenVO = mapTokenToVO(tokenInfo);
    }
    return tokenVO;
  }

  /**
   * Do login.
   *
   * @param code the code
   *
   * @return the token VO
   */
  @Override
  public TokenVO doLogin(String code) {
    TokenInfo tokenInfo = keycloakConnectorService.generateToken(code);
    TokenVO tokenVO = null;
    if (null != tokenInfo) {
      tokenVO = mapTokenToVO(tokenInfo);
    }
    return tokenVO;
  }

  /**
   * Refresh token.
   *
   * @param refreshToken the refresh token
   *
   * @return the token VO
   */
  @Override
  public TokenVO refreshToken(String refreshToken) {
    TokenInfo tokenInfo = keycloakConnectorService.refreshToken(refreshToken);
    TokenVO tokenVO = null;
    if (null != tokenInfo) {
      tokenVO = mapTokenToVO(tokenInfo);
    }
    return tokenVO;
  }

  /**
   * Map token to VO.
   *
   * @param tokenInfo the token info
   *
   * @return the token VO
   */
  private TokenVO mapTokenToVO(TokenInfo tokenInfo) {
    TokenVO tokenVO = new TokenVO();
    tokenVO.setAccessToken(tokenInfo.getAccessToken());
    tokenVO.setRefreshToken(tokenInfo.getRefreshToken());
    return tokenVO;
  }

  /**
   * Do logout.
   *
   * @param refreshToken the refresh token
   */
  @Override
  public void doLogout(String refreshToken) {
    keycloakConnectorService.logout(refreshToken);
  }

  /**
   * Gets the group detail.
   *
   * @param groupName the group name
   *
   * @return the group detail
   */
  @Override
  public ResourceInfoVO getResourceDetails(String groupName) {
    GroupInfo[] groups = keycloakConnectorService.getGroups();
    String groupId = "";
    ResourceInfoVO result = new ResourceInfoVO();
    if (null != groups && groups.length > 0) {
      groupId = Arrays.asList(groups).stream()
          .filter(groupInfo -> groupName.equalsIgnoreCase(groupInfo.getName()))
          .map(GroupInfo::getId).findFirst().orElse("");
    }
    if (StringUtils.isNotBlank(groupId)) {
      result = this.groupInfoMapper.entityToClass(keycloakConnectorService.getGroupDetail(groupId));
      // Group name has the format <ResourceType>-<idResource>-<userRole>, for instance
      // Dataschema-1-DATA_REQUESTER
      String[] splittedGroupName = groupName.split("-");
      String resourceType = splittedGroupName[0];
      String resourceId = splittedGroupName[1];
      String role = splittedGroupName[2];
      result.setResourceTypeEnum(ResourceTypeEnum.fromValue(resourceType));
      result.setSecurityRoleEnum(SecurityRoleEnum.fromValue(role));
      result.setResourceId(Long.valueOf(resourceId));
    }
    return result;
  }

  /**
   * Check access permission.
   *
   * @param resource the resource
   * @param scopes the scopes
   *
   * @return the boolean
   */
  @Override
  public Boolean checkAccessPermission(String resource, AccessScopeEnum... scopes) {
    return !keycloakConnectorService.checkUserPermision(resource, scopes).equals("DENY");
  }

  /**
   * Gets the users.
   *
   * @param userId the user resourceId
   *
   * @return the users
   */
  @Override
  public List<UserVO> getUsers(@Nullable String userId) {
    throw new UnsupportedOperationException("Method Not implemented yet");
  }

  /**
   * Creates the resource instance.
   *
   * @param resourceInfoVO the resource info vo
   */
  @Override
  public void createResourceInstance(ResourceInfoVO resourceInfoVO) {
    GroupInfo groupInfo = new GroupInfo();
    String groupName =
        ResourceGroupEnum.fromResourceTypeAndSecurityRole(resourceInfoVO.getResourceTypeEnum(),
            resourceInfoVO.getSecurityRoleEnum()).getGroupName(resourceInfoVO.getResourceId());
    groupInfo.setName(groupName);
    groupInfo.setPath("/" + groupName);
    groupInfo.setAttributes(resourceInfoVO.getAttributes());
    keycloakConnectorService.createGroupDetail(groupInfo);
  }

  /**
   * Delete resource instances.
   *
   * @param resourceInfoVO the resource info VO
   */
  @Override
  public void deleteResourceInstances(List<ResourceInfoVO> resourceInfoVO) {
    // Recover the resource names so they can be removed in the generic way.
    List<String> resourceNames =
        resourceInfoVO.stream().map(ResourceInfoVO::getName).collect(Collectors.toList());
    if (null != resourceNames && !resourceNames.isEmpty()) {
      deleteResourceInstancesByName(resourceNames);

    }
  }

  /**
   * Delete resource instances by name.
   *
   * @param resourceName the resource name
   */
  @Override
  public void deleteResourceInstancesByName(List<String> resourceName) {
    // Initialize the map of resouces along with empty string where later on the GroupId will be
    // placed
    Map<String, String> resources =
        resourceName.stream().collect(Collectors.toMap(Function.identity(), x -> ""));
    if (null != resources && resources.size() > 0) {
      // Once recovered all the group names from input, get the group names from Keycloak to
      // determine which ones must be removed
      GroupInfo[] groups = keycloakConnectorService.getGroups();
      if (null != groups && groups.length > 0) {
        Arrays.asList(groups).stream()
            .filter(groupInfo -> resources.containsKey(groupInfo.getName()))
            .forEach(groupInfo -> resources.put(groupInfo.getName(), groupInfo.getId()));
        // Removing groups one by one
        resources.values().stream()
            .forEach(groupId -> keycloakConnectorService.deleteGroupDetail(groupId));
      }

    }
  }

  /**
   * Adds the user to user group.
   *
   * @param userId the user resourceId
   * @param groupName the group name
   */
  @Override
  public void addUserToUserGroup(String userId, String groupName) {
    // Retrieve the groups available in keycloak. Keycloak does not support queries on groups
    GroupInfo[] groups = keycloakConnectorService.getGroups();
    if (null != groups && groups.length > 0) {
      // Retrieve the group id of the group where the user will be added
      String groupId = Arrays.asList(groups).stream()
          .filter(groupInfo -> groupName.equalsIgnoreCase(groupInfo.getName()))
          .map(GroupInfo::getId).findFirst().orElse("");
      // Finally add the user to the group
      if (StringUtils.isNotBlank(groupId)) {
        keycloakConnectorService.addUserToGroup(userId, groupId);
      }
    }

  }

  /**
   * Removes the user from user group.
   *
   * @param userId the user resourceId
   * @param groupId the group resourceId
   */
  @Override
  public void removeUserFromUserGroup(String userId, String groupId) {
    throw new UnsupportedOperationException("Method Not implemented yet");
  }

  /**
   * Gets the resources by user.
   *
   * @param userId the user resourceId
   *
   * @return the resources by user
   */
  @Override
  public List<ResourceAccessVO> getResourcesByUser(String userId) {
    GroupInfo[] groupInfos = keycloakConnectorService.getGroupsByUser(userId);
    List<ResourceAccessVO> result = new ArrayList<>();
    if (null != groupInfos && groupInfos.length > 0) {
      for (GroupInfo group : groupInfos) {
        // name has the format <ResourceName>-<ResourceId>-<RoleName>
        if (!StringUtils.isBlank(group.getName())) {
          String name = group.getName();
          String[] splittedName = name.split("-");
          ResourceAccessVO resourceAccessVO = new ResourceAccessVO();
          resourceAccessVO.setResource(ResourceTypeEnum.fromValue(splittedName[0]));
          resourceAccessVO.setId(Long.valueOf(splittedName[1]));
          resourceAccessVO.setRole(SecurityRoleEnum.fromValue(splittedName[2]));
          result.add(resourceAccessVO);
        }
      }
    }
    return result;
  }

  /**
   * Gets the groups by id resource type.
   *
   * @param idResource the id resource
   * @param resourceType the resource type
   *
   * @return the groups by id resource type
   */
  @Override
  public List<ResourceInfoVO> getGroupsByIdResourceType(Long idResource,
      ResourceTypeEnum resourceType) {
    // we get all groups
    GroupInfo[] groups = keycloakConnectorService.getGroups();
    List<ResourceInfoVO> resourceReturn = new ArrayList<>();
    // ge create the resource that we are looking for it to filter
    String resourceToContain = resourceType + "-" + idResource.toString() + "-";

    // we do a for and find the data that we need
    if (null != groups && groups.length > 0) {
      Arrays.asList(groups).stream().forEach(groupInfo -> {
        if (groupInfo.getName().contains(resourceToContain)) {
          ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
          resourceInfoVO.setName(groupInfo.getName());
          resourceInfoVO.setResourceId(idResource);
          resourceInfoVO.setResourceTypeEnum(resourceType);
          resourceInfoVO.setPath(groupInfo.getPath());
          resourceInfoVO.setAttributes(groupInfo.getAttributes());
          resourceReturn.add(resourceInfoVO);
        }
      });

    }

    return resourceReturn;
  }

  /**
   * Adds the contributor to user group.
   *
   * @param userMail the user mail
   * @param groupName the group name
   * @throws EEAException the EEA exception
   */
  @Override
  public void addContributorToUserGroup(String userMail, String groupName) throws EEAException {
    UserRepresentation[] users = keycloakConnectorService.getUsers();
    Optional<UserRepresentation> contributor = Arrays.asList(users).stream()
        .filter(user -> StringUtils.isNotBlank(user.getEmail()) && user.getEmail().equals(userMail))
        .findFirst();
    contributor.orElseThrow(() -> new EEAException("Error, user not found"));

    this.addUserToUserGroup(contributor.get().getId(), groupName);

  }


  /**
   * Creates the resource instance.
   *
   * @param resourceInfoVOs the resource info V os
   */
  @Override
  public void createResourceInstance(List<ResourceInfoVO> resourceInfoVOs) {
    for (ResourceInfoVO resourceInfoVO : resourceInfoVOs) {
      GroupInfo groupInfo = new GroupInfo();
      String groupName =
          ResourceGroupEnum
              .fromResourceTypeAndSecurityRole(resourceInfoVO.getResourceTypeEnum(),
                  resourceInfoVO.getSecurityRoleEnum())
              .getGroupName(resourceInfoVO.getResourceId());
      groupInfo.setName(groupName);
      groupInfo.setPath("/" + groupName);
      groupInfo.setAttributes(resourceInfoVO.getAttributes());
      keycloakConnectorService.createGroupDetail(groupInfo);
    }
  }


}
