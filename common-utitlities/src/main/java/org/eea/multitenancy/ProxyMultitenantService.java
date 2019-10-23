package org.eea.multitenancy;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.apache.commons.lang3.StringUtils;


/**
 * The type Proxy dataset service.
 *
 * @param <T> the type parameter
 */
public class ProxyMultitenantService<T> implements InvocationHandler {

  /**
   * The dataset service.
   */
  private T proxiedService;

  /**
   * Instantiates a new Proxy dataset service.
   *
   * @param proxiedService the proxied service
   */
  public ProxyMultitenantService(T proxiedService) {
    this.proxiedService = proxiedService;
  }

  /**
   * Invoke.
   *
   * @param proxy the proxy
   * @param method the method
   * @param args the args
   *
   * @return the object
   *
   * @throws Throwable the throwable
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Annotation[][] annotations = method.getParameterAnnotations();
    String datasetId = "";
    Boolean continueLoop = true;
    for (int i = 0; i < annotations.length && continueLoop; i++) {
      // annotated parameter, search @DatasetId annotated parameter if any
      if (annotations[i].length > 0) {
        for (Annotation annotation : annotations[i]) {
          if (annotation.annotationType().equals(DatasetId.class)) {
            datasetId = args[i].toString();
            continueLoop = false;
          }
        }
      }
    }
    if (StringUtils.isNotBlank(datasetId)) {
      TenantResolver.setTenantName("dataset_" + datasetId);
    }
    
    return method.invoke(proxiedService, args);
  }
}
