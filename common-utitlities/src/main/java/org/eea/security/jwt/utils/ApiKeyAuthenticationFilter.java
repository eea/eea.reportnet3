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
import lombok.extern.slf4j.Slf4j;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.thread.ThreadPropertiesManager;
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

/**
 * The type Api key authentication filter.
 */
@Component
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String apiKey = getApiKeyFromRequest(request);
    TokenVO token = null;
    try {
      if (StringUtils.hasText(apiKey)) {
        token = userManagementControllerZull.authenticateUserByApiKey(apiKey);
        String username = token.getPreferredUsername();
        Set<String> roles = token.getRoles();
        Set<String> groups = token.getGroups();
        if (null != groups && groups.size() > 0) {
          groups.stream().map(group -> {
            if (group.startsWith("/")) {
              group = group.substring(1);
            }
            return group.toUpperCase();
          }).forEach(roles::add);
        }
        UserDetails userDetails = EeaUserDetails.create(username, roles);

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, apiKey,
                userDetails.getAuthorities());
        Map<String, String> details = new HashMap<>();
        details.put("userId", token.getUserId());
        authentication.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        ThreadPropertiesManager.setVariable("user", authentication.getName());
      }
    } finally {
      filterChain.doFilter(request, response);
    }

  }

  private String getApiKeyFromRequest(HttpServletRequest request) {
    String apiKeyToken = request.getHeader("Authorization");
    String apiKey = null;
    if (StringUtils.hasText(apiKeyToken) && apiKeyToken.startsWith("ApiKey ")) {
      apiKey = apiKeyToken.substring(7, apiKeyToken.length());
    }
    return apiKey;
  }
}

