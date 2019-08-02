package org.eea.ums.service.keycloak.admin;

import lombok.extern.slf4j.Slf4j;
import org.eea.ums.service.keycloak.service.impl.KeycloakConnectorServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Token generator thread.
 */
@Slf4j
public class TokenGeneratorThread implements Runnable {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  private KeycloakConnectorServiceImpl keycloakConnectorService;
  private Boolean exit = false;
  private String adminUser;
  private String adminPass;
  private Long tokenExpiration;


  /**
   * Instantiates a new Token generator thread.
   *
   * @param keycloakConnectorService the keycloak connector service
   * @param adminUser the admin user
   * @param adminPass the admin pass
   * @param tokenExpiration the token expiration
   */
  public TokenGeneratorThread(
      KeycloakConnectorServiceImpl keycloakConnectorService, String adminUser, String adminPass,
      Long tokenExpiration) {

    this.keycloakConnectorService = keycloakConnectorService;
    this.adminUser = adminUser;
    this.adminPass = adminPass;
    this.tokenExpiration = tokenExpiration;

  }

  @Override
  public void run() {

    log.info("Starting token generator thread");
    while (!exit) {
      TokenMonitor.updateAdminToken(keycloakConnectorService.generateToken(adminUser, adminPass));
      try {
        Thread.sleep(this.tokenExpiration);
      } catch (InterruptedException e) {
        LOG_ERROR.error(
            "Error sleeping token generator thread during {} miliseconds. Thread will finish", e);
        stopThread();
      }
    }
    log.info("Exited from token generator thread");
  }

  /**
   * Stop thread.
   */
  synchronized public void stopThread() {
    this.exit = true;
    log.info("Finished token generator thread");
  }
}
