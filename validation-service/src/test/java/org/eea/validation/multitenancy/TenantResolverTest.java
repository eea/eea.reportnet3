package org.eea.validation.multitenancy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class TenantResolverTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class TenantResolverTest {

  /** The tenant resolver. */
  @InjectMocks
  private TenantResolver tenantResolver;

  /** The Constant NAME. */
  private static final String NAME = "dataset";

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
   * Test set tenant name.
   */
  @Test
  public void testSetTenantName() {
    tenantResolver.setTenantName(NAME);
  }

  /**
   * Test get tenant name.
   */
  @Test
  public void testGetTenantName() {
    tenantResolver.setTenantName(NAME);
    assertEquals("not the same name", NAME, tenantResolver.getTenantName());
  }

  /**
   * Test get tenant name null.
   */
  @Test
  public void testGetTenantNameNull() {
    tenantResolver.setTenantName(null);
    assertNotNull("name is null", tenantResolver.getTenantName());
  }

  /**
   * Test clean.
   */
  @Test
  public void testClean() {
    tenantResolver.clean();
  }

}
