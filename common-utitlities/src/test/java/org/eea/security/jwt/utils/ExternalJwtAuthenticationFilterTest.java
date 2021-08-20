package org.eea.security.jwt.utils;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.ums.TokenVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;


public class ExternalJwtAuthenticationFilterTest {

  @InjectMocks
  private ExternalJwtAuthenticationFilter externalJwtAuthenticationFilter;

  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  @Mock
  private JwtTokenProvider jwtTokenProvider;
  @Mock
  private HttpServletRequest request;

  @Mock
  private FilterChain filterChain;

  @Before
  public void init() {
    MockitoAnnotations.openMocks(this);
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  @Test
  public void doFilterInternal() throws ServletException, IOException {
    String jwt = "JWT123";
    // request configuration
    when(request.getHeader("Authorization")).thenReturn("JWT " + jwt);

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
    when(jwtTokenProvider.retrieveUserEmail(Mockito.eq("JWT123")))
        .thenReturn("userId1@reportnet.net");
    when(userManagementControllerZull.authenticateUserByEmail(Mockito.eq(
        URLEncoder.encode("userId1@reportnet.net", "UTF-8"))))
        .thenReturn(tokenVO);

    externalJwtAuthenticationFilter.doFilterInternal(request, null, filterChain);
    UsernamePasswordAuthenticationToken authenticationToken =
        (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext()
            .getAuthentication();
    Assert.assertNotNull(authenticationToken);

    Assert.assertEquals("Retrieved User is different from expected", "userName1",
        ((EeaUserDetails) authenticationToken.getPrincipal()).getUsername());
    Assert.assertEquals("Retrieved Subject is different from expected", "userId1",
        ((Map<String, String>) authenticationToken.getDetails())
            .get(AuthenticationDetails.USER_ID));
    Mockito.verify(filterChain, Mockito.times(1)).doFilter(request, null);
  }

  @Test
  public void doFilterInternalNoProvider() throws ServletException, IOException {
    String jwt = "JWT123";
    // request configuration
    when(request.getHeader("Authorization")).thenReturn("JWT " + jwt);

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
    when(jwtTokenProvider.retrieveUserEmail(Mockito.eq("JWT123")))
        .thenReturn(null);

    externalJwtAuthenticationFilter.doFilterInternal(request, null, filterChain);
    UsernamePasswordAuthenticationToken authenticationToken =
        (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext()
            .getAuthentication();
    Assert.assertNull(authenticationToken);

    Mockito.verify(filterChain, Mockito.times(1)).doFilter(request, null);
  }
}