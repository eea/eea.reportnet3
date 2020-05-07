package org.eea.security.jwt.utils;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eea.security.jwt.data.CacheTokenVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The type Eea feign security interceptor.
 */
@Component
@Slf4j
public class EeaFeignSecurityInterceptor implements RequestInterceptor {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_TOKEN_TYPE = "Bearer";


  @Override
  public void apply(RequestTemplate template) {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    Authentication authentication = securityContext.getAuthentication();

    if (authentication != null && authentication instanceof UsernamePasswordAuthenticationToken) {
      log.info("Securing invocation to {}", template.url());
      template.header(AUTHORIZATION_HEADER,
          String.format("%s %s", BEARER_TOKEN_TYPE,
              authentication.getCredentials()));
      template.header("FeignInvocationUser", authentication.getName());
     /* to be reviwed, it seems that in real invocation details are not present in the authentication
      template.header("FeignInvocationId",
          ((Map<String, String>) authentication.getDetails()).get("userId"));*/
    }
  }
}
