package org.eea.security.jwt.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import feign.RequestTemplate;

/**
 * The Class EeaFeignSecurityInterceptorTest.
 */
public class EeaFeignSecurityInterceptorTest {

  @InjectMocks
  private EeaFeignSecurityInterceptor eeaFeignSecurityInterceptor;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testApply() {
    RequestTemplate template = new RequestTemplate();
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken("user", "123", new HashSet<>());
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userIdTest");
    authentication.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    eeaFeignSecurityInterceptor.apply(template);

    Assert.assertTrue(template.headers().get("FeignInvocationUser").contains("user"));
    Assert.assertTrue(template.headers().get("Authorization").contains("123"));
  }
}
