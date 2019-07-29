package org.eea.recordstore.configuration;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eea.security.jwt.configuration.SecurityConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecordStoreSecurityConfiguration extends SecurityConfiguration {


  @Override
  protected String[] getAuthenticatedRequest() {
    return new String[]{"/recordstore/prueba"};
  }

  @Override
  protected String[] getPermitedRequest() {
    return new String[]{"/recordstore/connections"};
  }

  @Override
  protected List<Pair<String[], String>> getRoleProtectedRequest() {

    List<Pair<String[], String>> allowedRoles = new ArrayList<>();
    Pair<String[], String> allowedRoleProvider = new ImmutablePair<>(
        new String[]{"/recordstore/connection/*"}, "PROVIDER");
    Pair<String[], String> allowedRoleRequestor = new ImmutablePair<>(
        new String[]{"/recordstore/dataset/create/*"}, "REQUESTOR");
    allowedRoles.add(allowedRoleProvider);
    allowedRoles.add(allowedRoleRequestor);
    return allowedRoles;
  }
}


