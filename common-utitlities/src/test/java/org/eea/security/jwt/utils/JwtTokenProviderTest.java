package org.eea.security.jwt.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtHandler;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eea.security.jwt.data.CacheTokenVO;
import org.eea.utils.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.common.VerificationException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class JwtTokenProviderTest {

  private static final String SUPER_SECRET_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsojJChk5UfJOHuOLQ1SZtYB5JzSrWRkJ3gBMGryHGDgAGqjf+gHNAwHswVbx6k0deKr4HSdrD+VIkkM1AGC/CNa/STI1ir4zmYQPpewAcKvM7BF/CJlwMjV6c9GiJutC7h58DRFtdgz4O2KVqDkZ5l/pZeKKxnEXK5M8x+HmIqD+F3+BWgCIJF7Tcu1sFnWjJWKO5yyAPXyq7YYF59tV7JPcfoJqPMqsvxNqP48K19RQN9ndu1qfXmLONLYhU0rdeDpERXYLoPGpchYL5ZCd6Dt+RpDpc2FnOis42DJD/ev2/LWSUugZFZx6Zqfh4nWhzlYPrnfiSFyxZEX9OY8eIQIDAQAB";


  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private RedisTemplate<String, CacheTokenVO> securityRedisTemplate;

  @Before
  public void init() {
    this.jwtTokenProvider = new JwtTokenProvider();

  }

  @Test
  public void createPublicKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
    ReflectionTestUtils.setField(jwtTokenProvider, "publicKeyValue", SUPER_SECRET_PUBLIC_KEY);
    ReflectionTestUtils.setField(jwtTokenProvider, "securityRedisTemplate", securityRedisTemplate);
    ReflectionTestUtils.invokeMethod(jwtTokenProvider, "createPublicKey");
    Assert.assertNotNull(ReflectionTestUtils.getField(jwtTokenProvider, "publicKey"));
    Mockito.reset(securityRedisTemplate);
  }

  @Test(expected = InvalidKeySpecException.class)
  public void createPublicKeyKoInvalidKeySpecException()
      throws Throwable {
    ReflectionTestUtils.setField(jwtTokenProvider, "publicKeyValue", "KO");
    try {
      ReflectionTestUtils.invokeMethod(jwtTokenProvider, "createPublicKey");
    } catch (UndeclaredThrowableException e) {
      throw e.getCause();
    } finally {
      Assert.assertNull(ReflectionTestUtils.getField(jwtTokenProvider, "publicKey"));
    }

  }


  @Test
  public void retrieveToken()
      throws NoSuchAlgorithmException, VerificationException {
    Map<String, Object> keys = TestUtils.getRSAKeys();
    ReflectionTestUtils.setField(jwtTokenProvider, "publicKey", keys.get("public"));
    ReflectionTestUtils.setField(jwtTokenProvider, "securityRedisTemplate", securityRedisTemplate);
    ValueOperations<String, CacheTokenVO> operations = Mockito.mock(ValueOperations.class);
    CacheTokenVO tokenVO = new CacheTokenVO();
    tokenVO
        .setAccessToken(TestUtils.generateToken(keys, System.currentTimeMillis() + 10000, "user1"));
    Mockito.when(operations.get("auxUUID")).thenReturn(tokenVO);
    Mockito.when(securityRedisTemplate.opsForValue()).thenReturn(operations);

    Assert.assertNotNull(this.jwtTokenProvider
        .retrieveToken("auxUUID"));
  }

  @Test(expected = VerificationException.class)
  public void retrieveTokenKoInactive()
      throws InvalidKeySpecException, NoSuchAlgorithmException, VerificationException {
    ReflectionTestUtils.setField(jwtTokenProvider, "securityRedisTemplate", securityRedisTemplate);
    Map<String, Object> keys = TestUtils.getRSAKeys();
    ReflectionTestUtils.setField(jwtTokenProvider, "publicKey", keys.get("public"));
    ValueOperations<String, CacheTokenVO> operations = Mockito.mock(ValueOperations.class);
    Mockito.when(operations.get(Mockito.anyString())).thenReturn(null);
    Mockito.when(securityRedisTemplate.opsForValue()).thenReturn(operations);

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
    ReflectionTestUtils.setField(jwtTokenProvider, "securityRedisTemplate", securityRedisTemplate);
    ValueOperations<String, CacheTokenVO> operations = Mockito.mock(ValueOperations.class);
    CacheTokenVO tokenVO = new CacheTokenVO();
    tokenVO
        .setAccessToken(TestUtils.generateToken(keys, System.currentTimeMillis() + 1000, null));
    Mockito.when(operations.get("auxUUID")).thenReturn(tokenVO);
    Mockito.when(securityRedisTemplate.opsForValue()).thenReturn(operations);

    Assert.assertNotNull(this.jwtTokenProvider
        .retrieveToken("auxUUID"));
  }

  private final String SECRET = "mySecretKey";

