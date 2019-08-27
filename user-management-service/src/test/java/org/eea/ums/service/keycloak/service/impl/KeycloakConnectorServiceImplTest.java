package org.eea.ums.service.keycloak.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.ums.service.keycloak.model.CheckResourcePermissionResult;
import org.eea.ums.service.keycloak.model.ClientInfo;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.model.ResourceInfo;
import org.eea.ums.service.keycloak.model.TokenInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class KeycloakConnectorServiceImplTest {

  @InjectMocks
  private KeycloakConnectorServiceImpl keycloakConnectorService;
  @Mock
  private RestTemplate restTemplate;


  @Before
  public void init() {
    Map<String, String> resourceTypes = new HashMap<>();
    resourceTypes.put("Dataflow", "reportnet:type:dataflow");
    ReflectionTestUtils.setField(keycloakConnectorService, "resourceTypes", resourceTypes);
    ReflectionTestUtils.setField(keycloakConnectorService, "realmName", "Reportnet");
    ReflectionTestUtils.setField(keycloakConnectorService, "clientId", "reportnet");

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void checkUserPermision() {
    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
        "user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext()
        .setAuthentication(authenticationToken);
    CheckResourcePermissionResult body = new CheckResourcePermissionResult();
    body.setStatus("PERMIT");
    ResponseEntity<CheckResourcePermissionResult> checkResult = new ResponseEntity<>(body,
        HttpStatus.OK);
    Mockito.when(restTemplate
        .exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class),
            Mockito.any(Class.class))).thenReturn(checkResult);
    String result = keycloakConnectorService.checkUserPermision("Dataflow", AccessScopeEnum.CREATE);
    Assert.assertNotNull(result);
    Assert.assertEquals("PERMIT", result);
  }

  @Test
  public void generateToken() {
    TokenInfo body = new TokenInfo();
    body.setAccessToken("JWT");
    ResponseEntity<TokenInfo> result = new ResponseEntity<>(
        body,
        HttpStatus.OK);
    Mockito.when(restTemplate
        .postForEntity(Mockito.anyString(), Mockito.any(HttpEntity.class),
            Mockito.any(Class.class))).thenReturn(result);

    TokenInfo token = keycloakConnectorService.generateToken("user1", "1234");
    Assert.assertNotNull(result);
    Assert.assertEquals("JWT", token.getAccessToken());

  }

  @Test
  public void refreshToken() {
    TokenInfo body = new TokenInfo();
    body.setAccessToken("JWT");
    ResponseEntity<TokenInfo> result = new ResponseEntity<>(
        body,
        HttpStatus.OK);
    Mockito.when(restTemplate
        .postForEntity(Mockito.anyString(), Mockito.any(HttpEntity.class),
            Mockito.any(Class.class))).thenReturn(result);

    TokenInfo token = keycloakConnectorService.refreshToken("1234");
    Assert.assertNotNull(result);
    Assert.assertEquals("JWT", token.getAccessToken());

  }

  @Test
  public void getReportnetClientInfo() {
    ClientInfo info = new ClientInfo();
    info.setClientId("reportnet");
    ClientInfo[] body = new ClientInfo[]{info};

    ResponseEntity<ClientInfo[]> clientInfoResult = new ResponseEntity<>(body,
        HttpStatus.OK);
    Mockito.when(restTemplate
        .exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class),
            Mockito.any(Class.class))).thenReturn(clientInfoResult);
    ClientInfo result = ReflectionTestUtils
        .invokeMethod(keycloakConnectorService, "getReportnetClientInfo", "token");
    Assert.assertNotNull(result);

  }

  @Test
  public void getResourceInfo() {

    String[] bodyResourceSet = new String[]{"resource1"};

    ResponseEntity<String[]> resourceSetInfo = new ResponseEntity<>(bodyResourceSet,
        HttpStatus.OK);

    ResourceInfo bodyResourceInfo = new ResourceInfo();
    bodyResourceInfo.setName("Dataflow");
    ResponseEntity<ResourceInfo> resourceInfo = new ResponseEntity<>(bodyResourceInfo,
        HttpStatus.OK);
    Mockito.when(restTemplate
        .exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class),
            Mockito.any(Class.class))).then(new Answer<ResponseEntity>() {
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

    List<ResourceInfo> result = ReflectionTestUtils
        .invokeMethod(keycloakConnectorService, "getResourceInfo", "token");
    Assert.assertNotNull(result);
    Assert.assertTrue(!result.isEmpty());
    Assert.assertEquals(result.get(0).getName(), "Dataflow");

  }

  @Test
  public void getGroupsByUser() {

    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setId("1");
    groupInfo.setName("Dataflow-1-DATA_PROVIDER");
    groupInfo.setPath("/path");
    GroupInfo[] groupInfos = new GroupInfo[]{groupInfo};

    ResponseEntity<GroupInfo[]> responseGroupInfos = new ResponseEntity<>(groupInfos,
        HttpStatus.OK);

    Mockito.when(restTemplate
        .exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class),
            Mockito.any(Class.class))).thenReturn(responseGroupInfos);

    GroupInfo[] result = keycloakConnectorService.getGroupsByUser("user1");
    Assert.assertNotNull(result);
    Assert.assertTrue(result.length > 0);
    Assert.assertEquals(result[0].getName(), "Dataflow-1-DATA_PROVIDER");

  }
}