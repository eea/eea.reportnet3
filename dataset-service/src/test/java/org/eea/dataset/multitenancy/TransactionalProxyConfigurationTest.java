package org.eea.dataset.multitenancy;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalProxyConfigurationTest {

  @InjectMocks
  TransactionalProxyConfiguration transactionalProxyConfiguration;

  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testProxyDatasetService() {
    transactionalProxyConfiguration.proxyDatasetService();
  }

}
