package org.eea.security.jwt.utils;

import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.ums.TokenVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * The type Api key authentication filter.
 */
@Component
@Slf4j
public class ExternalJwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String JWT_TOKEN = "JWT ";
  /**
   * The user management controller zull.
   */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

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
    String jwt = getJwtFromRequest(request);
    TokenVO token = null;
    try {
      if (StringUtils.hasText(jwt)) {
        String userEmail = this.jwtTokenProvider.retrieveUserEmail(jwt);
        if (!StringUtils.isEmpty(userEmail)) {
          token = userManagementControllerZull.authenticateUserByEmail(
              URLEncoder.encode(userEmail, "UTF-8"));
          if (token != null) {
            AuthenticationUtils
                .performAuthentication(AuthenticationUtils.tokenVO2TokenDataVO(token),
                    JWT_TOKEN + jwt);
          }
        }
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
  private String getJwtFromRequest(HttpServletRequest request) {
    String jwtToken = request.getHeader("Authorization");
    String jwt = null;
    if (StringUtils.hasText(jwtToken) && jwtToken.startsWith(JWT_TOKEN)) {
      jwt = jwtToken.replace(JWT_TOKEN, "");
    }
    return jwt;
  }
}

