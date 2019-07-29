package org.eea.ums.configuration;


import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.eea.security.jwt.configuration.SecurityConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * The type Security configuration.
 */
@Configuration
public class UmsSecurityConfiguration extends SecurityConfiguration {

  @Override
  protected String[] getAuthenticatedRequest() {
    return null;
  }

  @Override
  protected String[] getPermitedRequest() {
    return null;
  }

  @Override
  protected List<Pair<String[], String>> getRoleProtectedRequest() {
    return null;
  }
}
