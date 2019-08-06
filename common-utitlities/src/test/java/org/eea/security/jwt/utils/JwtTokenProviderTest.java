package org.eea.security.jwt.utils;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import org.eea.utils.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.common.VerificationException;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class JwtTokenProviderTest {

  private static final String SUPER_SECRET_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsojJChk5UfJOHuOLQ1SZtYB5JzSrWRkJ3gBMGryHGDgAGqjf+gHNAwHswVbx6k0deKr4HSdrD+VIkkM1AGC/CNa/STI1ir4zmYQPpewAcKvM7BF/CJlwMjV6c9GiJutC7h58DRFtdgz4O2KVqDkZ5l/pZeKKxnEXK5M8x+HmIqD+F3+BWgCIJF7Tcu1sFnWjJWKO5yyAPXyq7YYF59tV7JPcfoJqPMqsvxNqP48K19RQN9ndu1qfXmLONLYhU0rdeDpERXYLoPGpchYL5ZCd6Dt+RpDpc2FnOis42DJD/ev2/LWSUugZFZx6Zqfh4nWhzlYPrnfiSFyxZEX9OY8eIQIDAQAB";


  private JwtTokenProvider jwtTokenProvider;

  @Before
  public void init() {
    this.jwtTokenProvider = new JwtTokenProvider();

  }

  @Test
  public void createPublicKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
    ReflectionTestUtils.setField(jwtTokenProvider, "publicKeyValue", SUPER_SECRET_PUBLIC_KEY);
    ReflectionTestUtils.invokeMethod(jwtTokenProvider, "createPublicKey");
    Assert.assertNotNull(ReflectionTestUtils.getField(jwtTokenProvider, "publicKey"));
  }

  @Test(expected = InvalidKeySpecException.class)
  public void createPublicKeyKoInvalidKeySpecException()
      throws InvalidKeySpecException, NoSuchAlgorithmException {
    ReflectionTestUtils.setField(jwtTokenProvider, "publicKeyValue", "KO");
    try {
      ReflectionTestUtils.invokeMethod(jwtTokenProvider, "createPublicKey");
    } finally {
      Assert.assertNull(ReflectionTestUtils.getField(jwtTokenProvider, "publicKey"));
    }

  }


  @Test
  public void retrieveToken()
      throws NoSuchAlgorithmException, VerificationException {
    Map<String, Object> keys = TestUtils.getRSAKeys();
    ReflectionTestUtils.setField(jwtTokenProvider, "publicKey", keys.get("public"));

    Assert.assertNotNull(this.jwtTokenProvider
        .retrieveToken(TestUtils.generateToken(keys, System.currentTimeMillis() + 1000, "user1")));
  }

  @Test(expected = VerificationException.class)
  public void retrieveTokenKoInactive()
      throws InvalidKeySpecException, NoSuchAlgorithmException, VerificationException {
    Map<String, Object> keys = TestUtils.getRSAKeys();
    ReflectionTestUtils.setField(jwtTokenProvider, "publicKey", keys.get("public"));

    Assert.assertNotNull(
        this.jwtTokenProvider
            .retrieveToken(
                TestUtils.generateToken(keys, System.currentTimeMillis() - 1000, "user1")));
  }

  @Test(expected = VerificationException.class)
  public void retrieveTokenKoNoSubject()
      throws InvalidKeySpecException, NoSuchAlgorithmException, VerificationException {
    Map<String, Object> keys = TestUtils.getRSAKeys();
    ReflectionTestUtils.setField(jwtTokenProvider, "publicKey", keys.get("public"));

    Assert.assertNotNull(
        this.jwtTokenProvider
            .retrieveToken(TestUtils.generateToken(keys, System.currentTimeMillis() + 1000, null)));
  }


}