package org.eea.ums.service.keycloak.admin;

import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.concurrent.ThreadSafe;
import org.eea.ums.service.keycloak.model.TokenInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

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


  /** The keycloak connector service. */
  @Autowired
  private KeycloakConnectorService keycloakConnectorService;

  /** The admin user. */
  @Value("${eea.keycloak.admin.user}")
  private String adminUser;

  /** The admin pass. */
  @Value("${eea.keycloak.admin.password}")
  private String adminPass;

  /** The token expiration time. */
  @Value("${eea.keycloak.admin.token.expiration}")
  private Long tokenExpirationTime;

  /** The last update time. */
  private Long lastUpdateTime = 0l;

  /** The admin token. */
  private String adminToken;

  /** The refresh token. */
  private String refreshToken;

  /**
   * Inits the.
   */
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
  public synchronized String getToken() {
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

  /**
   * Manage token info.
   *
   * @param tokenInfo the token info
   */
  private void manageTokenInfo(TokenInfo tokenInfo) {
    if (null != tokenInfo) {
      this.adminToken = Optional.ofNullable(tokenInfo.getAccessToken()).orElse("");
      this.refreshToken = Optional.ofNullable(tokenInfo.getRefreshToken()).orElse("");
    } else {
      LOG_ERROR.error("Error getting admin access token");
    }
  }
}
