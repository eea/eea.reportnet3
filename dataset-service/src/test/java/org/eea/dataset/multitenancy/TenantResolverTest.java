package org.eea.dataset.multitenancy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TenantResolverTest {

  @InjectMocks
  TenantResolver tenantResolver;

  private static final String NAME = "dataset";

  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testSetTenantName() {
    tenantResolver.setTenantName(NAME);
  }

  @Test
  public void testGetTenantName() {
    tenantResolver.setTenantName(NAME);
    assertEquals(NAME, tenantResolver.getTenantName());
  }

  @Test
  public void testGetTenantNameNull() {
    tenantResolver.setTenantName(null);
    assertNotNull(tenantResolver.getTenantName());
  }

  @Test
  public void testClean() {
    tenantResolver.clean();
  }

}
