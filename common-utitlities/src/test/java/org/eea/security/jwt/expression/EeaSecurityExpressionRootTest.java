package org.eea.security.jwt.expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@RunWith(MockitoJUnitRunner.class)
public class EeaSecurityExpressionRootTest {

  private EeaSecurityExpressionRoot eeaSecurityExpressionRoot;
  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  private static final Long DATAFLOW_ID = 1l;

  @Before
  public void init() {
    Set<String> roles = new HashSet<>();
    roles.add(ObjectAccessRoleEnum.DATAFLOW_LEAD_REPORTER.getAccessRole(DATAFLOW_ID));
    UserDetails userDetails = EeaUserDetails.create("test", roles);
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    Map<String, String> details = new HashMap<>();
    details.put("", "");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

    Mockito.reset(userManagementControllerZull);

    eeaSecurityExpressionRoot =
        new EeaSecurityExpressionRoot(authenticationToken, userManagementControllerZull);

  }

  @Test
  public void secondLevelAuthorize() {
    Assert.assertFalse(eeaSecurityExpressionRoot.secondLevelAuthorize(DATAFLOW_ID,
        ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN));
  }

  @Test
  public void secondLevelAuthorizeUnauthorized() {
    Assert.assertFalse(eeaSecurityExpressionRoot.secondLevelAuthorize(DATAFLOW_ID,
        ObjectAccessRoleEnum.DATAFLOW_REQUESTER));
  }

  @Test
  public void checkPermission() {
    Mockito.when(userManagementControllerZull.checkResourceAccessPermission(Mockito.anyString(),
        Mockito.any(AccessScopeEnum[].class))).thenReturn(true);
    Assert.assertTrue(eeaSecurityExpressionRoot.checkPermission("", AccessScopeEnum.CREATE));
  }

  @Test
  public void checkPermissionUnauthorized() {
    Mockito.when(userManagementControllerZull.checkResourceAccessPermission(Mockito.anyString(),
        Mockito.any(AccessScopeEnum[].class))).thenReturn(false);
    Assert.assertFalse(eeaSecurityExpressionRoot.checkPermission("", AccessScopeEnum.CREATE));
  }

  @Test
  public void checkApiKeyTest() {
    assertFalse(eeaSecurityExpressionRoot.checkApiKey(1L, 1L, 1L,
        ObjectAccessRoleEnum.DATAFLOW_LEAD_REPORTER));
  }

  @Test
  public void setGetFilterObject() {
    eeaSecurityExpressionRoot.setFilterObject(DATAFLOW_ID);
    assertEquals(DATAFLOW_ID, eeaSecurityExpressionRoot.getFilterObject());
  }

  @Test
  public void setGetReturnObject() {
    eeaSecurityExpressionRoot.setReturnObject(DATAFLOW_ID);
    assertEquals(DATAFLOW_ID, eeaSecurityExpressionRoot.getReturnObject());
  }

  @Test
  public void getThis() {
    assertEquals(eeaSecurityExpressionRoot, eeaSecurityExpressionRoot.getThis());
  }

}
