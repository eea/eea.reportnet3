package org.eea.ums.service.keycloak.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.eea.ums.service.keycloak.admin.TokenMonitor;
import org.eea.ums.service.keycloak.model.CheckResourcePermissionResult;
import org.eea.ums.service.keycloak.model.ClientInfo;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.model.ResourceInfo;
import org.eea.ums.service.keycloak.model.TokenInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * The Class KeycloakConnectorServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class KeycloakConnectorServiceImplTest {

  /** The keycloak connector service. */
  @InjectMocks
  private KeycloakConnectorServiceImpl keycloakConnectorService;

  /** The rest template. */
  @Mock
  private RestTemplate restTemplate;

  /** The token monitor. */
  @Mock
  private TokenMonitor tokenMonitor;


  /**
   * Inits the.
   */
  @Before
  public void init() {
    Map<String, String> resourceTypes = new HashMap<>();
    resourceTypes.put("Dataflow", "reportnet:type:dataflow");
    ReflectionTestUtils.setField(keycloakConnectorService, "resourceTypes", resourceTypes);
    ReflectionTestUtils.setField(keycloakConnectorService, "realmName", "Reportnet");
    ReflectionTestUtils.setField(keycloakConnectorService, "clientId", "reportnet");

    MockitoAnnotations.openMocks(this);
  }

  /**
   * Check user permision.
   */
  @Test
  public void checkUserPermision() {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    CheckResourcePermissionResult body = new CheckResourcePermissionResult();
    body.setStatus("PERMIT");
    ResponseEntity<CheckResourcePermissionResult> checkResult =
        new ResponseEntity<>(body, HttpStatus.OK);
    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(checkResult);
    String result = keycloakConnectorService.checkUserPermision("Dataflow", AccessScopeEnum.CREATE);
    Assert.assertNotNull(result);
    Assert.assertEquals("PERMIT", result);
  }

  /**
   * Generate token.
   */
  @Test
  public void generateToken() {
    TokenInfo body = new TokenInfo();
    body.setAccessToken("JWT");
    ResponseEntity<TokenInfo> result = new ResponseEntity<>(body, HttpStatus.OK);
    Mockito.when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(HttpEntity.class),
        Mockito.any(Class.class))).thenReturn(result);

    TokenInfo token = keycloakConnectorService.generateToken("user1", "1234");
    Assert.assertNotNull(result);
    Assert.assertEquals("JWT", token.getAccessToken());

  }

  /**
   * Generate admin token.
   */
  @Test
  public void generateAdminToken() {
    TokenInfo body = new TokenInfo();
    body.setAccessToken("JWT");
    ResponseEntity<TokenInfo> result = new ResponseEntity<>(body, HttpStatus.OK);
    Mockito.when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(HttpEntity.class),
        Mockito.any(Class.class))).thenReturn(result);

    TokenInfo token = keycloakConnectorService.generateAdminToken("user1", "1234");
    Assert.assertNotNull(result);
    Assert.assertEquals("JWT", token.getAccessToken());

  }

  /**
   * Generate token by code.
   */
  @Test
  public void generateTokenByCode() {
    TokenInfo body = new TokenInfo();
    body.setAccessToken("JWT");
    ResponseEntity<TokenInfo> result = new ResponseEntity<>(body, HttpStatus.OK);
    Mockito.when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(HttpEntity.class),
        Mockito.any(Class.class))).thenReturn(result);

    TokenInfo token = keycloakConnectorService.generateToken("code1");
    Assert.assertNotNull(result);
    Assert.assertEquals("JWT", token.getAccessToken());

  }

  /**
   * Refresh token.
   */
  @Test
  public void refreshToken() {
    TokenInfo body = new TokenInfo();
    body.setAccessToken("JWT");
    ResponseEntity<TokenInfo> result = new ResponseEntity<>(body, HttpStatus.OK);
    Mockito.when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(HttpEntity.class),
        Mockito.any(Class.class))).thenReturn(result);

    TokenInfo token = keycloakConnectorService.refreshToken("1234");
    Assert.assertNotNull(result);
    Assert.assertEquals("JWT", token.getAccessToken());

  }

  /**
   * Gets the reportnet client info.
   *
   * @return the reportnet client info
   */
  @Test
  public void getReportnetClientInfo() {
    ClientInfo info = new ClientInfo();
    info.setClientId("reportnet");
    ClientInfo[] body = new ClientInfo[] {info};

    ResponseEntity<ClientInfo[]> clientInfoResult = new ResponseEntity<>(body, HttpStatus.OK);
    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(clientInfoResult);
    ClientInfo result = ReflectionTestUtils.invokeMethod(keycloakConnectorService,
        "getReportnetClientInfo", "token");
    Assert.assertNotNull(result);

  }

  /**
   * Gets the resource info.
   *
   * @return the resource info
   */
  @Test
  public void getResourceInfo() {

    String[] bodyResourceSet = new String[] {"resource1"};

    ResponseEntity<String[]> resourceSetInfo = new ResponseEntity<>(bodyResourceSet, HttpStatus.OK);

    ResourceInfo bodyResourceInfo = new ResourceInfo();
    bodyResourceInfo.setName("Dataflow");
    ResponseEntity<ResourceInfo> resourceInfo =
        new ResponseEntity<>(bodyResourceInfo, HttpStatus.OK);
    when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class)))
            .then(new Answer<ResponseEntity>() {
              @Override
              public ResponseEntity answer(InvocationOnMock invocation) throws Throwable {
                String url = invocation.getArgument(0);
                if (url.endsWith("resource1")) {
                  return resourceInfo;
                } else {
                  return resourceSetInfo;
                }

              }
            });

    List<ResourceInfo> result =
        ReflectionTestUtils.invokeMethod(keycloakConnectorService, "getResourceInfo", "token");
    Assert.assertNotNull(result);
    Assert.assertTrue(!result.isEmpty());
    Assert.assertEquals("Dataflow", result.get(0).getName());

  }


  /**
   * Gets the groups by user.
   *
   * @return the groups by user
   */
  @Test
  public void getGroupsByUser() {

    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setId("1");
    groupInfo.setName("Dataflow-1-LEAD_REPORTER");
    groupInfo.setPath("/path");
    GroupInfo[] groupInfos = new GroupInfo[] {groupInfo};

    ResponseEntity<GroupInfo[]> responseGroupInfos =
        new ResponseEntity<>(groupInfos, HttpStatus.OK);

    when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(responseGroupInfos);

    GroupInfo[] result = keycloakConnectorService.getGroupsByUser("user1");
    Assert.assertNotNull(result);
    Assert.assertTrue(result.length > 0);
    Assert.assertEquals("Dataflow-1-LEAD_REPORTER", result[0].getName());

  }

  /**
   * Gets the groups by user error.
   *
   * @return the groups by user error
   */
  @Test(expected = RestClientException.class)
  public void getGroupsByUserError() {
    doThrow(new RestClientException("error test")).when(restTemplate).exchange(Mockito.anyString(),
        Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class));

    try {
      keycloakConnectorService.getGroupsByUser("user1");
    } catch (RestClientException e) {
      Assert.assertEquals("error test", e.getMessage());
      throw e;
    }


  }

  /**
   * Logout.
   */
  @Test
  public void logout() {
    keycloakConnectorService.logout("refreshToken");
    Mockito.verify(restTemplate, Mockito.times(1)).postForEntity(Mockito.anyString(),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class));
  }

  /**
   * Logout exception.
   */
  @Test(expected = RestClientException.class)
  public void logoutException() {

    Mockito.doThrow(new RestClientException("error test")).when(restTemplate).postForEntity(
        Mockito.anyString(), Mockito.any(HttpEntity.class), Mockito.any(Class.class));
    try {
      keycloakConnectorService.logout("refreshToken");
    } catch (RestClientException e) {
      Assert.assertEquals("error test", e.getMessage());
      throw e;
    }

  }

  /**
   * Gets the groups.
   *
   * @return the groups
   */
  @Test
  public void getGroups() {
    GroupInfo[] groupInfos = new GroupInfo[1];
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setId("idGroupInfo");
    groupInfo.setName("Dataflow-1-DATA_CUSTODIAN");
    groupInfos[0] = groupInfo;
    ResponseEntity<GroupInfo[]> responseGroupInfo = new ResponseEntity<>(groupInfos, HttpStatus.OK);
    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(responseGroupInfo);
    GroupInfo[] result = keycloakConnectorService.getGroups();
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.length);
    Assert.assertEquals("Dataflow-1-DATA_CUSTODIAN", result[0].getName());
    Assert.assertEquals("idGroupInfo", result[0].getId());
  }

  /**
   * Gets the groups error.
   *
   * @return the groups error
   */
  @Test(expected = RestClientException.class)
  public void getGroupsError() {

    Mockito.doThrow(new RestClientException("error test")).when(restTemplate).exchange(
        Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class),
        Mockito.any(Class.class));
    try {
      keycloakConnectorService.getGroups();
    } catch (RestClientException e) {
      Assert.assertEquals("error test", e.getMessage());
      throw e;
    }
  }

  /**
   * Adds the user to group.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void addUserToGroup() throws EEAException {
    ResponseEntity<Void> responseAddUserToGroup = new ResponseEntity<>(null, HttpStatus.OK);
    Mockito
        .when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
            Mockito.any(HttpEntity.class), Mockito.any(Class.class)))
        .thenReturn(responseAddUserToGroup);
    keycloakConnectorService.addUserToGroup("", "");
    Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.anyString(),
        Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class));

  }

  /**
   * Adds the user to group error.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void addUserToGroupError() throws EEAException {
    Mockito.doThrow(new RestClientException("error test")).when(restTemplate).exchange(
        Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class),
        Mockito.any(Class.class));
    try {
      keycloakConnectorService.addUserToGroup("user1", "");
    } catch (EEAException e) {
      Assert.assertEquals("Permission not created", e.getMessage());
      throw e;
    }
  }

  /**
   * Gets the group detail.
   *
   * @return the group detail
   */
  @Test
  public void getGroupDetail() {
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setId("idGroupInfo");
    groupInfo.setName("Dataflow-1-DATA_CUSTODIAN");
    ResponseEntity<GroupInfo> responseGroupInfo = new ResponseEntity<>(groupInfo, HttpStatus.OK);
    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(responseGroupInfo);
    GroupInfo result = keycloakConnectorService.getGroupDetail("group1");
    Assert.assertNotNull(result);
    Assert.assertEquals("Dataflow-1-DATA_CUSTODIAN", result.getName());
    Assert.assertEquals("idGroupInfo", result.getId());
  }

  /**
   * Gets the group detail error.
   *
   * @return the group detail error
   */
  @Test(expected = RestClientException.class)
  public void getGroupDetailError() {
    Mockito.doThrow(new RestClientException("error test")).when(restTemplate).exchange(
        Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class),
        Mockito.any(Class.class));
    try {
      keycloakConnectorService.getGroupDetail("group1");
    } catch (RestClientException e) {
      Assert.assertEquals("error test", e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the group detail.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createGroupDetail() throws EEAException {
    GroupInfo groupInfo = new GroupInfo();
    ResponseEntity<Void> result = new ResponseEntity<>(null, HttpStatus.OK);

    Mockito.when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(HttpEntity.class),
        Mockito.any(Class.class))).thenReturn(result);

    keycloakConnectorService.createGroupDetail(groupInfo);

    Mockito.verify(restTemplate, Mockito.times(1)).postForEntity(Mockito.anyString(),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class));


  }

  /**
   * Delete group detail.
   */
  @Test
  public void deleteGroupDetail() {
    ResponseEntity<Void> result = new ResponseEntity<>(null, HttpStatus.OK);

    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(result);

    keycloakConnectorService.deleteGroupDetail("");

    Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.anyString(),
        Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class));
  }

  /**
   * Adds the user test.
   */
  @Test
  public void addUserTest() {
    ResponseEntity<Void> result = new ResponseEntity<>(null, HttpStatus.OK);

    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(result);

    keycloakConnectorService.addUser("");

    Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.anyString(),
        Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class));
  }

  /**
   * Adds the role test.
   */
  @Test
  public void addRoleTest() {
    ResponseEntity<Void> result = new ResponseEntity<>(null, HttpStatus.OK);

    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(result);

    keycloakConnectorService.addRole("", "");

    Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.anyString(),
        Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class));
  }

  /**
   * Gets the users test.
   *
   * @return the users test
   */
  @Test
  public void getUsersTest() {
    UserRepresentation[] users = new UserRepresentation[1];
    UserRepresentation user = new UserRepresentation();
    user.setId("idGroupInfo");
    user.setUsername("Dataflow-1-DATA_CUSTODIAN");
    users[0] = user;
    ResponseEntity<UserRepresentation[]> responseGroupInfo =
        new ResponseEntity<>(users, HttpStatus.OK);

    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(responseGroupInfo);

    UserRepresentation[] result = keycloakConnectorService.getUsers();

    Assert.assertNotNull(result);
  }

  /**
   * Gets the roles test.
   *
   * @return the roles test
   */
  @Test
  public void getRolesTest() {
    RoleRepresentation[] roles = new RoleRepresentation[1];
    RoleRepresentation role = new RoleRepresentation();
    role.setId("idGroupInfo");
    role.setName("Dataflow-1-DATA_CUSTODIAN");
    roles[0] = role;
    ResponseEntity<RoleRepresentation[]> responseGroupInfo =
        new ResponseEntity<>(roles, HttpStatus.OK);

    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(responseGroupInfo);

    RoleRepresentation[] result = keycloakConnectorService.getRoles();

    Assert.assertNotNull(result);
  }

  /**
   * Gets the users by email test.
   *
   * @return the users by email test
   */
  @Test
  public void getUsersByEmailTest() {

    ResponseEntity<UserRepresentation[]> responseEntity =
        new ResponseEntity(new UserRepresentation[1], HttpStatus.OK);

    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(), Mockito.any(),
        Mockito.any(Class.class))).thenReturn(responseEntity);

    Assert.assertEquals(1, keycloakConnectorService.getUsersByEmail("sample@email.net").length);
  }

  /**
   * Update user test.
   */
  @Test
  public void updateUserTest() {
    UserRepresentation user = new UserRepresentation();
    user.setId("");
    keycloakConnectorService.updateUser(user);
    Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.anyString(),
        Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class));
  }

  /**
   * Gets the user.
   *
   * @return the user
   */
  @Test
  public void getUser() {
    UserRepresentation user = new UserRepresentation();
    ResponseEntity<UserRepresentation> responseUserRepresentation =
        new ResponseEntity<>(user, HttpStatus.OK);
    Mockito
        .when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
            Mockito.any(HttpEntity.class), Mockito.any(Class.class)))
        .thenReturn(responseUserRepresentation);
    UserRepresentation userRepresentation = keycloakConnectorService.getUser("");
    assertEquals(user, userRepresentation);
  }


  /**
   * Gets the user roles.
   *
   * @return the user roles
   */
  @Test
  public void getUserRoles() {
    RoleRepresentation[] roles = new RoleRepresentation[1];
    RoleRepresentation role = new RoleRepresentation();
    role.setId("idGroupInfo");
    role.setName("Dataflow-1-DATA_CUSTODIAN");
    roles[0] = role;
    ResponseEntity<RoleRepresentation[]> responseGroupInfo =
        new ResponseEntity<>(roles, HttpStatus.OK);

    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(responseGroupInfo);

    RoleRepresentation[] result = keycloakConnectorService.getUserRoles("userId");

    Assert.assertNotNull(result);
  }

  /**
   * Gets the user roles no roles.
   *
   * @return the user roles no roles
   */
  @Test
  public void getUserRolesNoRoles() {
    RoleRepresentation[] roles = new RoleRepresentation[0];
    ResponseEntity<RoleRepresentation[]> responseGroupInfo =
        new ResponseEntity<>(roles, HttpStatus.OK);

    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(responseGroupInfo);

    RoleRepresentation[] result = keycloakConnectorService.getUserRoles("userId");

    Assert.assertNotNull(result);
  }

  /**
   * Gets the groups with search test.
   *
   * @return the groups with search test
   */
  @Test
  public void getGroupsWithSearchTest() {
    GroupInfo[] groupInfo = new GroupInfo[0];
    ResponseEntity<GroupInfo[]> responseGroupInfo = new ResponseEntity<>(groupInfo, HttpStatus.OK);

    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(responseGroupInfo);

    Assert.assertNotNull(keycloakConnectorService.getGroupsWithSearch("value"));
  }

  /**
   * Gets the users by group id test.
   *
   * @return the users by group id test
   */
  @Test
  public void getUsersByGroupIdTest() {
    UserRepresentation[] userRepresentation = new UserRepresentation[0];
    ResponseEntity<UserRepresentation[]> responseUserRepresentation =
        new ResponseEntity<>(userRepresentation, HttpStatus.OK);

    Mockito
        .when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
            Mockito.any(HttpEntity.class), Mockito.any(Class.class)))
        .thenReturn(responseUserRepresentation);

    Assert.assertNotNull(keycloakConnectorService.getUsersByGroupId("value"));
  }


  /**
   * Removes the user from group.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void removeUserFromGroup() throws EEAException {
    ResponseEntity<Void> responseRemoveUserFromGroup = new ResponseEntity<>(null, HttpStatus.OK);
    Mockito
        .when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
            Mockito.any(HttpEntity.class), Mockito.any(Class.class)))
        .thenReturn(responseRemoveUserFromGroup);
    keycloakConnectorService.removeUserFromGroup("", "");
    Mockito.verify(restTemplate, Mockito.times(1)).exchange(Mockito.anyString(),
        Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class));

  }

  /**
   * Removes the user from group error.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void removeUserFromGroupError() throws EEAException {
    Mockito.doThrow(new RestClientException("error test")).when(restTemplate).exchange(
        Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class),
        Mockito.any(Class.class));
    try {
      keycloakConnectorService.removeUserFromGroup("user1", "");
    } catch (EEAException e) {
      Assert.assertEquals(String.format(EEAErrorMessage.PERMISSION_NOT_REMOVED, ""),
          e.getMessage());
      throw e;
    }
  }
}
