package org.eea.validation.multitenancy;

import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.lang.reflect.Method;
import org.eea.validation.service.ValidationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class ProxyValidationServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProxyValidationServiceImplTest {


  /** The validation service. */
  @Mock
  private ValidationService validationService;

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
    Mockito.doNothing().when(validationService).validateDataSetData(Mockito.any());
    Method method = ValidationService.class.getMethod("validateDataSetData", Long.class);
    ProxyValidationServiceImpl proxy = new ProxyValidationServiceImpl(validationService);
    Object result = proxy.invoke(validationService, method, new Object[] {1L});
    assertNull("failed", result);
  }

  /**
   * Test invoke exception.
   *
   * @throws Throwable the throwable
   */
  @Test(expected = Throwable.class)
  public void testInvokeException() throws Throwable {
    Method method = ValidationService.class.getMethod("validateDataSetData", Long.class);
    Mockito.when(method.invoke(validationService, new Object[] {1L})).thenThrow(Throwable.class);
    ProxyValidationServiceImpl proxy = new ProxyValidationServiceImpl(validationService);
    proxy.invoke(validationService, method, new Object[] {1L});
  }

}
