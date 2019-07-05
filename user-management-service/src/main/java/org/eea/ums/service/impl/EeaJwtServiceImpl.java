package org.eea.ums.service.impl;

import org.eea.ums.service.EeaJwtService;
import org.eea.ums.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EeaJwtServiceImpl implements EeaJwtService {

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @Override
  public String generateToken(String username, String password, Object... extraParams) {
    String token = "";
    if (verifyUserExist(username, password)) {
      token = jwtTokenProvider.generateToken(username, password);
    }

    return token;
  }

  private Boolean verifyUserExist(String username, String password) {
    Boolean exists = false;
    if (username.equals("eea")) {
      exists = true;
    }
    return exists;
  }
}
