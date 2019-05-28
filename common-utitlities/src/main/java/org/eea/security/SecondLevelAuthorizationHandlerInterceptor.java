package org.eea.security;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * The Class SecondLevelAuthorizationHandlerInterceptor.
 */
public class SecondLevelAuthorizationHandlerInterceptor extends HandlerInterceptorAdapter {

  /** The Constant LOG. */
  private static final Logger LOG =
      LoggerFactory.getLogger(SecondLevelAuthorizationHandlerInterceptor.class);

  /**
   * Pre handle.
   *
   * @param request the request
   * @param response the response
   * @param handler the handler
   * @return true, if successful
   * @throws Exception the exception
   */
  @Override
  public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
      final Object handler) throws Exception {
    final Principal principal = request.getUserPrincipal();
    if (null != principal) {
      LOG.info("Retrieved user {}", principal.getName());
    } else {
      LOG.info("No user retrieved");
    }
    return true;
  }

}
