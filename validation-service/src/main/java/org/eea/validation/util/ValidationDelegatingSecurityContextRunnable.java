package org.eea.validation.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

public class ValidationDelegatingSecurityContextRunnable implements Runnable {

  private final Runnable delegate;

  /**
   * The {@link SecurityContext} that the delegate {@link Runnable} will be ran as.
   */
  private final SecurityContext delegateSecurityContext;

  private static final Logger LOG = LoggerFactory
      .getLogger(ValidationDelegatingSecurityContextRunnable.class);

  /**
   * Creates a new {@link ValidationDelegatingSecurityContextRunnable} with a specific {@link
   * SecurityContext}.
   *
   * @param delegate the delegate {@link Runnable} to run with the specified {@link
   *     SecurityContext}. Cannot be null.
   * @param securityContext the {@link SecurityContext} to establish for the delegate {@link
   *     Runnable}. Cannot be null.
   */
  public ValidationDelegatingSecurityContextRunnable(Runnable delegate,
      SecurityContext securityContext) {
    Assert.notNull(delegate, "delegate cannot be null");
    Assert.notNull(securityContext, "securityContext cannot be null");
    this.delegate = delegate;
    this.delegateSecurityContext = securityContext;
    LOG.info(
        " creating new ValidationDelegatingSecurityContextRunnable with security context {} {}",
        securityContext.getAuthentication().getPrincipal(),
        securityContext.getAuthentication().getCredentials());
  }

  /**
   * Creates a new {@link DelegatingSecurityContextRunnable} with the {@link SecurityContext} from
   * the {@link SecurityContextHolder}.
   *
   * @param delegate the delegate {@link Runnable} to run under the current {@link
   *     SecurityContext}. Cannot be null.
   */
  public ValidationDelegatingSecurityContextRunnable(Runnable delegate) {
    this(delegate, SecurityContextHolder.getContext());
    LOG.info(
        " creating new ValidationDelegatingSecurityContextRunnable with BRAND NEW security context {} {}",
        SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
        SecurityContextHolder.getContext().getAuthentication().getCredentials());
  }

  @Override
  public void run() {

    try {

      delegate.run();
    } finally {

      SecurityContextHolder.clearContext();

    }
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  /**
   * Factory method for creating a {@link DelegatingSecurityContextRunnable}.
   *
   * @param delegate the original {@link Runnable} that will be delegated to after establishing
   *     a {@link SecurityContext} on the {@link SecurityContextHolder}. Cannot have null.
   * @param securityContext the {@link SecurityContext} to establish before invoking the
   *     delegate {@link Runnable}. If null, the current {@link SecurityContext} from the {@link
   *     SecurityContextHolder} will be used.
   */
  public static Runnable create(Runnable delegate, SecurityContext securityContext) {
    Assert.notNull(delegate, "delegate cannot be  null");
    return securityContext == null ? new DelegatingSecurityContextRunnable(delegate)
        : new DelegatingSecurityContextRunnable(delegate, securityContext);
  }
}