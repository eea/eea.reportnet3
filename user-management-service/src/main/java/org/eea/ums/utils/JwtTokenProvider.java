package org.eea.ums.utils;

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

  @Value("${eea.security.secret}")
  private String jwtSecret;

  @Value("${eea.security.expiration-time}")
  private int jwtExpirationInMs;

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
