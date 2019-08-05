package org.eea.utils;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestUtils {

  public static String generateToken(Map<String, Object> keys, Long milliseconds, String subject) {

    PrivateKey privateKey = (PrivateKey) keys.get("private");
    JwtBuilder builder = Jwts.builder();
    builder.setIssuedAt(new Date()).setIssuer("test")
        .setExpiration(new Date(milliseconds))
        .signWith(SignatureAlgorithm.RS256, privateKey);
    if (null != subject) {
      builder.setSubject("user1");
    }

    return builder.compact();
  }

  public static Map<String, Object> getRSAKeys() throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(512);
    KeyPair keyPair = keyPairGenerator.generateKeyPair();
    PrivateKey privateKey = keyPair.getPrivate();
    PublicKey publicKey = keyPair.getPublic();
    Map<String, Object> keys = new HashMap<>();
    keys.put("private", privateKey);
    keys.put("public", publicKey);
    return keys;
  }
}
