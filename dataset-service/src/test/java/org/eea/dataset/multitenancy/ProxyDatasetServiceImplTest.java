package org.eea.dataset.multitenancy;

import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import org.eea.dataset.service.DatasetService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

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
    Mockito.doNothing().when(datasetService).processFile(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
    Method method = DatasetService.class.getMethod("processFile", Long.class, String.class,
        InputStream.class, String.class);
    ProxyDatasetServiceImpl proxy = new ProxyDatasetServiceImpl(datasetService);
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    Object result =
        proxy.invoke(datasetService, method, new Object[] {1L, "id", file.getInputStream(), null});
    assertNull("failed", result);
  }

  /**
   * Test invoke exception.
   *
   * @throws Throwable the throwable
   */
  @Test(expected = Throwable.class)
  public void testInvokeException() throws Throwable {
    Method method =
        DatasetService.class.getMethod("getTableValuesById", String.class, Pageable.class);
    Mockito.when(method.invoke(datasetService, new Object[] {"id", Pageable.unpaged()}))
        .thenThrow(Throwable.class);
    ProxyDatasetServiceImpl proxy = new ProxyDatasetServiceImpl(datasetService);
    proxy.invoke(datasetService, method, new Object[] {"id", Pageable.unpaged()});
  }

}
