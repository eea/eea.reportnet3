package org.eea.ums.controller;

import static org.mockito.Mockito.times;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.TokenVO;
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
  public void addContributorToResource() {
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
    Mockito.verify(securityProviderInterfaceService, Mockito.times(1)).addContributorToUserGroup(
        "userId_123", ResourceGroupEnum.DATAFLOW_CUSTODIAN.getGroupName(1l));
  }


  @Test
  public void addUserToResources() {
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
  public void addContributorsToDataflow() throws EEAException {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

    RepresentativeVO representative = new RepresentativeVO();
    representative.setDataProviderId(1L);
    userManagementController.addContributorsToDataflow(1L, Arrays.asList(representative));
    Mockito.verify(securityProviderInterfaceService, Mockito.times(1)).addContributorsToDataflow(1L,
        Arrays.asList(representative));
  }

}
