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
import lombok.extern.slf4j.Slf4j;

/**
 * The type Jwt token provider.
 */
@Component
@Slf4j
public class JwtTokenProvider {


  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  @Value("${eea.keycloak.publicKey}")
  private String publicKeyValue;

  private PublicKey publicKey;

  @Autowired
  @Qualifier("securityRedisTemplate")
  private RedisTemplate<String, CacheTokenVO> securityRedisTemplate;

  /**
   * Create public key.
   *
   * @throws NoSuchAlgorithmException the no such algorithm exception
   * @throws InvalidKeySpecException the invalid key spec exception
   */
  @PostConstruct
  private void createPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {

    X509EncodedKeySpec X509publicKey =
        new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyValue.getBytes()));
    KeyFactory kf = null;
    kf = KeyFactory.getInstance("RSA");
    this.publicKey = kf.generatePublic(X509publicKey);

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

    TokenDataVO tokenDataVO = new TokenDataVO();
    if (StringUtils.hasText(cachedToken)) {
      tokenDataVO = parseToken(cachedToken);
    } else {
      throw new VerificationException("Error: Could not retrieve token from Cache");
    }
    return tokenDataVO;
  }

  /**
   * Parse a JWT Token extracting the more relevant data from the JWT into a TokenDataVO.
   *
   * @param jwt the jwt token
   *
   * @return the token data vo
   *
   * @throws VerificationException the verification exception
   * @see org.eea.security.jwt.data.TokenDataVO
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
    tokenDataVO.setExpiration(token.getExpiration());
    return tokenDataVO;
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
}
