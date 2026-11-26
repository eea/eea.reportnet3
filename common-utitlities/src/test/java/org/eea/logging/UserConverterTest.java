package org.eea.logging;

import java.util.ArrayList;
import java.util.HashSet;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.thread.ThreadPropertiesManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Tests for UserConverter (%user in logback.xml).
 */
public class UserConverterTest {

  private final UserConverter converter = new UserConverter();

  @After
  public void tearDown() {
    SecurityContextHolder.clearContext();
    ThreadPropertiesManager.clearUser();
  }

  /** Normal request: login with EeaUserDetails (JWT, API key, Kafka event with token). */
  @Test
  public void loggedInUserIsPrinted() {
    login(EeaUserDetails.create("john", new HashSet<>()));
    Assert.assertEquals("john", converter.convert(null));
  }

  /** Orchestrator admin login: the principal is a plain String. */
  @Test
  public void stringPrincipalIsPrinted() {
    login("reportnet_admin");
    Assert.assertEquals("reportnet_admin", converter.convert(null));
  }

  /** No login, but Reportnet set the thread user (Kafka, new Thread). */
  @Test
  public void threadUserIsPrintedWhenNoLogin() {
    ThreadPropertiesManager.setVariable("user", "kafkaUser");
    Assert.assertEquals("kafkaUser", converter.convert(null));
  }

  /** The login wins over the thread user. */
  @Test
  public void loginWinsOverThreadUser() {
    ThreadPropertiesManager.setVariable("user", "oldUser");
    login(EeaUserDetails.create("john", new HashSet<>()));
    Assert.assertEquals("john", converter.convert(null));
  }

  /** Anonymous requests (e.g. /actuator/health) are not a user. */
  @Test
  public void anonymousIsNotAUser() {
    SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("key",
        "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    Assert.assertEquals("-", converter.convert(null));
  }

  /** A login that is not authenticated is ignored. */
  @Test
  public void notAuthenticatedIsNotAUser() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("john", "token"));
    Assert.assertEquals("-", converter.convert(null));
  }

  /** "null" is Reportnet's default when an event has no user. */
  @Test
  public void nullTextIsNotAUser() {
    ThreadPropertiesManager.setVariable("user", "null");
    Assert.assertEquals("-", converter.convert(null));
  }

  /** Nothing set at all (startup, scheduled jobs). */
  @Test
  public void nothingSetPrintsDash() {
    Assert.assertEquals("-", converter.convert(null));
  }

  private void login(Object principal) {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(principal, "token", new ArrayList<>()));
  }
}
