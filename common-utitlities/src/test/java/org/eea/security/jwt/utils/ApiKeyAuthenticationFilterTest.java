package org.eea.security.jwt.utils;

import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.ums.TokenVO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class ApiKeyAuthenticationFilterTest {

  @InjectMocks
  private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  @Mock
  private HttpServletRequest request;

  @Mock
  private FilterChain filterChain;

  public void init() {
    Mockito.reset(userManagementControllerZull, request, filterChain);
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  @Test
  public void doFilterInternal() throws ServletException, IOException {
    String apiKey = "ApiKey1";
    // request configuration
    when(request.getHeader("Authorization")).thenReturn("ApiKey " + apiKey);

    // Token configuration
    TokenVO tokenVO = new TokenVO();
    tokenVO.setUserId("userId1");
    tokenVO.setPreferredUsername("userName1");
    Set<String> groups = new HashSet<>();
    groups.add("Dataflow-1-DATA_PROVIDER");
    tokenVO.setGroups(groups);

    Set<String> roles = new HashSet<>();
    roles.add("DATA_PROVIDER");
    tokenVO.setRoles(roles);
    when(userManagementControllerZull.authenticateUserByApiKey(Mockito.eq("ApiKey1")))
        .thenReturn(tokenVO);

    apiKeyAuthenticationFilter.doFilterInternal(request, null, filterChain);
    UsernamePasswordAuthenticationToken authenticationToken =
        (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext()
            .getAuthentication();
    Assert.assertNotNull(authenticationToken);
    Assert.assertEquals("Retrieved ApiKey is different from expected", "ApiKey " + apiKey,
        authenticationToken.getCredentials());
    Assert.assertEquals("Retrieved User is different from expected", "userName1",
        ((EeaUserDetails) authenticationToken.getPrincipal()).getUsername());
    Assert.assertEquals("Retrieved Subject is different from expected", "userId1",
        ((Map<String, String>) authenticationToken.getDetails())
            .get(AuthenticationDetails.USER_ID));
    Mockito.verify(filterChain, Mockito.times(1)).doFilter(request, null);
  }


}
