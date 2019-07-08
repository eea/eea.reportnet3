package org.eea.ums.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.security.jwt.utils.JwtTokenProvider;
import org.eea.ums.service.EeaJwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class EeaJwtServiceImpl implements EeaJwtService {

  @Value("${eea.security.secret}")
  private String jwtSecret;

  @Value("${eea.security.expiration-time}")
  private int jwtExpirationInMs;


  @Override
  public String generateToken(String username, String password, Object... extraParams) {
    String token = "";
    if (verifyUserExist(username, password)) {
      token = generateToken(username, password);
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


  public String generateToken(Authentication authentication) {

    //TODO OJO ESTE UserDetails es el token. Hay que hacer una clase que implemente UserDetails para dar soporte
    //TODO REVISAR https://www.callicoder.com/spring-boot-spring-security-jwt-mysql-react-app-part-2/
    EeaUserDetails userPrincipal = (EeaUserDetails) authentication.getPrincipal();

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

    return Jwts.builder()
        .setSubject(userPrincipal.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(expiryDate)
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact();
  }

  public String generateToken(String username, String password) {

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(expiryDate)
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact();
  }


}
