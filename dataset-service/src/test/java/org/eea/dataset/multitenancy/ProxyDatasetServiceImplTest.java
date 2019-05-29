package org.eea.dataset.multitenancy;

import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.lang.reflect.Method;
import org.eea.dataset.service.DatasetService;
import org.eea.interfaces.vo.dataset.TableVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class ProxyDatasetServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProxyDatasetServiceImplTest {

  /** The dataset service. */
  @Mock
  private DatasetService datasetService;

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
   * Test invoke.
   *
   * @throws Throwable the throwable
   */
  @Test
  public void testInvoke() throws Throwable {
    Mockito.when(datasetService.getTableValuesById(Mockito.any(), Mockito.any()))
        .thenReturn(new TableVO());
    Method method = DatasetService.class.getMethod("getTableValuesById", Long.class);
    ProxyDatasetServiceImpl proxy = new ProxyDatasetServiceImpl(datasetService);
    Object result = proxy.invoke(datasetService, method, new Long[] {1l});
    assertNotNull(result);
  }

  /**
   * Test invoke exception.
   *
   * @throws Throwable the throwable
   */
  @Test(expected = Throwable.class)
  public void testInvokeException() throws Throwable {
    Method method = DatasetService.class.getMethod("getDatasetById", Long.class);
    Mockito.when(method.invoke(datasetService, Mockito.any())).thenThrow(Throwable.class);
    ProxyDatasetServiceImpl proxy = new ProxyDatasetServiceImpl(datasetService);
    proxy.invoke(datasetService, method, new Long[] {1l});
  }

}
