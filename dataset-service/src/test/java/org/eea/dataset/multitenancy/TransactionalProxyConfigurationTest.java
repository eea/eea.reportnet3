package org.eea.dataset.multitenancy;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class TransactionalProxyConfigurationTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionalProxyConfigurationTest {

  /** The transactional proxy configuration. */
  @InjectMocks
  private TransactionalProxyConfiguration transactionalProxyConfiguration;

  /**
   * Inits the mocks.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test proxy dataset service.
   */
  @Test
  public void testProxyDatasetService() {
    transactionalProxyConfiguration.proxyDatasetService();
  }

}
