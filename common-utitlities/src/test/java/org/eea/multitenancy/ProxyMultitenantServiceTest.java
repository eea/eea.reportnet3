package org.eea.multitenancy;

import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.lang.reflect.Method;
import org.eea.exception.EEAException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class ProxyMultitenantServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProxyMultitenantServiceTest {

  /**
   * The dataset service.
   */
  @Mock
  private ProxyTestInterface proxyTestInterface;

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
   * Test invoke.
   *
   * @throws Throwable the throwable
   */
  @Test
  public void testInvoke() throws Throwable {
    Mockito.doNothing().when(proxyTestInterface).testMethod(Mockito.any());
    Method method = ProxyTestInterface.class.getMethod("testMethod", String.class);
    ProxyMultitenantService<ProxyTestInterface> proxy =
        new ProxyMultitenantService<>(proxyTestInterface);

    Object result = proxy.invoke(proxyTestInterface, method, new Object[]{"id"});
    assertNull("result not null", result);
  }

  /**
   * Test invoke exception.
   *
   * @throws Throwable the throwable
   */
  @Test(expected = Throwable.class)
  public void testInvokeException() throws Throwable {
    Mockito.doThrow(EEAException.class).when(proxyTestInterface).testMethod(Mockito.any());
    Method method = ProxyTestInterface.class.getMethod("testMethod", String.class);
    ProxyMultitenantService<ProxyTestInterface> proxy =
        new ProxyMultitenantService<>(proxyTestInterface);

    Object result = proxy.invoke(proxyTestInterface, method, new Object[]{"id"});
  }

}
