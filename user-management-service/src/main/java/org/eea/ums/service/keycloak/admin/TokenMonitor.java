package org.eea.ums.service.keycloak.admin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import javax.annotation.concurrent.ThreadSafe;
import org.eea.ums.service.keycloak.service.impl.KeycloakConnectorServiceImpl;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The type Token monitor.
 */
@ThreadSafe
@Component
public class TokenMonitor implements DisposableBean {

  @Autowired
  private KeycloakConnectorServiceImpl keycloakConnectorService;
  @Value("${eea.keycloak.admin.user}")
  private String adminUser;
  @Value("${eea.keycloak.admin.password}")
  private String adminPass;
  @Value("${eea.keycloak.admin.token.expiration}")
  private Long tokenExpirationTime;

  private static String adminToken;
  private TokenGeneratorThread tokenGeneratorThread;
  private ExecutorService executor = Executors.newSingleThreadExecutor();

  @PostConstruct
  private void startTokenGeneratorThread() {
    tokenGeneratorThread = new TokenGeneratorThread(keycloakConnectorService, adminUser,
        adminPass, tokenExpirationTime);
    executor.submit(tokenGeneratorThread);
  }


  @Override
  public void destroy() {
    if (tokenGeneratorThread != null) {
      tokenGeneratorThread.stopThread();
    }
    if (!executor.isShutdown()) {
      executor.shutdown();
    }
  }

  /**
   * Update admin token.
   *
   * @param token the token
   */
  synchronized public static void updateAdminToken(String token) {
    adminToken = token;
  }

  /**
   * Gets token.
   *
   * @return the token
   */
  synchronized public static String getToken() {
    return adminToken;
  }
}
