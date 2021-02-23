package org.eea.thread;

import java.util.concurrent.ExecutorService;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

/**
 * The Class EEADelegatingSecurityContextExecutorService.
 */
public class EEADelegatingSecurityContextExecutorService
    extends DelegatingSecurityContextExecutorService {

  /**
   * Instantiates a new EEA delegating security context executor service.
   *
   * @param delegate the delegate
   */
  public EEADelegatingSecurityContextExecutorService(ExecutorService delegate) {
    super(delegate);
  }

  /**
   * Gets the delegate executor service.
   *
   * @return the delegate executor service
   */
  public ExecutorService getDelegateExecutorService() {
    return (ExecutorService) getDelegateExecutor();
  }
}
