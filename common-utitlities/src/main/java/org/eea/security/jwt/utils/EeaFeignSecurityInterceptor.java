package org.eea.security.jwt.utils;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Eea feign security interceptor.
 */
@Component
@Slf4j
public class EeaFeignSecurityInterceptor implements RequestInterceptor {

  private static final String AUTHORIZATION_HEADER = "Authorization";


  @Override
  public void apply(RequestTemplate template) {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    Authentication authentication = securityContext.getAuthentication();

    if (authentication != null && authentication instanceof UsernamePasswordAuthenticationToken) {
      log.info("Securing invocation to {} with token {} and user {}", template.url(),
          authentication.getCredentials().toString(), authentication.getName());
      template.header(AUTHORIZATION_HEADER, authentication.getCredentials().toString());
      template.header("FeignInvocationUser", authentication.getName());
    }
  }
}
