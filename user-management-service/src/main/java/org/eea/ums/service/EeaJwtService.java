package org.eea.ums.service;

/**
 * The interface Eea jwt service.
 */
public interface EeaJwtService {

  /**
   * Generate token string.
   *
   * @param username the username
   * @param password the password
   * @param extraParams the extra params
   *
   * @return the string
   */
  String generateToken(String username, String password, Object... extraParams);
}
