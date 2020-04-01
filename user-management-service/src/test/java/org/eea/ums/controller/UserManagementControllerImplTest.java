package org.eea.ums.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.ums.mapper.UserRepresentationMapper;
import org.eea.ums.service.BackupManagmentService;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

@RunWith(MockitoJUnitRunner.class)
public class UserManagementControllerImplTest {

  @InjectMocks
  private UserManagementControllerImpl userManagementController;

  @Mock
  private SecurityProviderInterfaceService securityProviderInterfaceService;

  @Mock
  private KeycloakConnectorService keycloakConnectorService;

  @Mock
  private BackupManagmentService backupManagmentService;

  @Mock
  private UserRepresentationMapper userRepresentationMapper;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }


  @Test
  public void generateTokenTest() {
    TokenVO tokenVO = new TokenVO();
    tokenVO.setAccessToken("token");
    Mockito.doReturn(tokenVO).when(securityProviderInterfaceService).doLogin(Mockito.anyString(),
        Mockito.anyString(), Mockito.anyBoolean());

    TokenVO result = userManagementController.generateToken("", "");
    Assert.assertNotNull(result);
    Assert.assertEquals("token", result.getAccessToken());
  }

  @Test
  public void generateTokenByCodeTest() {
    TokenVO tokenVO = new TokenVO();
    tokenVO.setAccessToken("token");
    Mockito.when(securityProviderInterfaceService.doLogin(Mockito.anyString())).thenReturn(tokenVO);
    TokenVO result = userManagementController.generateToken("");
    Assert.assertNotNull(result);
    Assert.assertEquals("token", result.getAccessToken());
  }

  @Test
  public void refreshTokenTest() {
    TokenVO tokenVO = new TokenVO();
    tokenVO.setAccessToken("token");
    Mockito.when(securityProviderInterfaceService.refreshToken(Mockito.anyString()))
        .thenReturn(tokenVO);
    TokenVO result = userManagementController.refreshToken("");
    Assert.assertNotNull(result);
    Assert.assertEquals("token", result.getAccessToken());
  }

  @Test
  public void checkResourceAccessPermissionTest() {
    Mockito.when(securityProviderInterfaceService.checkAccessPermission("Dataflow",
        new AccessScopeEnum[] {AccessScopeEnum.CREATE})).thenReturn(true);
    AccessScopeEnum[] scopes = new AccessScopeEnum[] {AccessScopeEnum.CREATE};
    boolean checkedAccessPermission =
        userManagementController.checkResourceAccessPermission("Dataflow", scopes);
    Assert.assertTrue(checkedAccessPermission);
  }

  @Test
  public void doLogOut() {

    userManagementController.doLogOut("refreshToken");
    Mockito.verify(securityProviderInterfaceService, Mockito.times(1))
        .doLogout(Mockito.anyString());
  }

  @Test
  public void addContributorToResource() throws EEAException {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    userManagementController.addUserToResource(1l, ResourceGroupEnum.DATAFLOW_CUSTODIAN);
    Mockito.verify(securityProviderInterfaceService, Mockito.times(1))
        .addUserToUserGroup("userId_123", ResourceGroupEnum.DATAFLOW_CUSTODIAN.getGroupName(1l));
  }

  @Test
  public void testGetResourcesByUser() {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    userManagementController.getResourcesByUser();

  }

  @Test
  public void testGetResourcesByUser2() {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    userManagementController.getResourcesByUser(ResourceTypeEnum.DATAFLOW);

  }

  @Test
  public void testGetResourcesByUser3() {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    userManagementController.getResourcesByUser(SecurityRoleEnum.DATA_PROVIDER);

  }

  @Test
  public void testGetResourcesByUser4() {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    userManagementController.getResourcesByUser(ResourceTypeEnum.DATAFLOW,
        SecurityRoleEnum.DATA_PROVIDER);

  }

  @Test
  public void readExcelTest() throws IOException {
    MockMultipartFile file = new MockMultipartFile("files", "filename.txt", "text/plain",
        "hello".getBytes(StandardCharsets.UTF_8));

    userManagementController.createUsers(file);
    Mockito.verify(backupManagmentService, times(1)).readAndSaveUsers(Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void readExcelFailTest() throws IOException {
    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Not found");
  }

  @Test
  public void getUsersTest() throws IOException {

    UserRepresentation[] userList = new UserRepresentation[1];
    UserRepresentation user = new UserRepresentation();
    user.setId("idGroupInfo");
    user.setUsername("Dataflow-1-DATA_CUSTODIAN");
    userList[0] = user;

    Mockito.when(keycloakConnectorService.getUsers()).thenReturn(userList);

    userManagementController.getUsers();
    Mockito.verify(keycloakConnectorService, times(1)).getUsers();

  }

  @Test
  public void getUsersTestFail() throws IOException {

    UserRepresentation[] userList = new UserRepresentation[1];
    Mockito.when(keycloakConnectorService.getUsers()).thenReturn(userList);

    userManagementController.getUsers();
    Mockito.verify(keycloakConnectorService, times(1)).getUsers();

  }

  /**
   * Gets the user by email test.
   *
   * @return the user by email test
   */
  @Test
  public void getUserByEmailTest() {
    Mockito.when(keycloakConnectorService.getUsersByEmail(Mockito.any()))
        .thenReturn(new UserRepresentation[1]);
    Mockito.when(userRepresentationMapper.entityToClass(Mockito.any()))
        .thenReturn(new UserRepresentationVO());
    Assert.assertNotNull(userManagementController.getUserByEmail("sample@email.net"));
  }

  /**
   * Gets the user by email no users test.
   *
   * @return the user by email no users test
   */
  @Test
  public void getUserByEmailNoUsersTest() {
    Mockito.when(keycloakConnectorService.getUsersByEmail(Mockito.any())).thenReturn(null);
    Assert.assertNull(userManagementController.getUserByEmail("sample@email.net"));
  }

  /**
   * Gets the user by email to many users test.
   *
   * @return the user by email to many users test
   */
  @Test
  public void getUserByEmailToManyUsersTest() {
    Mockito.when(keycloakConnectorService.getUsersByEmail(Mockito.any()))
        .thenReturn(new UserRepresentation[2]);
    Assert.assertNull(userManagementController.getUserByEmail("sample@email.net"));
  }

  @Test
  public void addContributorsToResources() throws EEAException {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    ResourceAssignationVO resource = new ResourceAssignationVO();
    resource.setEmail("userId_123");
    resource.setResourceGroup(ResourceGroupEnum.DATAFLOW_CUSTODIAN);
    resource.setResourceId(1L);
    List<ResourceAssignationVO> resources = new ArrayList<>();
    resources.add(resource);
    userManagementController.addContributorsToResources(resources);
    Mockito.verify(securityProviderInterfaceService, Mockito.times(1))
        .addContributorsToUserGroup(resources);
  }


  @Test
  public void addUserToResources() throws EEAException {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    ResourceAssignationVO resource = new ResourceAssignationVO();
    resource.setEmail("test@reportnet.net");
    resource.setResourceGroup(ResourceGroupEnum.DATAFLOW_CUSTODIAN);
    resource.setResourceId(1L);
    List<ResourceAssignationVO> resources = new ArrayList<>();
    resources.add(resource);
    userManagementController.addUserToResources(resources);
    Mockito.verify(securityProviderInterfaceService, Mockito.times(1))
        .addUserToUserGroup("userId_123", ResourceGroupEnum.DATAFLOW_CUSTODIAN.getGroupName(1l));
  }

  @Test
  public void updateUserAttributesTest() {
    Map<String, List<String>> attributes = new HashMap<String, List<String>>();
    List<String> atts = new ArrayList<String>();
    atts.add("attribute1");
    attributes.put("AT1", atts);
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    Mockito.when(keycloakConnectorService.getUser(Mockito.any()))
        .thenReturn(new UserRepresentation());
    userManagementController.updateUserAttributes(attributes);
    Mockito.verify(keycloakConnectorService, Mockito.times(1)).updateUser(Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void updateUserAttributesTestError() {
    Map<String, List<String>> attributes = new HashMap<String, List<String>>();
    List<String> atts = new ArrayList<String>();
    atts.add("attribute1");
    attributes.put("AT1", atts);
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    try {
      userManagementController.updateUserAttributes(attributes);
    } catch (ResponseStatusException e) {
      assertEquals("bad status", HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals("bad message", EEAErrorMessage.USER_NOTFOUND, e.getReason());
      throw e;
    }
  }

  @Test
  public void getUserAttributesTest() {
    Map<String, List<String>> attributes = new HashMap<String, List<String>>();
    UserRepresentation user = new UserRepresentation();
    user.setAttributes(attributes);
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    Mockito.when(keycloakConnectorService.getUser(Mockito.any())).thenReturn(user);
    assertEquals("error", attributes, userManagementController.getUserAttributes());
  }

  @Test(expected = ResponseStatusException.class)
  public void getUserAttributesTestError() {
    Map<String, List<String>> attributes = new HashMap<String, List<String>>();
    UserRepresentation user = new UserRepresentation();
    user.setAttributes(attributes);
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    try {
      userManagementController.getUserAttributes();
    } catch (ResponseStatusException e) {
      assertEquals("bad status", HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals("bad message", EEAErrorMessage.USER_NOTFOUND, e.getReason());
      throw e;
    }
  }

  @Test
  public void addContributorToResorceTest() throws EEAException {
    userManagementController.addContributorToResource(1L, ResourceGroupEnum.DATAFLOW_CUSTODIAN, "");
    Mockito.verify(securityProviderInterfaceService, Mockito.times(1))
        .addContributorToUserGroup(Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void addContributorToResourceErrorTest() throws EEAException {
    Mockito.doThrow(EEAException.class).when(securityProviderInterfaceService)
        .addContributorToUserGroup(Mockito.any(), Mockito.any(), Mockito.any());
    try {
      userManagementController.addContributorToResource(1L, ResourceGroupEnum.DATAFLOW_CUSTODIAN,
          "");
    } catch (ResponseStatusException e) {
      assertEquals("bad status", HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals("bad message", EEAErrorMessage.PERMISSION_NOT_CREATED, e.getReason());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void addContributorsToResourceErrorTest() throws EEAException {
    Mockito.doThrow(EEAException.class).when(securityProviderInterfaceService)
        .addContributorsToUserGroup(Mockito.any());
    try {
      userManagementController.addContributorsToResources(new ArrayList<ResourceAssignationVO>());
    } catch (ResponseStatusException e) {
      assertEquals("bad status", HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals("bad message", EEAErrorMessage.PERMISSION_NOT_CREATED, e.getReason());
      throw e;
    }
  }



}
