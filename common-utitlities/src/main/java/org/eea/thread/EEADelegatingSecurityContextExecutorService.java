package org.eea.thread;

import java.util.concurrent.ExecutorService;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

public class EEADelegatingSecurityContextExecutorService
    extends DelegatingSecurityContextExecutorService {

  public EEADelegatingSecurityContextExecutorService(ExecutorService delegate) {
    super(delegate);
  }

  public ExecutorService getDelegateExecutorService() {
    return (ExecutorService) getDelegateExecutor();
  }

}
