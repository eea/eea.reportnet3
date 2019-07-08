package org.eea.ums.configuration;


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
    return new String[]{"/user/test-security"};
  }

  @Override
  protected String[] getPermitedRequest() {
    return new String[]{"/user/generateToken/*"};
  }

  @Override
  protected List<Pair<String[], String>> getRoleProtectedRequest() {
    return null;
  }
}
