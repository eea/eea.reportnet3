package org.eea.ums.service.keycloak.admin;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import javax.annotation.concurrent.ThreadSafe;
import lombok.extern.slf4j.Slf4j;
import org.eea.ums.service.keycloak.model.TokenInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The type Token monitor.
 */
@ThreadSafe
@Component
@Slf4j
public class TokenMonitor {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  @Autowired
  private KeycloakConnectorService keycloakConnectorService;
  @Value("${eea.keycloak.admin.user}")
  private String adminUser;
  @Value("${eea.keycloak.admin.password}")
  private String adminPass;
  @Value("${eea.keycloak.admin.token.expiration}")
  private Long tokenExpirationTime;

  private Long lastUpdateTime = 0l;
  private String adminToken;
  private String refreshToken;

  @PostConstruct
  private void init() {
    manageTokenInfo(keycloakConnectorService.generateAdminToken(adminUser, adminPass));
    lastUpdateTime = System.currentTimeMillis();
  }

  /**
   * Gets token.
   *
   * @return the token
   */
  synchronized public String getToken() {
    Long currentTime = System.currentTimeMillis();
    Long difference = currentTime - lastUpdateTime;
    if ((difference) > tokenExpirationTime) {
      log.info("Renewing admin token");
      TokenInfo tokenInfo = null;
      try {
        tokenInfo = keycloakConnectorService.refreshToken(refreshToken);
        log.info("New admin and refresh token generated with values {}", tokenInfo);
      } catch (Exception e) {
        log.warn(
            "Error trying to refresh admin token, using admin credentials to get a new admin token due to {}",
            e.getMessage(), e);
        tokenInfo = keycloakConnectorService.generateAdminToken(adminUser, adminPass);
      }
      manageTokenInfo(tokenInfo);
      lastUpdateTime = currentTime;
      log.info("Admin Token refreshed successfully");
    }
    return adminToken;
  }

  private void manageTokenInfo(TokenInfo tokenInfo) {
    if (null != tokenInfo) {
      this.adminToken = Optional.ofNullable(tokenInfo.getAccessToken()).orElse("");
      this.refreshToken = Optional.ofNullable(tokenInfo.getRefreshToken()).orElse("");
    } else {
      LOG_ERROR.error("Error getting admin access token");
    }
  }
}
