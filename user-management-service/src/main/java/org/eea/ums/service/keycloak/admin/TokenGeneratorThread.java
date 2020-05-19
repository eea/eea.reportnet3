package org.eea.ums.service.keycloak.admin;

import java.util.Optional;
import org.eea.ums.service.keycloak.model.TokenInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Token generator thread. This Thread will refresh the admin token based on the frenquency
 * passed as parameter
 */
@Slf4j
public class TokenGeneratorThread implements Runnable {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  private KeycloakConnectorService keycloakConnectorService;
  private Boolean exit = false;
  private String adminUser;
  private String adminPass;
  private Long tokenExpiration;
  private String refreshToken;


  /**
   * Instantiates a new Token generator thread.
   *
   * @param keycloakConnectorService the keycloak connector service
   * @param adminUser the admin user
   * @param adminPass the admin pass
   * @param tokenExpiration the token expiration
   */
  public TokenGeneratorThread(KeycloakConnectorService keycloakConnectorService, String adminUser,
      String adminPass, Long tokenExpiration) {

    this.keycloakConnectorService = keycloakConnectorService;
    this.adminUser = adminUser;
    this.adminPass = adminPass;
    this.tokenExpiration = tokenExpiration;

  }

  @Override
  public void run() {

    log.info("Starting token generator thread");
    // First attemp to retrieve an admin token during ums initialization
    TokenInfo firstToken = keycloakConnectorService.generateAdminToken(adminUser, adminPass);
    if (null != firstToken) {
      manageTokenInfo(firstToken);
    }
    // from this point on the thread will be retrieving admin token (using refresh tokens) every
    // tokenExpiration ms
    while (!exit) {
      TokenInfo tokenInfo = keycloakConnectorService.refreshToken(refreshToken);
      manageTokenInfo(tokenInfo);
      log.info("Token refreshed. Token info: {}", tokenInfo);
    }
    log.info("Exited from token generator thread");
  }

  private void manageTokenInfo(TokenInfo tokenInfo) {
    if (null != tokenInfo) {
      String accessToken = Optional.ofNullable(tokenInfo.getAccessToken()).orElse("");
      this.refreshToken = Optional.ofNullable(tokenInfo.getRefreshToken()).orElse("");
      TokenMonitor.updateAdminToken(accessToken);
      sleepThread(this.tokenExpiration);
    } else {
      LOG_ERROR.error("Error getting admin access token, finishing Token Generator thread ");
      Thread.currentThread().interrupt();
      stopThread();
    }
  }

  private void sleepThread(Long time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      LOG_ERROR.error(
          "Error sleeping token generator thread during {} miliseconds. Thread will finish", e);
      stopThread();
    }
  }

  /**
   * Stop thread.
   */
  public void stopThread() {
    this.exit = true;
    log.info("Finished token generator thread");
  }
}
