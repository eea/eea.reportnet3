package org.eea.ums.service.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.security.jwt.data.CacheTokenVO;
import org.eea.security.jwt.data.TokenDataVO;
import org.eea.security.jwt.utils.JwtTokenProvider;
import org.eea.ums.mapper.GroupInfoMapper;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.model.TokenInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.eea.ums.service.vo.UserVO;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * The Class KeycloakSecurityProviderInterfaceService.
 */
@Service
public class KeycloakSecurityProviderInterfaceService implements SecurityProviderInterfaceService {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG =
      LoggerFactory.getLogger(KeycloakSecurityProviderInterfaceService.class);
  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");
  /**
   * The Constant APIKEYS.
   */
  private static final String API_KEYS = "ApiKeys";
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
   * The security redis template.
   */
  @Autowired
  @Qualifier("securityRedisTemplate")
  private RedisTemplate<String, CacheTokenVO> securityRedisTemplate;

  /**
   * The jwt token provider.
   */
  @Autowired
  private JwtTokenProvider jwtTokenProvider;


  /** The users. */
  private List<UserRepresentation> users;

  /**
   * Inits the.
   */
  @PostConstruct
  private void init() {
    users = new ArrayList<>(Arrays.asList(keycloakConnectorService.getUsers()));
  }

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
      tokenVO.setAccessToken(addTokenInfoToCache(tokenVO, tokenInfo.getRefreshExpiresIn()));
    }

    LOG.info("User {} logged in and cached succesfully ", username);
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
      tokenVO.setAccessToken(addTokenInfoToCache(tokenVO, tokenInfo.getRefreshExpiresIn()));
      LOG.info("User {} logged in and cached succesfully", tokenVO.getPreferredUsername());
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
      tokenVO.setAccessToken(addTokenInfoToCache(tokenVO, tokenInfo.getRefreshExpiresIn()));
      LOG.info("Session for User {} renewed and cached succesfully",
          tokenVO.getPreferredUsername());
    }

    return tokenVO;
  }


  /**
   * Do logout.
   *
   * @param authToken the auth token
   */
  @Override
  public void doLogout(String authToken) {
    keycloakConnectorService.logout(authToken);
    LOG.info("Auth token {} logged out and removed from cache succesfully", authToken);
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
    GroupInfo[] groups = keycloakConnectorService.getGroupsWithSearch(groupName);
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
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void createResourceInstance(ResourceInfoVO resourceInfoVO) throws EEAException {
    GroupInfo groupInfo = new GroupInfo();
    String groupName =
        ResourceGroupEnum.fromResourceTypeAndSecurityRole(resourceInfoVO.getResourceTypeEnum(),
            resourceInfoVO.getSecurityRoleEnum()).getGroupName(resourceInfoVO.getResourceId());
    groupInfo.setName(groupName);
    groupInfo.setPath("/" + groupName);
    groupInfo.setAttributes(resourceInfoVO.getAttributes());
    keycloakConnectorService.createGroupDetail(groupInfo);
    LOG.info("Resource {} created succesfully", resourceInfoVO);

  }

  /**
   * Delete resource instances.
   *
   * @param resourceInfoVO the resource info VO
   */
  @Override
  @Async
  public void deleteResourceInstances(List<ResourceInfoVO> resourceInfoVO) {
    // Recover the resource names so they can be removed in the generic way.
    List<String> resourceNames =
        resourceInfoVO.stream().map(ResourceInfoVO::getName).collect(Collectors.toList());
    if (null != resourceNames && !resourceNames.isEmpty()) {
      deleteResourceInstancesByName(resourceNames);
      LOG.info("Resources {} removed succesfully", resourceInfoVO);
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
      List<GroupInfo> groups = new ArrayList<>();
      for (String resource : resourceName) {
        GroupInfo[] groupCall = keycloakConnectorService.getGroupsWithSearch(resource);
        if (null != groupCall && groupCall.length > 0) {
          groups.addAll(Arrays.asList(groupCall));
        }
      }
      if (!groups.isEmpty()) {
        groups.stream().filter(groupInfo -> resources.containsKey(groupInfo.getName()))
            .forEach(groupInfo -> resources.put(groupInfo.getName(), groupInfo.getId()));
        // Removing groups one by one
        for (String resource : resources.values()) {
          keycloakConnectorService.deleteGroupDetail(resource);
        }
        LOG.info("Resources {} removed succesfully", resources);
      }

    }
  }

  /**
   * Delete resource instances containing the ID in the name.
   * <p>
   * Example: Dataflow-1-DATA_CUSTODIAN and Dataflow-1-LEAD_REPORTER would be deleted if the list
   * contains the ID 1.
   * </p>
   *
   * @param datasetIds the dataset ids
   */
  @Override
  public void deleteResourceInstancesByDatasetId(List<Long> datasetIds) {
    if (datasetIds != null && !datasetIds.isEmpty()) {
      Set<Long> set = new HashSet<>(datasetIds);
      List<GroupInfo> groups = new ArrayList<>();
      for (Long datasetId : datasetIds) {
        GroupInfo[] groupCall = keycloakConnectorService.getGroupsWithSearch(datasetId.toString());
        if (null != groupCall && groupCall.length > 0) {
          groups.addAll(Arrays.asList(groupCall));
        }
      }
      for (int i = 0; i < groups.size(); i++) {
        if (set.contains(Long.parseLong(groups.get(i).getName().split("-")[1]))) {
          keycloakConnectorService.deleteGroupDetail(groups.get(i).getId());
          LOG.info("Group {} with id {} deleted", groups.get(i).getName(), groups.get(i).getId());
        }
      }
    }
  }

  /**
   * Adds the user to user group.
   *
   * @param userId the user resourceId
   * @param groupName the group name
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void addUserToUserGroup(String userId, String groupName) throws EEAException {
    // Retrieve the groups available in keycloak. Keycloak does not support queries on groups
    GroupInfo[] groups = keycloakConnectorService.getGroupsWithSearch(groupName);
    if (null != groups && groups.length > 0) {
      // Retrieve the group id of the group where the user will be added
      String groupId = Arrays.asList(groups).stream()
          .filter(groupInfo -> groupName.equalsIgnoreCase(groupInfo.getName()))
          .map(GroupInfo::getId).findFirst().orElse("");
      // Finally add the user to the group
      if (StringUtils.isNotBlank(groupId)) {
        keycloakConnectorService.addUserToGroup(userId, groupId);
        LOG.info("User {} added to group {} succesfully", userId, groupName);
      }
    }

  }

  /**
   * Removes the user from user group.
   *
   * @param userId the user userId
   * @param groupName the group name
   * @throws EEAException the EEA exception
   */
  @Override
  public void removeUserFromUserGroup(String userId, String groupName) throws EEAException {
    GroupInfo[] groups = keycloakConnectorService.getGroupsWithSearch(groupName);
    if (null != groups && groups.length > 0) {
      keycloakConnectorService.removeUserFromGroup(userId, groups[0].getId());
      LOG.info("User {} remove to group {} succesfully", userId, groupName);
    }
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
          // we check if the 2 value of array is a number, if not , we dont asing
          if (isNumeric(splittedName[1])) {
            ResourceAccessVO resourceAccessVO = new ResourceAccessVO();
            resourceAccessVO.setResource(ResourceTypeEnum.fromValue(splittedName[0]));
            resourceAccessVO.setId(Long.valueOf(splittedName[1]));
            resourceAccessVO.setRole(SecurityRoleEnum.fromValue(splittedName[2]));
            result.add(resourceAccessVO);
          }
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


    List<ResourceInfoVO> resourceReturn = new ArrayList<>();
    // ge create the resource that we are looking for it to filter
    String resourceToContain = resourceType + "-" + idResource.toString() + "-";
    // we get the groups
    GroupInfo[] groups = keycloakConnectorService.getGroupsWithSearch(resourceToContain);

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
   * @param contributor the contributor
   * @param userMail the user mail
   * @param groupName the group name
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void addContributorToUserGroup(Optional<UserRepresentation> contributor, String userMail,
      String groupName) throws EEAException {
    if (!contributor.isPresent()) {
      synchronized (users) {
        contributor = users.stream()
            .filter(
                user -> StringUtils.isNotBlank(user.getEmail()) && user.getEmail().equals(userMail))
            .findFirst();
        if (!contributor.isPresent()) {
          // just in case the user was not found in
          users.clear();
          users.addAll(Arrays.asList(keycloakConnectorService.getUsers()));
          contributor = users.stream().filter(
              user -> StringUtils.isNotBlank(user.getEmail()) && user.getEmail().equals(userMail))
              .findFirst();
        }
      }
    }
    if (contributor.isPresent()) {
      LOG.info("New contributor, the email and the group to be assigned is: {}, {}",
          contributor.get().getEmail(), groupName);
      this.addUserToUserGroup(contributor.get().getId(), groupName);
    } else {
      LOG.error("Contributor is not present. The userMail is {} and the group name {}", userMail,
          groupName);
      throw new EEAException(String.format(
          "Error, user with mail %s not found and it was impossible to add it to the group %s",
          userMail, groupName));
    }
  }


  /**
   * Adds the contributor to user group.
   *
   * @param contributor the contributor
   * @param userMail the user mail
   * @param groupName the group name
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void removeContributorFromUserGroup(Optional<UserRepresentation> contributor,
      String userMail, String groupName) throws EEAException {
    if (!contributor.isPresent()) {
      synchronized (users) {
        contributor = users.stream()
            .filter(
                user -> StringUtils.isNotBlank(user.getEmail()) && user.getEmail().equals(userMail))
            .findFirst();

      }
    }
    if (contributor.isPresent()) {
      LOG.info("remove contributor to user group, the email and the group to be removed is: {}, {}",
          contributor.get().getEmail(), groupName);
      this.removeUserFromUserGroup(contributor.get().getId(), groupName);
    } else {
      LOG.error("Contributor is not present. The userMail is {} and the group name {}", userMail,
          groupName);
      throw new EEAException("Error, user not found");
    }

  }


  /**
   * Adds the contributors to user group.
   *
   * @param resources the resources
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void addContributorsToUserGroup(List<ResourceAssignationVO> resources)
      throws EEAException {

    List<UserRepresentation> contributors = new ArrayList<>();
    int cont = 0;

    for (ResourceAssignationVO resourceAssignationVO : resources) {
      Optional<UserRepresentation> contributor = Optional.empty();
      synchronized (users) {
        contributor = users.stream().filter(user -> StringUtils.isNotBlank(user.getEmail())
            && user.getEmail().equals(resourceAssignationVO.getEmail())).findFirst();
        if (contributor.isPresent()) {
          contributors.add(contributor.get());
        } else {
          users.clear(); // just in case the user was not found in
          users.addAll(Arrays.asList(keycloakConnectorService.getUsers()));

          // first try because it was a new user
          contributor = users.stream().filter(user -> StringUtils.isNotBlank(user.getEmail())
              && user.getEmail().equals(resourceAssignationVO.getEmail())).findFirst();
          if (contributor.isPresent()) {
            contributors.add(contributor.get());
          }
        }
      }
      try {

        addContributorToUserGroup(contributor, resources.get(cont).getEmail(), resources.get(cont)
            .getResourceGroup().getGroupName(resources.get(cont).getResourceId()));
      } catch (EEAException e) {
        for (int j = 0; j < resources.subList(0, cont).size(); j++) {
          removeUserFromUserGroup(contributors.get(j).getId(),
              resources.get(j).getResourceGroup().getGroupName(resources.get(j).getResourceId()));
        }
        throw e;
      }
      cont++;
    }
  }

  /**
   * Adds the contributors from user group.
   *
   * @param resources the resources
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void removeContributorsFromUserGroup(List<ResourceAssignationVO> resources)
      throws EEAException {

    List<UserRepresentation> contributors = new ArrayList<>();
    int cont = 0;
    for (ResourceAssignationVO resourceAssignationVO : resources) {
      Optional<UserRepresentation> contributor = Optional.empty();
      synchronized (users) {
        contributor = users.stream().filter(user -> StringUtils.isNotBlank(user.getEmail())
            && user.getEmail().equals(resourceAssignationVO.getEmail())).findFirst();
      }
      if (contributor.isPresent()) {
        contributors.add(contributor.get());
      }
      try {

        removeContributorFromUserGroup(contributor, resources.get(cont).getEmail(), resources
            .get(cont).getResourceGroup().getGroupName(resources.get(cont).getResourceId()));
      } catch (EEAException e) {
        for (int j = 0; j < resources.subList(0, cont).size(); j++) {
          addUserToUserGroup(contributors.get(j).getId(),
              resources.get(j).getResourceGroup().getGroupName(resources.get(j).getResourceId()));
        }
        throw e;
      }
      cont++;
    }
  }


  /**
   * Creates the resource instance.
   *
   * @param resourceInfoVOs the resource info V os
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void createResourceInstance(List<ResourceInfoVO> resourceInfoVOs) throws EEAException {
    for (int i = 0; i < resourceInfoVOs.size(); i++) {
      GroupInfo groupInfo = new GroupInfo();
      String groupName = ResourceGroupEnum
          .fromResourceTypeAndSecurityRole(resourceInfoVOs.get(i).getResourceTypeEnum(),
              resourceInfoVOs.get(i).getSecurityRoleEnum())
          .getGroupName(resourceInfoVOs.get(i).getResourceId());
      groupInfo.setName(groupName);
      groupInfo.setPath("/" + groupName);
      groupInfo.setAttributes(resourceInfoVOs.get(i).getAttributes());
      try {
        keycloakConnectorService.createGroupDetail(groupInfo);
      } catch (EEAException e) {
        LOG_ERROR.error("Error creating group {} due to reason {}", groupInfo.getName(),
            e.getMessage(), e);
        deleteResourceInstances(resourceInfoVOs.subList(0, i));
        throw e;
      }
    }

  }

  /**
   * Authenticate api key.
   *
   * @param apiKey the api key
   *
   * @return the token VO
   */
  @Override
  @Cacheable(value = "api_key")
  public TokenVO authenticateApiKey(String apiKey) {
    List<UserRepresentation> userRepresentations = new ArrayList<>();
    Long dataflowId = 0l;

    for (UserRepresentation userRepresentation : keycloakConnectorService.getUsers()) {
      if (null != userRepresentation.getAttributes()
          && 1 <= userRepresentation.getAttributes().size()
          && userRepresentation.getAttributes().containsKey(API_KEYS)) {
        List<String> apiKeys = userRepresentation.getAttributes().get(API_KEYS);
        // an api key in attributes is represented as a string where positions are:
        // ApiKeyValue,dataflowId,dataproviderId

        String userApiKey =
            apiKeys.stream().filter(value -> value.startsWith(apiKey)).findFirst().orElse("");
        if (StringUtils.isNotEmpty(userApiKey)) {

          String[] apiKeyValues = userApiKey.split(",");
          dataflowId = Long.valueOf(apiKeyValues[1]);
          userRepresentations.add(userRepresentation);
        }
      }
    }
    TokenVO tokenVO = null;
    if (1 == userRepresentations.size()) {
      UserRepresentation user = userRepresentations.get(0);
      LOG.info("Found user {} with api key {}", user.getUsername(), apiKey);
      tokenVO = new TokenVO();
      tokenVO.setUserId(user.getId());
      Set<String> userGroups = new HashSet<>();
      for (GroupInfo groupInfo : keycloakConnectorService.getGroupsByUser(user.getId())) {
        if (groupInfo.getName()
            .equals(ResourceGroupEnum.DATAFLOW_LEAD_REPORTER.getGroupName(dataflowId))
            || groupInfo.getName()
                .equals(ResourceGroupEnum.DATAFLOW_REPORTER_READ.getGroupName(dataflowId))
            || groupInfo.getName()
                .equals(ResourceGroupEnum.DATAFLOW_REPORTER_WRITE.getGroupName(dataflowId))
            || groupInfo.getName()
                .equals(ResourceGroupEnum.DATAFLOW_EDITOR_WRITE.getGroupName(dataflowId))) {
          userGroups.add(groupInfo.getName());
        }
      }
      tokenVO.setGroups(userGroups);
      tokenVO.setRoles(Arrays.asList(keycloakConnectorService.getUserRoles(user.getId())).stream()
          .map(RoleRepresentation::getName).collect(Collectors.toSet()));

      tokenVO.setPreferredUsername(user.getUsername());
      LOG.info("User {} logged in and cached succesfully via api key {}",
          tokenVO.getPreferredUsername(), apiKey);
    } else {
      LOG_ERROR.error("{} users found with api key {} ", userRepresentations.size(), apiKey);
    }

    return tokenVO;
  }

  /**
   * Authenticate email.
   *
   * @param email the email
   * @return the token VO
   */
  @Override
  public TokenVO authenticateEmail(String email) {
    List<UserRepresentation> userRepresentations = new ArrayList<>();

    synchronized (this.users) {
      int usersReload = 0;

      do {
        for (UserRepresentation userRepresentation : this.users) {
          if (email.equals(userRepresentation.getEmail())) {
            userRepresentations.add(userRepresentation);
            usersReload = 2;
            break;
          }
        }
        if (userRepresentations.size() == 0) {
          usersReload++;
          if (usersReload < 2) { // ensure that there is only one invocation to Keycloak to retrieve
                                 // users
            users.clear(); // just in case the user was not found in
            users.addAll(Arrays.asList(keycloakConnectorService.getUsers()));
          }
        }
      } while (usersReload < 2);
    }
    TokenVO tokenVO = null;
    if (1 == userRepresentations.size()) {
      UserRepresentation user = userRepresentations.get(0);
      LOG.info("Found user {} with email {}", user.getUsername(), email);
      tokenVO = new TokenVO();
      tokenVO.setUserId(user.getId());
      tokenVO.setPreferredUsername(user.getUsername());
      Set<String> userGroups = new HashSet<>();
      for (GroupInfo groupInfo : keycloakConnectorService.getGroupsByUser(user.getId())) {
        userGroups.add(groupInfo.getName());
      }
      tokenVO.setGroups(userGroups);

      tokenVO.setRoles(Arrays.asList(keycloakConnectorService.getUserRoles(user.getId())).stream()
          .map(RoleRepresentation::getName).collect(Collectors.toSet()));

      LOG.info("User {} logged in and cached succesfully via email {}",
          tokenVO.getPreferredUsername(), email);
    } else {
      LOG_ERROR.error("{} users found with email {} ", userRepresentations.size(), email);
    }

    return tokenVO;
  }


  /**
   * Creates the api key.
   *
   * @param userId the user id
   * @param dataflowId the dataflow id
   * @param dataProvider the data provider
   *
   * @return the string
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public String createApiKey(String userId, Long dataflowId, Long dataProvider)
      throws EEAException {
    UserRepresentation user = keycloakConnectorService.getUser(userId);
    if (user == null) {
      throw new EEAException(String.format(EEAErrorMessage.USER_NOTFOUND, userId));
    }
    // Create new uuid for the new key
    String apiKey = UUID.randomUUID().toString();
    // Initialize the attributes
    Map<String, List<String>> attributes =
        user.getAttributes() != null ? user.getAttributes() : new HashMap<>();
    List<String> apiKeys =
        attributes.get(API_KEYS) != null ? attributes.get(API_KEYS) : new ArrayList<>();
    String newValueAttribute = dataflowId + "," + dataProvider;
    // Find and remove old key
    if (!apiKeys.isEmpty()) {
      for (String keyString : apiKeys) {
        if (keyString.contains(newValueAttribute)) {
          apiKeys.remove(keyString);
          break;
        }
      }
    }
    apiKeys.add(apiKey + "," + newValueAttribute);
    attributes.put(API_KEYS, apiKeys);
    user.setAttributes(attributes);
    keycloakConnectorService.updateUser(user);
    return apiKey;
  }

  /**
   * Gets the api key.
   *
   * @param userId the user id
   * @param dataflowId the dataflow id
   * @param dataProvider the data provider
   *
   * @return the api key
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public String getApiKey(String userId, Long dataflowId, Long dataProvider) throws EEAException {
    UserRepresentation user = keycloakConnectorService.getUser(userId);
    if (user == null) {
      throw new EEAException(String.format(EEAErrorMessage.USER_NOTFOUND, userId));
    }
    String result = "";
    // Initialize the attributes
    Map<String, List<String>> attributes =
        user.getAttributes() != null ? user.getAttributes() : new HashMap<>();
    List<String> apiKeys =
        attributes.get(API_KEYS) != null ? attributes.get(API_KEYS) : new ArrayList<>();
    String findValue = "," + dataflowId + "," + dataProvider;
    if (!apiKeys.isEmpty()) {
      for (String keyString : apiKeys) {
        if (keyString.contains(findValue)) {
          result = keyString.replace(findValue, "");
          break;
        }
      }
    }
    return result;

  }


  /**
   * Gets the user without keys.
   *
   * @param userId the user id
   *
   * @return the user without keys
   */
  @Override
  public UserRepresentation getUserWithoutKeys(String userId) {
    UserRepresentation user = keycloakConnectorService.getUser(userId);
    if (user != null && user.getAttributes() != null) {
      user.getAttributes().remove(API_KEYS);
    }
    return user;
  }

  /**
   * Sets the attributes with ApiKeys.
   *
   * @param user the user
   * @param attributes the attributes
   *
   * @return the user representation
   */
  @Override
  public UserRepresentation setAttributesWithApiKey(UserRepresentation user,
      Map<String, List<String>> attributes) {
    if (user.getAttributes() != null && user.getAttributes().get(API_KEYS) != null) {
      attributes.put(API_KEYS, user.getAttributes().get(API_KEYS));
    }
    user.setAttributes(attributes);
    return user;
  }

  /**
   * Map token to VO.
   *
   * @param tokenInfo the token info
   *
   * @return the token VO
   */

  private TokenVO mapTokenToVO(TokenInfo tokenInfo) {
    TokenDataVO token = null;
    TokenVO tokenVO = new TokenVO();
    try {
      token = jwtTokenProvider.parseToken(tokenInfo.getAccessToken());
    } catch (VerificationException e) {
      LOG_ERROR.error("Error trying to parse token", e);
    }

    if (null != token) {
      Set<String> eeaGroups = new HashSet<>();
      Optional.ofNullable(token.getOtherClaims())
          .map(claims -> (List<String>) claims.get("user_groups"))
          .filter(groups -> !groups.isEmpty()).ifPresent(groups -> groups.stream().map(group -> {
            if (group.startsWith("/")) {
              group = group.substring(1);
            }
            return group.toUpperCase();
          }).forEach(eeaGroups::add));

      tokenVO.setRoles(token.getRoles());
      tokenVO.setRefreshToken(tokenInfo.getRefreshToken());
      tokenVO.setGroups(eeaGroups);
      tokenVO.setPreferredUsername(token.getPreferredUsername());
      tokenVO.setAccessTokenExpiration(token.getExpiration());
      tokenVO.setUserId(token.getUserId());
      tokenVO.setAccessToken(tokenInfo.getAccessToken());
    }
    return tokenVO;
  }

  /**
   * Adds the token info to cache.
   *
   * @param tokenVO the token VO
   * @param cacheExpireIn the cache expire in
   *
   * @return the string
   */
  private String addTokenInfoToCache(TokenVO tokenVO, Long cacheExpireIn) {
    CacheTokenVO cacheTokenVO = new CacheTokenVO();
    cacheTokenVO.setAccessToken(tokenVO.getAccessToken());
    cacheTokenVO.setRefreshToken(tokenVO.getRefreshToken());
    cacheTokenVO.setExpiration(tokenVO.getAccessTokenExpiration());
    String key = UUID.randomUUID().toString();
    securityRedisTemplate.opsForValue().set(key, cacheTokenVO, cacheExpireIn, TimeUnit.SECONDS);
    return key;
  }

  /**
   * Checks if is numeric.
   *
   * @param cadena the cadena
   * @return true, if is numeric
   */
  private static boolean isNumeric(String cadena) {
    boolean resultado;
    try {
      Integer.parseInt(cadena);
      resultado = true;
    } catch (NumberFormatException excepcion) {
      resultado = false;
    }

    return resultado;
  }
}
