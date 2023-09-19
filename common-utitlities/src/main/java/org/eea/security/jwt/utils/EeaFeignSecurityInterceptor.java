package org.eea.security.jwt.utils;

import org.eea.utils.LiteralConstants;
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

  private static final String DREMIO_PROMOTE_BY_PATH_CONSTANT = "dremio%3A";

  @Override
  public void apply(RequestTemplate template) {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    Authentication authentication = securityContext.getAuthentication();
    if (template.url().contains(DREMIO_PROMOTE_BY_PATH_CONSTANT)){
      //this is done to encode the path of the folder for promoting. It needs to be enhanced
      String[] urlInParts = template.url().split(DREMIO_PROMOTE_BY_PATH_CONSTANT);
      if(urlInParts.length == 2) {
        String dremioPath = urlInParts[1];
        dremioPath = dremioPath.replace("/", "%2F");
        String fullUrl = urlInParts[0] + DREMIO_PROMOTE_BY_PATH_CONSTANT + dremioPath;
        template.uri(fullUrl);
      }
    }
    if (template.url().contains("api/v3/")) {
      return;
    }
    if (authentication instanceof UsernamePasswordAuthenticationToken) {
      template.header(AUTHORIZATION_HEADER, authentication.getCredentials().toString());
      template.header("FeignInvocationUser", authentication.getName());
    }
  }
}
