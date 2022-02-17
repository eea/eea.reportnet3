package org.eea.ums.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.UserNationalCoordinatorVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.eea.ums.mapper.UserRepresentationMapper;
import org.eea.ums.service.BackupManagmentService;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.eea.ums.service.UserNationalCoordinatorService;
import org.eea.ums.service.UserRoleService;
import org.eea.ums.service.keycloak.model.GroupInfo;
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

/**
 * The Class UserManagementControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserManagementControllerImplTest {

  /** The user management controller. */
  @InjectMocks
  private UserManagementControllerImpl userManagementController;

  /** The security provider interface service. */
  @Mock
  private SecurityProviderInterfaceService securityProviderInterfaceService;

  /** The keycloak connector service. */
  @Mock
  private KeycloakConnectorService keycloakConnectorService;

  /** The backup managment service. */
  @Mock
  private BackupManagmentService backupManagmentService;

  /** The user representation mapper. */
  @Mock
  private UserRepresentationMapper userRepresentationMapper;

  /** The user role service. */
  @Mock
  private UserRoleService userRoleService;

  /** The notification controller. */
  @Mock
  private NotificationControllerZuul notificationController;

  /** The http servlet response. */
  @Mock
  private HttpServletResponse httpServletResponse;

  /** The user national coordinator service. */
  @Mock
  private UserNationalCoordinatorService userNationalCoordinatorService;

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
  }


  /**
   * Generate token test.
   */
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

  /**
   * Generate token by code test.
   */
  @Test
  public void generateTokenByCodeTest() {
    TokenVO tokenVO = new TokenVO();
    tokenVO.setAccessToken("token");
    Mockito.when(securityProviderInterfaceService.doLogin(Mockito.anyString())).thenReturn(tokenVO);
    TokenVO result = userManagementController.generateToken("");
    Assert.assertNotNull(result);
    Assert.assertEquals("token", result.getAccessToken());
  }

  /**
   * Refresh token test.
   */
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

  /**
   * Check resource access permission test.
   */
  @Test
  public void checkResourceAccessPermissionTest() {
    Mockito.when(securityProviderInterfaceService.checkAccessPermission("Dataflow",
        new AccessScopeEnum[] {AccessScopeEnum.CREATE})).thenReturn(true);
    AccessScopeEnum[] scopes = new AccessScopeEnum[] {AccessScopeEnum.CREATE};
    boolean checkedAccessPermission =
        userManagementController.checkResourceAccessPermission("Dataflow", scopes);
    Assert.assertTrue(checkedAccessPermission);
  }

  /**
   * Do log out.
   */
  @Test
  public void doLogOut() {

    userManagementController.doLogOut("refreshToken");
    Mockito.verify(securityProviderInterfaceService, Mockito.times(1))
        .doLogout(Mockito.anyString());
  }

  /**
   * Adds the contributor to resource.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void addContributorToResource() throws EEAException {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    userManagementController.addUserToResource(1l, ResourceGroupEnum.DATAFLOW_CUSTODIAN);
    Mockito.verify(securityProviderInterfaceService, Mockito.times(1))
        .addUserToUserGroup("userId_123", ResourceGroupEnum.DATAFLOW_CUSTODIAN.getGroupName(1l));
  }

  /**
   * Test get resources by user.
   */
  @Test
  public void testGetResourcesByUser() {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    List<ResourceAccessVO> resourceList = new ArrayList<>();
    Mockito.when(securityProviderInterfaceService.getResourcesByUser(Mockito.any()))
        .thenReturn(resourceList);
    assertEquals("assertion error", resourceList, userManagementController.getResourcesByUser());

  }

  /**
   * Test get resources by user 2.
   */
  @Test
  public void testGetResourcesByUser2() {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    List<ResourceAccessVO> resourceList = new ArrayList<>();
    Mockito.when(securityProviderInterfaceService.getResourcesByUser(Mockito.any()))
        .thenReturn(resourceList);
    assertEquals("assertion error", resourceList,
        userManagementController.getResourcesByUser(ResourceTypeEnum.DATAFLOW));

  }

  /**
   * Test get resources by user 3.
   */
  @Test
  public void testGetResourcesByUser3() {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    List<ResourceAccessVO> resourceList = new ArrayList<>();
    Mockito.when(securityProviderInterfaceService.getResourcesByUser(Mockito.any()))
        .thenReturn(resourceList);
    assertEquals("assertion error", resourceList,
        userManagementController.getResourcesByUser(SecurityRoleEnum.LEAD_REPORTER));
  }

  /**
   * Test get resources by user 4.
   */
  @Test
  public void testGetResourcesByUser4() {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    List<ResourceAccessVO> resourceList = new ArrayList<>();
    Mockito.when(securityProviderInterfaceService.getResourcesByUser(Mockito.any()))
        .thenReturn(resourceList);
    assertEquals("assertion error", resourceList, userManagementController
        .getResourcesByUser(ResourceTypeEnum.DATAFLOW, SecurityRoleEnum.LEAD_REPORTER));

  }

  /**
   * Read excel test.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void readExcelTest() throws IOException {
    MockMultipartFile file = new MockMultipartFile("files", "filename.txt", "text/plain",
        "hello".getBytes(StandardCharsets.UTF_8));

    userManagementController.createUsers(file);
    Mockito.verify(backupManagmentService, times(1)).readAndSaveUsers(Mockito.any());
  }

  /**
   * Read excel fail test.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = ResponseStatusException.class)
  public void readExcelFailTest() throws IOException {
    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Not found");
  }

  /**
   * Gets the email by user id test.
   *
   * @return the email by user id test
   */
  @Test
  public void getEmailByUserIdTest() {
    UserRepresentation user = new UserRepresentation();
    UserRepresentationVO userVO = new UserRepresentationVO();
    userVO.setEmail("provider@reportnet.net");
    Mockito.when(keycloakConnectorService.getUser(Mockito.any())).thenReturn(user);
    Mockito.when(userRepresentationMapper.entityToClass(Mockito.any())).thenReturn(userVO);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken("userId", "123", new HashSet<>());
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId");
    authentication.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    Assert.assertEquals("provider@reportnet.net",
        userManagementController.getUserByUserId().getEmail());
  }

  /**
   * Gets the email by user id null test.
   *
   * @return the email by user id null test
   */
  @Test
  public void getEmailByUserIdNullTest() {
    Mockito.when(keycloakConnectorService.getUser(Mockito.any())).thenReturn(null);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken("userId", "123", new HashSet<>());
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId");
    authentication.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    Assert.assertNull(userManagementController.getUserByUserId());
  }

  /**
   * Gets the users test.
   *
   * @return the users test
   * @throws IOException Signals that an I/O exception has occurred.
   */
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

  /**
   * Gets the users test fail.
   *
   * @return the users test fail
   * @throws IOException Signals that an I/O exception has occurred.
   */
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
    UserRepresentation user = new UserRepresentation();
    user.setEmail("sample@email.net");
    UserRepresentation[] users = new UserRepresentation[1];
    users[0] = user;
    Mockito.when(keycloakConnectorService.getUsersByEmail(Mockito.any())).thenReturn(users);
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

  /**
   * Adds the contributors to resources.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void addContributorsToResources() throws EEAException {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
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


  /**
   * Adds the user to resources.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void addUserToResources() throws EEAException {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
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

  /**
   * Update user attributes test.
   */
  @Test
  public void updateUserAttributesTest() {
    Map<String, List<String>> attributes = new HashMap<>();
    List<String> atts = new ArrayList<>();
    atts.add("attribute1");
    attributes.put("AT1", atts);
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    Mockito.when(keycloakConnectorService.getUser(Mockito.any()))
        .thenReturn(new UserRepresentation());
    userManagementController.updateUserAttributes(attributes);
    Mockito.verify(keycloakConnectorService, Mockito.times(1)).updateUser(Mockito.any());
  }

  /**
   * Update user attributes test error.
   */
  @Test(expected = ResponseStatusException.class)
  public void updateUserAttributesTestError() {
    Map<String, List<String>> attributes = new HashMap<>();
    List<String> atts = new ArrayList<>();
    atts.add("attribute1");
    attributes.put("AT1", atts);
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    try {
      userManagementController.updateUserAttributes(attributes);
    } catch (ResponseStatusException e) {
      assertEquals("bad status", HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals("bad message", String.format(EEAErrorMessage.USER_NOTFOUND, "userId_123"),
          e.getReason());
      throw e;
    }
  }

  /**
   * Gets the user attributes test.
   *
   * @return the user attributes test
   */
  @Test
  public void getUserAttributesTest() {
    Map<String, List<String>> attributes = new HashMap<>();
    UserRepresentation user = new UserRepresentation();
    user.setAttributes(attributes);
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    Mockito.when(securityProviderInterfaceService.getUserWithoutKeys(Mockito.any()))
        .thenReturn(user);
    assertEquals("error", attributes, userManagementController.getUserAttributes());
  }

  /**
   * Gets the user attributes test error.
   *
   * @return the user attributes test error
   */
  @Test(expected = ResponseStatusException.class)
  public void getUserAttributesTestError() {
    Map<String, List<String>> attributes = new HashMap<>();
    UserRepresentation user = new UserRepresentation();
    user.setAttributes(attributes);
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    try {
      userManagementController.getUserAttributes();
    } catch (ResponseStatusException e) {
      assertEquals("bad status", HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals("bad message", String.format(EEAErrorMessage.USER_NOTFOUND, "userId_123"),
          e.getReason());
      throw e;
    }
  }

  /**
   * Adds the contributor to resorce test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void addContributorToResorceTest() throws EEAException {
    userManagementController.addContributorToResource(1L, ResourceGroupEnum.DATAFLOW_CUSTODIAN, "");
    Mockito.verify(securityProviderInterfaceService, Mockito.times(1))
        .addContributorToUserGroup(Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Adds the contributor to resource error test.
   *
   * @throws EEAException the EEA exception
   */
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

  /**
   * Adds the contributors to resource error test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void addContributorsToResourceErrorTest() throws EEAException {
    Mockito.doThrow(EEAException.class).when(securityProviderInterfaceService)
        .addContributorsToUserGroup(Mockito.any());
    try {
      userManagementController.addContributorsToResources(new ArrayList<>());
    } catch (ResponseStatusException e) {
      assertEquals("bad status", HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals("bad message", EEAErrorMessage.PERMISSION_NOT_CREATED, e.getReason());
      throw e;
    }
  }

  /**
   * Creates the api key no user error test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createApiKeyNoUserErrorTest() throws EEAException {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    doThrow(new EEAException(EEAErrorMessage.PERMISSION_NOT_CREATED))
        .when(securityProviderInterfaceService)
        .createApiKey(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong());
    try {
      userManagementController.createApiKey(1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals("bad status", HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals("bad message", EEAErrorMessage.PERMISSION_NOT_CREATED, e.getReason());
      throw e;
    }
  }

  /**
   * Creates the api key permission error test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createApiKeyPermissionErrorTest() throws EEAException {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    doThrow(new EEAException("error")).when(securityProviderInterfaceService)
        .createApiKey(Mockito.any(), Mockito.any(), Mockito.any());
    try {
      userManagementController.createApiKey(1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals("bad status", HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals("bad message", EEAErrorMessage.PERMISSION_NOT_CREATED, e.getReason());
      throw e;
    }
  }

  /**
   * Creates the api key success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createApiKeySuccessTest() throws EEAException {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    when(securityProviderInterfaceService.createApiKey(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn("uuid");
    assertEquals("error", "uuid", userManagementController.createApiKey(1L, 1L));
  }

  /**
   * Gets the api key no user error test.
   *
   * @return the api key no user error test
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getApiKeyNoUserErrorTest() throws EEAException {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    doThrow(new EEAException("error")).when(securityProviderInterfaceService)
        .getApiKey(Mockito.any(), Mockito.any(), Mockito.any());
    try {
      userManagementController.getApiKey(1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals("bad status", HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals("bad message", EEAErrorMessage.PERMISSION_NOT_CREATED, e.getReason());
      throw e;
    }
  }

  /**
   * Gets the api key permission error test.
   *
   * @return the api key permission error test
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getApiKeyPermissionErrorTest() throws EEAException {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    doThrow(new EEAException("error")).when(securityProviderInterfaceService)
        .getApiKey(Mockito.any(), Mockito.any(), Mockito.any());
    try {
      userManagementController.getApiKey(1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals("bad status", HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals("bad message", EEAErrorMessage.PERMISSION_NOT_CREATED, e.getReason());
      throw e;
    }
  }

  /**
   * Gets the api key success test.
   *
   * @return the api key success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getApiKeySuccessTest() throws EEAException {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    // when(keycloakConnectorService.getUser(Mockito.any())).thenReturn(new UserRepresentation());
    when(securityProviderInterfaceService.getApiKey(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn("uuid");
    assertEquals("error", "uuid", userManagementController.getApiKey(1L, 1L));
  }

  /**
   * Authenticate user by api key.
   */
  @Test
  public void authenticateUserByApiKey() {
    TokenVO tokenVO = new TokenVO();
    tokenVO.setPreferredUsername("userName1");
    when(securityProviderInterfaceService.authenticateApiKey(Mockito.eq("apiKey1")))
        .thenReturn(tokenVO);
    TokenVO result = this.userManagementController.authenticateUserByApiKey("apiKey1");
    Assert.assertNotNull(result);
    Assert.assertEquals(result, tokenVO);
  }

  /**
   * Authenticate user by api key wrong api key.
   */
  @Test
  public void authenticateUserByApiKeyWrongApiKey() {
    TokenVO result = this.userManagementController.authenticateUserByApiKey("apiKey1");
    Assert.assertNull(result);
  }

  /**
   * Gets the users by group test.
   *
   * @return the users by group test
   */
  @Test
  public void getUsersByGroupTest() {
    GroupInfo[] groupInfo = new GroupInfo[1];
    UserRepresentation[] userRepresentation = new UserRepresentation[1];
    groupInfo[0] = new GroupInfo();
    userRepresentation[0] = new UserRepresentation();
    Mockito.when(keycloakConnectorService.getGroupsWithSearch(Mockito.any())).thenReturn(groupInfo);
    Mockito.when(keycloakConnectorService.getUsersByGroupId(Mockito.any()))
        .thenReturn(userRepresentation);
    assertNotNull(userManagementController.getUsersByGroup(""));
  }

  /**
   * Gets the users by group test null.
   *
   * @return the users by group test null
   */
  @Test
  public void getUsersByGroupTestNull() {
    assertNull(userManagementController.getUsersByGroup(""));
  }

  /**
   * Removes the contributor from resource.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void removeContributorFromResource() throws EEAException {
    userManagementController.removeContributorFromResource(1L,
        ResourceGroupEnum.DATAFLOW_EDITOR_READ, "");
    Mockito.verify(securityProviderInterfaceService, Mockito.times(1))
        .removeContributorFromUserGroup(Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Removes the contributor from resource exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void removeContributorFromResourceException() throws EEAException {
    Mockito.doThrow(EEAException.class).when(securityProviderInterfaceService)
        .removeContributorFromUserGroup(Mockito.any(), Mockito.any(), Mockito.any());
    try {
      userManagementController.removeContributorFromResource(1L,
          ResourceGroupEnum.DATAFLOW_EDITOR_READ, "");
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals(EEAErrorMessage.PERMISSION_NOT_CREATED, e.getReason());
      throw e;
    }
  }

  /**
   * Removes the contributors from resources.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void removeContributorsFromResources() throws EEAException {
    userManagementController.removeContributorsFromResources(new ArrayList<>());
    Mockito.verify(securityProviderInterfaceService, Mockito.times(1))
        .removeContributorsFromUserGroup(Mockito.any());
  }


  /**
   * Removes the contributors from resources exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void removeContributorsFromResourcesException() throws EEAException {
    Mockito.doThrow(EEAException.class).when(securityProviderInterfaceService)
        .removeContributorsFromUserGroup(Mockito.any());
    try {
      userManagementController.removeContributorsFromResources(new ArrayList<>());
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals(EEAErrorMessage.PERMISSION_NOT_CREATED, e.getReason());
      throw e;
    }
  }

  /**
   * Removes the user from resources.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void removeUserFromResources() throws EEAException {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    List<ResourceAssignationVO> resources = new ArrayList<>();
    ResourceAssignationVO resource = new ResourceAssignationVO();
    resource.setResourceGroup(ResourceGroupEnum.DATAFLOW_EDITOR_READ);
    resource.setResourceId(1L);
    resources.add(resource);
    userManagementController.removeUserFromResources(resources);
    Mockito.verify(securityProviderInterfaceService, Mockito.times(1))
        .removeUserFromUserGroup(Mockito.any(), Mockito.any());
  }

  /**
   * Removes the user from resources exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void removeUserFromResourcesException() throws EEAException {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    List<ResourceAssignationVO> resources = new ArrayList<>();
    ResourceAssignationVO resource = new ResourceAssignationVO();
    resource.setResourceGroup(ResourceGroupEnum.DATAFLOW_EDITOR_READ);
    resource.setResourceId(1L);
    resources.add(resource);
    Mockito.doThrow(EEAException.class).when(securityProviderInterfaceService)
        .removeUserFromUserGroup(Mockito.any(), Mockito.any());
    try {
      userManagementController.removeUserFromResources(resources);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals(EEAErrorMessage.PERMISSION_NOT_CREATED, e.getReason());
      throw e;
    }
  }

  /**
   * Gets the resources by user email test.
   *
   * @return the resources by user email test
   */
  @Test
  public void getResourcesByUserEmailTest() {
    UserRepresentation[] userList = new UserRepresentation[1];
    UserRepresentation user = new UserRepresentation();
    user.setId("idGroupInfo");
    user.setUsername("Dataflow-1-DATA_CUSTODIAN");
    userList[0] = user;
    Mockito.when(keycloakConnectorService.getUsersByEmail(Mockito.any())).thenReturn(userList);
    List<ResourceAccessVO> resourceList = new ArrayList<>();
    Mockito.when(securityProviderInterfaceService.getResourcesByUser(Mockito.any()))
        .thenReturn(resourceList);
    assertEquals("assertion error", resourceList,
        userManagementController.getResourcesByUserEmail("email"));
  }

  /**
   * Authenticate user by email.
   */
  @Test
  public void authenticateUserByEmail() {
    TokenVO tokenVO = new TokenVO();
    tokenVO.setUserId("user1");
    Mockito
        .when(securityProviderInterfaceService.authenticateEmail(Mockito.eq("user1@reportnet.net")))
        .thenReturn(tokenVO);
    TokenVO result = userManagementController.authenticateUserByEmail("user1@reportnet.net");
    Assert.assertNotNull(result);
    Assert.assertEquals("user1", result.getUserId());
  }


  /**
   * Gets the user roles by dataflow and country test.
   *
   * @return the user roles by dataflow and country test
   */
  @Test
  public void getUserRolesByDataflowAndCountryTest() {
    assertNotNull(userManagementController.getUserRolesByDataflowAndCountry(1L, 1L));
  }

  /**
   * Gets the user roles by dataflow test.
   *
   * @return the user roles by dataflow test
   */
  @Test
  public void getUserRolesByDataflowTest() {
    assertNotNull(userManagementController.getUserRolesByDataflow(0L));
  }


  /**
   * Download users by country exception test.
   */
  @Test(expected = ResponseStatusException.class)
  public void downloadUsersByCountryExceptionTest() {
    Mockito.doThrow(ResponseStatusException.class).when(userRoleService)
        .downloadUsersByCountry(Mockito.anyLong(), Mockito.anyString());
    try {
      userManagementController.downloadUsersByCountry(1L, "fileName", null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  /**
   * Export users by country test.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Test
  public void exportUsersByCountryTest() throws IOException, EEAException {
    UserNotificationContentVO notification = new UserNotificationContentVO();
    notification.setDataflowId(1L);
    Mockito.lenient().doNothing().when(notificationController)
        .createUserNotificationPrivate("EVENT", notification);
    userManagementController.exportUsersByCountry(1L);
    Mockito.verify(userRoleService, times(1)).exportUsersByCountry(1L);
  }

  /**
   * Export users by country exception test.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Test
  public void exportUsersByCountryExceptionTest() throws IOException, EEAException {
    Mockito.doThrow(IOException.class).when(userRoleService)
        .exportUsersByCountry(Mockito.anyLong());
    userManagementController.exportUsersByCountry(1L);
    Mockito.verify(userRoleService, times(1)).exportUsersByCountry(Mockito.anyLong());
  }

  /**
   * Gets the api key test.
   *
   * @return the api key test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getApiKeyTest() throws EEAException {
    when(securityProviderInterfaceService.getApiKey(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn("uuid");
    assertEquals("uuid", userManagementController.getApiKey("uuid", 1L, 1L));
  }

  /**
   * Adds the user to resources exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void addUserToResourcesExceptionTest() throws EEAException {
    try {
      UsernamePasswordAuthenticationToken authenticationToken =
          new UsernamePasswordAuthenticationToken("user1", null, null);
      Map<String, String> details = new HashMap<>();
      details.put(AuthenticationDetails.USER_ID, "userId_123");
      authenticationToken.setDetails(details);
      SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      ResourceAssignationVO resource = new ResourceAssignationVO();
      resource.setEmail("test@reportnet.net");
      resource.setResourceGroup(ResourceGroupEnum.DATAFLOW_CUSTODIAN);
      resource.setResourceId(1L);
      List<ResourceAssignationVO> resources = new ArrayList<>();
      resources.add(resource);
      Mockito.doThrow(EEAException.class).when(securityProviderInterfaceService)
          .addUserToUserGroup(Mockito.anyString(), Mockito.anyString());
      userManagementController.addUserToResources(resources);
    } catch (ResponseStatusException e) {
      assertNotNull(e);
      throw e;
    }
  }

  /**
   * Creates the users exception test.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = ResponseStatusException.class)
  public void createUsersExceptionTest() throws IOException {
    MockMultipartFile file = new MockMultipartFile("files", "filename.txt", "text/plain",
        "hello".getBytes(StandardCharsets.UTF_8));
    try {
      Mockito.doThrow(IOException.class).when(backupManagmentService)
          .readAndSaveUsers(Mockito.any());
      userManagementController.createUsers(file);
    } catch (ResponseStatusException e) {
      assertNotNull(e);
      throw e;
    }
  }

  /**
   * Adds the user to resource exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void addUserToResourceExceptionTest() throws EEAException {
    try {
      UsernamePasswordAuthenticationToken authenticationToken =
          new UsernamePasswordAuthenticationToken("user1", null, null);
      Map<String, String> details = new HashMap<>();
      details.put(AuthenticationDetails.USER_ID, "userId_123");
      authenticationToken.setDetails(details);
      SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      ResourceAssignationVO resource = new ResourceAssignationVO();
      resource.setEmail("test@reportnet.net");
      resource.setResourceGroup(ResourceGroupEnum.DATAFLOW_CUSTODIAN);
      resource.setResourceId(1L);
      List<ResourceAssignationVO> resources = new ArrayList<>();
      resources.add(resource);
      Mockito.doThrow(EEAException.class).when(securityProviderInterfaceService)
          .addUserToUserGroup(Mockito.anyString(), Mockito.anyString());
      userManagementController.addUserToResource(1L, ResourceGroupEnum.DATACOLLECTION_CUSTODIAN);
    } catch (ResponseStatusException e) {
      assertNotNull(e);
      throw e;
    }
  }

  /**
   * Gets the user national coordinator test.
   *
   * @return the user national coordinator test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getUserNationalCoordinatorTest() throws EEAException {
    assertNotNull(userManagementController.getUserNationalCoordinator());
  }

  /**
   * Creates the user national coordinator test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createUserNationalCoordinatorTest() throws EEAException {
    UserNationalCoordinatorVO userNC = new UserNationalCoordinatorVO();
    userNC.setCountryCode("ES");
    userNC.setEmail("abc@abc.com");
    userManagementController.createNationalCoordinator(userNC);
    Mockito.verify(userNationalCoordinatorService, times(1))
        .createNationalCoordinator(Mockito.any());
  }

  /**
   * Creates the user national coordinator exception 400 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createUserNationalCoordinatorException400Test() throws EEAException {
    UserNationalCoordinatorVO userNC = new UserNationalCoordinatorVO();
    userNC.setCountryCode("ES");
    userNC.setEmail("abc@abc.com");
    Mockito.doThrow(new EEAException(String.format(EEAErrorMessage.NOT_EMAIL, userNC.getEmail())))
        .when(userNationalCoordinatorService).createNationalCoordinator(Mockito.any());
    try {
      userManagementController.createNationalCoordinator(userNC);
    } catch (ResponseStatusException e) {
      assertEquals(String.format(EEAErrorMessage.NOT_EMAIL, userNC.getEmail()), e.getReason());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void createUserNationalCoordinatorException404Test() throws EEAException {
    UserNationalCoordinatorVO userNC = new UserNationalCoordinatorVO();
    userNC.setCountryCode("ES");
    userNC.setEmail("abc@abc.com");
    Mockito
        .doThrow(new EEAException(String.format(EEAErrorMessage.USER_NOTFOUND, userNC.getEmail())))
        .when(userNationalCoordinatorService).createNationalCoordinator(Mockito.any());
    try {
      userManagementController.createNationalCoordinator(userNC);
    } catch (ResponseStatusException e) {
      assertEquals(String.format(EEAErrorMessage.USER_NOTFOUND, userNC.getEmail()), e.getReason());
      throw e;
    }
  }

  /**
   * Creates the user national coordinator exception 500 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createUserNationalCoordinatorException500Test() throws EEAException {
    UserNationalCoordinatorVO userNC = new UserNationalCoordinatorVO();
    userNC.setCountryCode("ES");
    userNC.setEmail("abc@abc.com");
    Mockito.doThrow(new EEAException("")).when(userNationalCoordinatorService)
        .createNationalCoordinator(Mockito.any());
    try {
      userManagementController.createNationalCoordinator(userNC);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  /**
   * Delete user national coordinator test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteUserNationalCoordinatorTest() throws EEAException {
    UserNationalCoordinatorVO userNC = new UserNationalCoordinatorVO();
    userNC.setCountryCode("ES");
    userNC.setEmail("abc@abc.com");
    userManagementController.deleteNationalCoordinator(userNC);
    Mockito.verify(userNationalCoordinatorService, times(1))
        .deleteNationalCoordinator(Mockito.any());
  }

  /**
   * Delete user national coordinator exception 400 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteUserNationalCoordinatorException400Test() throws EEAException {
    UserNationalCoordinatorVO userNC = new UserNationalCoordinatorVO();
    userNC.setCountryCode("ES");
    userNC.setEmail("abc@abc.com");
    Mockito.doThrow(new EEAException(String.format(EEAErrorMessage.NOT_EMAIL, userNC.getEmail())))
        .when(userNationalCoordinatorService).deleteNationalCoordinator(Mockito.any());
    try {
      userManagementController.deleteNationalCoordinator(userNC);
    } catch (ResponseStatusException e) {
      assertEquals(String.format(EEAErrorMessage.NOT_EMAIL, userNC.getEmail()), e.getReason());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void deleteUserNationalCoordinatorException404Test() throws EEAException {
    UserNationalCoordinatorVO userNC = new UserNationalCoordinatorVO();
    userNC.setCountryCode("ES");
    userNC.setEmail("abc@abc.com");
    Mockito
        .doThrow(new EEAException(String.format(EEAErrorMessage.USER_NOTFOUND, userNC.getEmail())))
        .when(userNationalCoordinatorService).deleteNationalCoordinator(Mockito.any());
    try {
      userManagementController.deleteNationalCoordinator(userNC);
    } catch (ResponseStatusException e) {
      assertEquals(String.format(EEAErrorMessage.USER_NOTFOUND, userNC.getEmail()), e.getReason());
      throw e;
    }
  }

  /**
   * Delete user national coordinator exception 500 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteUserNationalCoordinatorException500Test() throws EEAException {
    UserNationalCoordinatorVO userNC = new UserNationalCoordinatorVO();
    userNC.setCountryCode("ES");
    userNC.setEmail("abc@abc.com");
    Mockito.doThrow(new EEAException("")).when(userNationalCoordinatorService)
        .deleteNationalCoordinator(Mockito.any());
    try {
      userManagementController.deleteNationalCoordinator(userNC);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }


}
