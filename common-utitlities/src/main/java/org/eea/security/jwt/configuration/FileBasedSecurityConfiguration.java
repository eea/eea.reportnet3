package org.eea.security.jwt.configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

/**
 * The type File based security configuration.
 */
@Configuration
public class FileBasedSecurityConfiguration extends SecurityConfiguration {


  /** The env. */
  @Autowired
  private Environment env;

  /** The permitted request. */
  @Value("${eea.security.permittedRequest:@null}")
  private String[] permittedRequest;

  /** The authenticated request. */
  @Value("${eea.security.authenticatedRequest:@null}")
  private String[] authenticatedRequest;

  /** The Constant PROTECTED_REQUEST_PREFIX: {@value}. */
  private static final String PROTECTED_REQUEST_PREFIX = "eea.security.roleProtectedRequest";

  /** The role protected request. */
  private List<Pair<String[], String>> roleProtectedRequest;


  /**
   * Fills role protected request attribute with data got from Application.yml.
   */
  @PostConstruct
  private void fillRoleProtectedRequest() {
    Map<String, Object> map = new HashMap<>();
    for (Iterator it = ((AbstractEnvironment) env).getPropertySources().iterator(); it.hasNext();) {
      PropertySource propertySource = (PropertySource) it.next();
      if (propertySource instanceof MapPropertySource) {
        map.putAll(((MapPropertySource) propertySource).getSource());
      }
    }
    List<String> propertyKeys = map.keySet().stream()
        .filter(key -> key.startsWith(PROTECTED_REQUEST_PREFIX)).collect(Collectors.toList());
    if (null != propertyKeys && !propertyKeys.isEmpty()) {
      roleProtectedRequest = propertyKeys.stream().map(key -> {
        String[] splittedKey = key.split("\\.");
        String role = splittedKey[splittedKey.length - 1];
        Pair<String[], String> pair = new ImmutablePair(map.get(key).toString().split(","), role);
        return pair;
      }).collect(Collectors.toList());
    }
  }

  /**
   * Gets the authenticated request.
   *
   * @return the authenticated request
   */
  @Override
  protected String[] getAuthenticatedRequest() {
    return Arrays.copyOf(authenticatedRequest, authenticatedRequest.length);
  }

  /**
   * Gets the permitted request.
   *
   * @return the permitted request
   */
  @Override
  protected String[] getPermittedRequest() {
    return Arrays.copyOf(permittedRequest, permittedRequest.length);
  }

  /**
   * Gets the role protected request.
   *
   * @return the role protected request
   */
  @Override
  protected List<Pair<String[], String>> getRoleProtectedRequest() {
    return roleProtectedRequest;
  }
}


