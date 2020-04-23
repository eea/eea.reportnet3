package org.eea.security.jwt.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import feign.RequestTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The Class EeaFeignSecurityInterceptorTest.
 */
public class EeaFeignSecurityInterceptorTest {

  /**
   * The eea feign security interceptor.
   */
  @InjectMocks
  private EeaFeignSecurityInterceptor eeaFeignSecurityInterceptor;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test apply.
   */
  @Test
  public void testApply() {
    RequestTemplate template = new RequestTemplate();
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken("user", "123", new HashSet<>());
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userIdTest");
    authentication.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    eeaFeignSecurityInterceptor.apply(template);

    //Assert.assertTrue(template.headers().get("FeignInvocationId").contains("userIdTest"));
    Assert.assertTrue(template.headers().get("FeignInvocationUser").contains("user"));
    Assert.assertTrue(template.headers().get("Authorization").contains("Bearer 123"));
  }

}
