package org.eea.dataset.multitenancy;

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

@RunWith(MockitoJUnitRunner.class)
public class MultiTenantDataSourceTest {

  @InjectMocks
  MultiTenantDataSource multiTenantDataSource;

  @Mock
  Map<Object, Object> dataSources;

  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testDetermineCurrentLookupKey() {
    multiTenantDataSource.determineCurrentLookupKey();
  }

  @Test
  public void testAddDataSource() {
    multiTenantDataSource.addDataSource(new ConnectionDataVO());
  }

}
