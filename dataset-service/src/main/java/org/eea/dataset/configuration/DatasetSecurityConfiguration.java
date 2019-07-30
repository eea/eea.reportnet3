package org.eea.dataset.configuration;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eea.security.jwt.configuration.SecurityConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasetSecurityConfiguration extends SecurityConfiguration {


  @Override
  protected String[] getAuthenticatedRequest() {
    return new String[0];
  }

  @Override
  protected String[] getPermittedRequest() {
    return new String[]{"/dataset/**"};
  }

  @Override
  protected List<Pair<String[], String>> getRoleProtectedRequest() {

    return null;
  }
}


