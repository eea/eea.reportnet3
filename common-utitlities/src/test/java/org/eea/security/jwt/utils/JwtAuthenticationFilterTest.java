package org.eea.security.jwt.utils;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.eea.security.jwt.data.TokenDataVO;
import org.eea.utils.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.common.VerificationException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class JwtAuthenticationFilterTest {

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;
  @Mock
  private JwtTokenProvider tokenProvider;
  @Mock
  private HttpServletRequest request;

  @Mock
  private FilterChain filterChain;

  @Before
  public void init() {
    Mockito.reset(tokenProvider, request);
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  @Test
  public void doFilterInternal()
      throws NoSuchAlgorithmException, VerificationException, ServletException, IOException {

    Map<String, Object> keys = TestUtils.getRSAKeys();
    String token = TestUtils.generateToken(keys, System.currentTimeMillis() + 1000, "user1");
    Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    TokenDataVO jwt = new TokenDataVO();

    jwt.setPreferredUsername("user1");
    List<String> userGroups = new ArrayList<>();
    userGroups.add("DATAFLOW_1_DATA_PROVIDER");
    Map<String, Object> otherClaims = new HashMap<>();
    otherClaims.put("user_groups", userGroups);
    jwt.setOtherClaims(otherClaims);
    jwt.setUserId("userId_123");

    Set<String> roles = new HashSet<>();
    roles.add("DATA_PROVIDER");
    jwt.setRoles(roles);
    Mockito.when(tokenProvider.retrieveToken(Mockito.anyString())).thenReturn(jwt);
    jwtAuthenticationFilter.doFilterInternal(request, null, filterChain);
    UsernamePasswordAuthenticationToken authenticationToken =
        (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext()
            .getAuthentication();
    Assert.assertNotNull(authenticationToken);
    Assert.assertEquals("Retrieved JWT is different from expected", "Bearer " + token,
        authenticationToken.getCredentials());
    Assert.assertEquals("Retrieved User is different from expected", "user1",
        ((EeaUserDetails) authenticationToken.getPrincipal()).getUsername());
    Assert.assertEquals("Retrieved Subject is different from expected", "userId_123",
        ((Map<String, String>) authenticationToken.getDetails())
            .get(AuthenticationDetails.USER_ID));
    Mockito.verify(filterChain, Mockito.times(1)).doFilter(request, null);

  }

  @Test
  public void doFilterInternalKo()
      throws NoSuchAlgorithmException, VerificationException, ServletException, IOException {

    Map<String, Object> keys = TestUtils.getRSAKeys();
    String token = TestUtils.generateToken(keys, System.currentTimeMillis() + 1000, "user1");
    Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    Mockito.doThrow(new VerificationException()).when(tokenProvider)
        .retrieveToken(Mockito.anyString());

    jwtAuthenticationFilter.doFilterInternal(request, null, filterChain);
    UsernamePasswordAuthenticationToken authenticationToken =
        (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext()
            .getAuthentication();
    Assert.assertNull(authenticationToken);
    Mockito.verify(filterChain, Mockito.times(1)).doFilter(request, null);

  }
}
