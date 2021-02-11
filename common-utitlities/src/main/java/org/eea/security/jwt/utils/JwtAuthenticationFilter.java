package org.eea.security.jwt.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eea.security.jwt.data.TokenDataVO;
import org.keycloak.common.VerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Jwt authentication filter.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_TOKEN = "Bearer ";

  /**
   * The token provider.
   */
  @Autowired
  private JwtTokenProvider tokenProvider;

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

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
    if (!StringUtils.isEmpty(jwt) && jwt.startsWith("FEING_")) {
      String feignInvocationUser = request.getHeader("FeignInvocationUser");
      String feignInvocationUserId = request.getHeader("FeignInvocationUserId");
      createFeignSecurity(feignInvocationUser, feignInvocationUserId);
    } else {
      try {
        TokenDataVO token = null;
        if (StringUtils.hasText(jwt) && (token = tokenProvider.retrieveToken(jwt)) != null) {
          AuthenticationUtils.performAuthentication(token, BEARER_TOKEN + jwt);
        }
      } catch (VerificationException e) {
        // before showing error check if invocation came from feign client and toke was dued during
        // the previous process
        String feignInvocationUser = request.getHeader("FeignInvocationUser");
        String feignInvocationUserId = request.getHeader("FeignInvocationUserId");

        if (!StringUtils.isEmpty(feignInvocationUser)) {
          createFeignSecurity(feignInvocationUser, feignInvocationUserId);
        } else {
          LOG_ERROR.error(
              "Could not set authentication security context: uri={}, token={}, feignInvocationUser={}",
              request.getRequestURI(), jwt, request.getHeader("FeignInvocationUser"), e);
        }
      }
    }

    filterChain.doFilter(request, response);
  }

  private void createFeignSecurity(String feignInvocationUser, String feignInvocationUserId) {
    log.info(
        "Invocation came from a feign client, setting security context with user {} and user id {} ",
        feignInvocationUser, feignInvocationUserId);
    Set<String> authorities = new HashSet<>();
    authorities.add("FEIGN");
    UserDetails userDetails = EeaUserDetails.create(feignInvocationUser, authorities);

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, feignInvocationUserId);
    authentication.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  /**
   * Gets the jwt from request.
   *
   * @param request the request
   *
   * @return the jwt from request
   */
  private String getJwtFromRequest(HttpServletRequest request) {
    String jwt = request.getHeader("FeignInvocationUser");
    if (StringUtils.isEmpty(jwt)) {// if invocation comes from outside then it needs to be
                                   // authenticated
      String bearerToken = request.getHeader("Authorization");

      if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_TOKEN)) {
        jwt = bearerToken.substring(7, bearerToken.length());
      }
    } else {// if FeignInvocationUser comes then let it go
      jwt = "FEING_" + jwt;
    }

    return jwt;
  }
}

