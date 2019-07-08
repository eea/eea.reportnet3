package org.eea.security.jwt.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtTokenProvider {

  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");
  //TODO Pending on decide what engine will be done to generate JWT (Declare o Keycloak)
  @Value("${eea.security.secret:JWTSuperSecretKey}")
  private String jwtSecret;

  public String getUserIdFromJWT(String token) {
    Claims claims = Jwts.parser()
        .setSigningKey(jwtSecret)
        .parseClaimsJws(token)
        .getBody();

    return claims.getSubject();
  }

  public boolean validateToken(String authToken) {
    try {
      Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
      return true;
    } catch (SignatureException ex) {
      LOG_ERROR.error("Invalid JWT signature");
    } catch (MalformedJwtException ex) {
      LOG_ERROR.error("Invalid JWT token");
    } catch (ExpiredJwtException ex) {
      LOG_ERROR.error("Expired JWT token");
    } catch (UnsupportedJwtException ex) {
      LOG_ERROR.error("Unsupported JWT token");
    } catch (IllegalArgumentException ex) {
      LOG_ERROR.error("JWT claims string is empty.");
    }
    return false;
  }
}
