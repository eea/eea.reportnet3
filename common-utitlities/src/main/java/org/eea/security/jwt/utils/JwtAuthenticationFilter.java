package org.eea.security.jwt.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

  /** The token provider. */
  @Autowired
  private JwtTokenProvider tokenProvider;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Do filter internal.
   *
   * @param request the request
   * @param response the response
   * @param filterChain the filter chain
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    try {
      String jwt = getJwtFromRequest(request);
      TokenDataVO token = null;
      if (StringUtils.hasText(jwt) && (token = tokenProvider.retrieveToken(jwt)) != null) {
        String username = token.getPreferredUsername();
        Map<String, Object> otherClaims = token.getOtherClaims();

        Set<String> roles = token.getRoles();
        List<String> groups = (List<String>) otherClaims.get("user_groups");
        if (null != groups && !groups.isEmpty()) {
          groups.stream().map(group -> {
            if (group.startsWith("/")) {
              group = group.substring(1);
            }
            return group.toUpperCase();
          }).forEach(roles::add);
        }
        UserDetails userDetails = EeaUserDetails.create(username, roles);

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, jwt, userDetails.getAuthorities());
        Map<String, String> details = new HashMap<>();
        details.put(AuthenticationDetails.USER_ID, token.getUserId());
        authentication.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (VerificationException e) {
      //before showing error check if invocation came from feign client and toke was dued during the previous process
      String feignInvocationUser = request.getHeader("FeignInvocationUser");
      String feignInvocationUserId = request.getHeader("FeignInvocationUserId");

      if (!StringUtils.isEmpty(feignInvocationUser)) {
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
      } else {
        LOG_ERROR.error("Could not set user authentication in security context", e);
      }
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Gets the jwt from request.
   *
   * @param request the request
   * @return the jwt from request
   */
  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    String jwt = null;
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      jwt = bearerToken.substring(7, bearerToken.length());
    }
    return jwt;
  }
}

