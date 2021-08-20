package org.eea.validation.multitenancy;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import org.eea.validation.service.ValidationService;
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
  private ValidationService validationService;

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
   * Test proxy validation service.
   */
  @Test
  public void testProxyDatasetService() {
    ValidationService result = transactionalProxyConfiguration
        .proxyValidationService(validationService);
    assertNotNull("is null", result);
  }

}