//  @Test
//  public void testJsonWebTokenGetIssuer()
//      throws NoSuchAlgorithmException, InvalidKeySpecException, CertificateException {
//    String token = "eyJhbGciOiJSUzUxMiJ9.eyJ1aWQiOiJ0ZXN0IiwiY291bnRyeSI6InRlc3QiLCJmaXJzdE5hbWUiOiJ0ZXN0IiwibGFzdE5hbWUiOiJ0ZXN0Iiwicm9sZSI6InRlc3QiLCJuYmYiOjE1OTgzNDkwODIsImlzcyI6IlNFUCIsImV4cCI6MTU5ODM1MDg4MiwiaWF0IjoxNTk4MzQ5MDgyLCJlbWFpbCI6InBlcGVAcGVwZS5jb20ifQ.JnYU6AwOmHkVIn11HkeGO9Q0Rvf4DHxTM_rr5jN_2DXJbE9Vs1ysYaQCuTbLL8E4hdySfN8WMzP7OInEkz7Xm5XRGw5ZSf60VOOGCOs4ywvniXMGikZyhMg_oEIg8WfrgzE3ZOz9b6Ab0vpIPMYFJylvl3RwN1l4FYUfJpGjHJDViPv9goCyF-VgfI7zbvhS_C0iMCbM4Tyjd764C0Q_zj31KzWjR7P5ijm2clhK23DLw-YIEdBppWSqlO6gKZ8KeVknylN4bR7fFQSgPQVimeibyX_TtXJToCwsLaBA43xNbR15nGS1RyvHWo663IIaTRNx8RPktI4sxJmtHBzLLQ";
//    PublicKey publicKey = createPublicKey(
//        "MIIDPTCCAiWgAwIBAgIELKfsgzANBgkqhkiG9w0BAQsFADBPMRAwDgYDVQQGEwdCZWxnaXVtMREwDwYDVQQHEwhCcnVzc2VsczEMMAoGA1UEChMDRVVDMQwwCgYDVQQLEwNFVUMxDDAKBgNVBAMTA0VVQzAeFw0yMDA3MTQxOTU0MzhaFw0yMDEwMTIxOTU0MzhaME8xEDAOBgNVBAYTB0JlbGdpdW0xETAPBgNVBAcTCEJydXNzZWxzMQwwCgYDVQQKEwNFVUMxDDAKBgNVBAsTA0VVQzEMMAoGA1UEAxMDRVVDMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAj+WR8W07/yfXeXvdz54Byoc2ujlN01E1Z+jg9IIqa3mLs22IomCgcvbgTlR3ojoLur18gi5OIWqBm8bSYQenWIcQFmtYn3kezC3uYa8oSFSRP8wfBprJ7/u6PlXlnNYPj6F8XXeRavN4CTyr8yWbVmdkETIS0DjaE0+OraSKGlJCj7eFSN0lKagBXDID42gshBLpDrRjcrv6Olh6A6911iN7zIM38F6ST+VGyVNizSC51EkZobbqdTjp1qleNi8IrGQeGIiOKjkYvQtnUTrzlHMzfi1zn+HbJ9By4OKVBNhSYJS6mm+vI4Fw6Vfz+PnmtEltWD0p2Vel1HufZWVhHQIDAQABoyEwHzAdBgNVHQ4EFgQUJaCen7kgMaSsZvSpdTEesmrMl2EwDQYJKoZIhvcNAQELBQADggEBAFSftovxNrYSvhINbEkpNBd3QqVQFzKtH8lMlBGnWMlzbDnj1KdP+DYmTHhvneT5c8b5uuhAkMy5xXZNkB9hY1cHG3SAesxyAafj1Wi7bm6F1/VQ3zG3rKo1/cI2Nc2v4Tyf/6B8xa/wA8rfT7XCOeA2eNR/XM/VYU/Fc3Jb1QRXBEAvi3VP2+37NV8I+BWncG6OZp85wXghnzQit5nxAHV9PwanO6v34X1K6Sp2Klce6pUvftr9JvskrMyM3z32vQsu/ZOr7xx0iCd4hqgvAADIY9XR8YHlOTWI0iV/r1WX3xMNjbF/NJxK4NZYFVKyy2Vhh0vT49wTTtufHLfWtA0=");
//    int i = token.lastIndexOf('.');
//    String withoutSignature = token.substring(0, i + 1);
//    Jwt<Header, Claims> untrusted = Jwts.parser().parseClaimsJwt(withoutSignature);
//    //Let's set the JWT Claims
//    if (untrusted.getBody().getIssuer().equals("SEP")) {
//      Jwt<Header, Claims> parsedToken = Jwts.parser().setSigningKey(publicKey).parse(token);
//
//      System.out.println(parsedToken.getBody().toString());
//    }
//
//  }

  private PublicKey createPublicKey(String publicKeyValue)
      throws NoSuchAlgorithmException, InvalidKeySpecException, CertificateException {
    String cert = "-----BEGIN CERTIFICATE-----\n"
        + "MIIDPTCCAiWgAwIBAgIELKfsgzANBgkqhkiG9w0BAQsFADBPMRAwDgYDVQQGEwdCZWxnaXVtMREwDwYDVQQHEwhCcnVzc2VsczEMMAoGA1UEChMDRVVDMQwwCgYDVQQLEwNFVUMxDDAKBgNVBAMTA0VVQzAeFw0yMDA3MTQxOTU0MzhaFw0yMDEwMTIxOTU0MzhaME8xEDAOBgNVBAYTB0JlbGdpdW0xETAPBgNVBAcTCEJydXNzZWxzMQwwCgYDVQQKEwNFVUMxDDAKBgNVBAsTA0VVQzEMMAoGA1UEAxMDRVVDMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAj+WR8W07/yfXeXvdz54Byoc2ujlN01E1Z+jg9IIqa3mLs22IomCgcvbgTlR3ojoLur18gi5OIWqBm8bSYQenWIcQFmtYn3kezC3uYa8oSFSRP8wfBprJ7/u6PlXlnNYPj6F8XXeRavN4CTyr8yWbVmdkETIS0DjaE0+OraSKGlJCj7eFSN0lKagBXDID42gshBLpDrRjcrv6Olh6A6911iN7zIM38F6ST+VGyVNizSC51EkZobbqdTjp1qleNi8IrGQeGIiOKjkYvQtnUTrzlHMzfi1zn+HbJ9By4OKVBNhSYJS6mm+vI4Fw6Vfz+PnmtEltWD0p2Vel1HufZWVhHQIDAQABoyEwHzAdBgNVHQ4EFgQUJaCen7kgMaSsZvSpdTEesmrMl2EwDQYJKoZIhvcNAQELBQADggEBAFSftovxNrYSvhINbEkpNBd3QqVQFzKtH8lMlBGnWMlzbDnj1KdP+DYmTHhvneT5c8b5uuhAkMy5xXZNkB9hY1cHG3SAesxyAafj1Wi7bm6F1/VQ3zG3rKo1/cI2Nc2v4Tyf/6B8xa/wA8rfT7XCOeA2eNR/XM/VYU/Fc3Jb1QRXBEAvi3VP2+37NV8I+BWncG6OZp85wXghnzQit5nxAHV9PwanO6v34X1K6Sp2Klce6pUvftr9JvskrMyM3z32vQsu/ZOr7xx0iCd4hqgvAADIY9XR8YHlOTWI0iV/r1WX3xMNjbF/NJxK4NZYFVKyy2Vhh0vT49wTTtufHLfWtA0="
        + "-----END CERTIFICATE-----";
    byte[] certBytes = cert.getBytes(java.nio.charset.StandardCharsets.UTF_8);

    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    InputStream in = new ByteArrayInputStream(certBytes);
    X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(in);

    System.out.println("Subject DN : " + certificate.getSubjectDN().getName());
    System.out.println("Issuer : " + certificate.getIssuerDN().getName());
    System.out.println("Not After: " + certificate.getNotAfter());
    System.out.println("Not Before: " + certificate.getNotBefore());
    System.out.println("version: " + certificate.getVersion());
    System.out.println("serial number : " + certificate.getSerialNumber());

    return certificate.getPublicKey();


  }
}