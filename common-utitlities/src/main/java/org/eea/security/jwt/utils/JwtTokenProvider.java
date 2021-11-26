package org.eea.security.jwt.utils;

import static org.keycloak.TokenVerifier.IS_ACTIVE;
import static org.keycloak.TokenVerifier.SUBJECT_EXISTS_CHECK;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.eea.security.jwt.data.CacheTokenVO;
import org.eea.security.jwt.data.TokenDataVO;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Jwt token provider.
 */
@Component

/** The Constant log. */
@Slf4j
public class JwtTokenProvider {


  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The public key value. */
  @Value("${eea.keycloak.publicKey}")
  private String publicKeyValue;

  /** The public key. */
  private PublicKey publicKey;

  /** The external public key values. */
  @Value("${eea.external.publicKeys:#{null}}")
  private List<String> externalPublicKeyValues;

  /** The external public keys. */
  private Map<String, PublicKey> externalPublicKeys;


  /** The security redis template. */
  @Autowired
  @Qualifier("securityRedisTemplate")
  private RedisTemplate<String, CacheTokenVO> securityRedisTemplate;

  /**
   * Create public key.
   *
   * @throws NoSuchAlgorithmException the no such algorithm exception
   * @throws InvalidKeySpecException the invalid key spec exception
   * @throws CertificateException the certificate exception
   */
  @PostConstruct
  private void createPublicKey()
      throws NoSuchAlgorithmException, InvalidKeySpecException, CertificateException {
    this.externalPublicKeys = new HashMap<>();
    // Configure publicKey for Keycloak as it is the main Token Generator
    this.publicKey = retrievePubliKeyFromString(publicKeyValue);

    // Configure the rest of public keys the external systems in which Reportnet is going to trust
    // Token Generators
    if (null != externalPublicKeyValues && externalPublicKeyValues.size() > 0) {
      for (String providerKey : externalPublicKeyValues) {
        String[] providerKeyArray = providerKey.split(":");
        String issuer = providerKeyArray[0];
        String pkValue = providerKeyArray[1];

        if (pkValue.startsWith("-----BEGIN CERTIFICATE-----")
            && pkValue.endsWith("-----END CERTIFICATE-----")) {
          // is a Certificate
          this.externalPublicKeys.put(issuer,
              this.retrievePubliKeyFromCertifiate(
                  pkValue.replace("-----BEGIN CERTIFICATE-----", "-----BEGIN CERTIFICATE-----\n")
                      .replace("-----END CERTIFICATE-----", "\n-----END CERTIFICATE-----")));
        } else {
          this.externalPublicKeys.put(issuer, this.retrievePubliKeyFromString(pkValue));
        }
      }
    }
  }


  /**
   * Retrieve token json web token from token cache.
   *
   * @param authToken the auth token
   *
   * @return the json web token
   *
   * @throws VerificationException the verification exception
   */
  public TokenDataVO retrieveToken(String authToken) throws VerificationException {
    String cachedToken = retrieveAccessToken(authToken);

    if (StringUtils.hasText(cachedToken)) {
      return parseToken(cachedToken);
    } else {
      throw new VerificationException("Error: Could not retrieve token from Cache");
    }
  }

  /**
   * Parse a JWT Token extracting the more relevant data from the JWT into a TokenDataVO.
   *
   * @param jwt the jwt token
   *
   * @return the token data vo
   *
   * @throws VerificationException the verification exception
   * @see TokenDataVO
   */
  public TokenDataVO parseToken(String jwt) throws VerificationException {

    TokenVerifier tokenVerifier = TokenVerifier.create(jwt, AccessToken.class).publicKey(publicKey)
        .withChecks(SUBJECT_EXISTS_CHECK, IS_ACTIVE).verify();

    AccessToken token = (AccessToken) tokenVerifier.getToken();

    TokenDataVO tokenDataVO = new TokenDataVO();
    tokenDataVO.setOtherClaims(token.getOtherClaims());
    tokenDataVO.setPreferredUsername(token.getPreferredUsername());
    tokenDataVO.setUserId(token.getSubject());
    if (null != token.getRealmAccess()) {
      tokenDataVO.setRoles(token.getRealmAccess().getRoles());
    }
    Date tokenTime =
        new Date(TimeUnit.MILLISECONDS.convert(token.getExpiration(), TimeUnit.SECONDS));
    Date timeNow = new Date();
    Long diff = tokenTime.getTime() - timeNow.getTime();// it should be 5 minutes as initial time
    tokenDataVO.setExpiration(diff.intValue());
    return tokenDataVO;
  }

  /**
   * Parse a token generated by an external trusted application to retrieve the user email.
   *
   * @param jwt the jwt
   *
   * @return the user's email
   */
  public String retrieveUserEmail(String jwt) {

    int endBodyIndex = jwt.lastIndexOf('.');
    String withoutSignature = jwt.substring(0, endBodyIndex + 1);
    Jwt<Header, Claims> untrusted = Jwts.parser().parseClaimsJwt(withoutSignature);
    PublicKey publicKey = this.externalPublicKeys.get(untrusted.getBody().getIssuer());
    String email = null;
    if (publicKey == null) {
      log.warn("No public key for issuer {}", untrusted.getBody().getIssuer());
    } else {
      Jwt<Header, Claims> parsedToken = Jwts.parser()
          .setSigningKey(this.externalPublicKeys.get(untrusted.getBody().getIssuer())).parse(jwt);
      email = parsedToken.getBody().get("email").toString();
    }
    return email;
  }

  /**
   * Retrieve access token string from redis if exists, otherwise retrieves keyToken input
   * parameter.
   *
   * @param keyToken the key token
   *
   * @return the string
   */
  public String retrieveAccessToken(String keyToken) {
    CacheTokenVO result = securityRedisTemplate.opsForValue().get(keyToken);

    return null != result ? result.getAccessToken() : keyToken;
  }

  /**
   * Retrieve publi key from certifiate.
   *
   * @param certificate the certificate
   * @return the public key
   * @throws CertificateException the certificate exception
   */
  private PublicKey retrievePubliKeyFromCertifiate(String certificate) throws CertificateException {
    byte[] certBytes = certificate.getBytes(java.nio.charset.StandardCharsets.UTF_8);

    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    InputStream in = new ByteArrayInputStream(certBytes);
    X509Certificate x509Certificate = (X509Certificate) certFactory.generateCertificate(in);

    return x509Certificate.getPublicKey();
  }

  /**
   * Retrieve publi key from string.
   *
   * @param publicKeyValue the public key value
   * @return the public key
   * @throws CertificateException the certificate exception
   * @throws NoSuchAlgorithmException the no such algorithm exception
   * @throws InvalidKeySpecException the invalid key spec exception
   */
  private PublicKey retrievePubliKeyFromString(String publicKeyValue)
      throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
    X509EncodedKeySpec X509publicKey =
        new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyValue.getBytes()));
    KeyFactory kf = null;
    kf = KeyFactory.getInstance("RSA");
    return kf.generatePublic(X509publicKey);
  }
}
