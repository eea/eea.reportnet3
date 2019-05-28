package org.eea.dataset.multitenancy;

import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.util.Map;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class MultiTenantDataSourceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiTenantDataSourceTest {

  /** The multi tenant data source. */
  @InjectMocks
  private MultiTenantDataSource multiTenantDataSource;

  /** The data sources. */
  @Mock
  private Map<Object, Object> dataSources;

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
   * Test determine current lookup key.
   */
  @Test
  public void testDetermineCurrentLookupKey() {
    Object result = multiTenantDataSource.determineCurrentLookupKey();
    assertNotNull(result);
  }

  /**
   * Test add data source.
   */
  @Test
  public void testAddDataSource() {
    multiTenantDataSource.addDataSource(new ConnectionDataVO());
  }

}
