package org.eea.dataset.multitenancy;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import org.eea.dataset.service.DatasetService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class TransactionalProxyConfigurationTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionalProxyConfigurationTest {

  /**
   * The transactional proxy configuration.
   */
  @InjectMocks
  private TransactionalProxyConfiguration transactionalProxyConfiguration;

  @Mock
  private DatasetService datasetService;

  /**
   * Inits the mocks.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test proxy dataset service.
   */
  @Test
  public void testProxyDatasetService() {
    DatasetService result = transactionalProxyConfiguration.proxyDatasetService(datasetService);
    assertNotNull("null?", result);
  }

}
