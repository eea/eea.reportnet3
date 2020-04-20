package org.eea.security.jwt.utils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import feign.RequestTemplate;

/**
 * The Class EeaFeignSecurityInterceptorTest.
 */
public class EeaFeignSecurityInterceptorTest {

  /** The eea feign security interceptor. */
  @InjectMocks
  EeaFeignSecurityInterceptor eeaFeignSecurityInterceptor;

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
    eeaFeignSecurityInterceptor.apply(template);
  }

}
