package org.eea.security.jwt.utils;

import static org.keycloak.TokenVerifier.IS_ACTIVE;
import static org.keycloak.TokenVerifier.SUBJECT_EXISTS_CHECK;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.TokenVerifier;
import org.keycloak.TokenVerifier.TokenTypeCheck;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtTokenProvider {


  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  @Value("${eea.keycloak.publicKey}")
  private String publicKeyValue;

  private PublicKey publicKey;

  @PostConstruct
  public void createPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {

    X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(
        Base64.getDecoder().decode(publicKeyValue.getBytes()));
    KeyFactory kf = null;
    kf = KeyFactory.getInstance("RSA");
    this.publicKey = kf.generatePublic(X509publicKey);

  }

  public String getUserIdFromJWT(String token) {
   /* Claims claims = Jwts.parser()
        .setSigningKey(jwtSecret)
        .parseClaimsJws(token)
        .getBody();

    return claims.getSubject();*/
    return "user1";
  }

  public boolean validateToken(String authToken) {

    boolean validated = false;
    try {

      TokenVerifier token = TokenVerifier.create(authToken, AccessToken.class).publicKey(publicKey)
          .withChecks(SUBJECT_EXISTS_CHECK,
              IS_ACTIVE)
          .verify();

      validated = true;
    } catch (
        VerificationException e) {
      e.printStackTrace();
    }
    return validated;
  }
}
