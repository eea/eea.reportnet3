package org.eea.security.jwt.utils;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.ums.TokenVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Api key authentication filter.
 */
@Component
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

  private static final String APIKEY_TOKEN = "ApiKey ";
  /**
   * The user management controller zull.
   */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;


  /**
   * Do filter internal.
   *
   * @param request the request
   * @param response the response
   * @param filterChain the filter chain
   *
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String apiKey = getApiKeyFromRequest(request);
    TokenVO token = null;
    try {
      if (StringUtils.hasText(apiKey)) {
        token = userManagementControllerZull.authenticateUserByApiKey(apiKey);
        AuthenticationUtils.performAuthentication(AuthenticationUtils.tokenVO2TokenDataVO(token),
            APIKEY_TOKEN + apiKey);
      }
    } finally {
      filterChain.doFilter(request, response);
    }
  }

  /**
   * Gets the api key from request.
   *
   * @param request the request
   *
   * @return the api key from request
   */
  private String getApiKeyFromRequest(HttpServletRequest request) {
    String apiKeyToken = request.getHeader("Authorization");
    String apiKey = null;
    if (StringUtils.hasText(apiKeyToken) && apiKeyToken.startsWith(APIKEY_TOKEN)) {
      apiKey = apiKeyToken.substring(7, apiKeyToken.length());
    }
    return apiKey;
  }
}

