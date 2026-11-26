package org.eea.logging;

import org.eea.thread.ThreadPropertiesManager;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Logback converter that prints the current user (%user) in log lines.
 * Order: Spring Security context (REST / Feign) -> ThreadPropertiesManager (Kafka / async) -> "-".
 */
public class UserConverter extends ClassicConverter {

  private static final String NO_USER = "-";

  @Override
  public String convert(ILoggingEvent event) {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.isAuthenticated()
          && !(auth instanceof AnonymousAuthenticationToken)) {
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
          return ((UserDetails) principal).getUsername();
        }
        if (principal != null) {
          return principal.toString();
        }
      }
      Object user = ThreadPropertiesManager.getVariable("user");
      if (user != null && !"null".equals(user)) {
        return user.toString();
      }
    } catch (Exception e) {
      // never break logging because of the user lookup
    }
    return NO_USER;
  }
}
