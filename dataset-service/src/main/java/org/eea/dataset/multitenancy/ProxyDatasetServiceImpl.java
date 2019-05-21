package org.eea.dataset.multitenancy;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.eea.dataset.service.DatasetService;


/**
 * The type Proxy dataset service.
 */
public class ProxyDatasetServiceImpl implements InvocationHandler {

  private DatasetService datasetService;

  /**
   * Instantiates a new Proxy dataset service.
   *
   * @param datasetService the dataset service
   */
  public ProxyDatasetServiceImpl(DatasetService datasetService) {
    this.datasetService = datasetService;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Annotation[][] annotations = method.getParameterAnnotations();
    String datasetId = "";

    outerloop: for (int i = 0; i < annotations.length; i++) {
      if (annotations[i].length > 0) {// annotated parameter, search @DatasetId annotated parameter
                                      // if any
        for (Annotation annotation : annotations[i]) {
          if (annotation.annotationType().equals(DatasetId.class)) {
            datasetId = args[i].toString();
            break outerloop;
          }
        }
      }
    }
    if (!"".equals(datasetId)) {
      TenantResolver.setTenantName("dataset_" + datasetId);
    }
    Object result = null;
    try {
      result = method.invoke(datasetService, args);
    } finally {
      TenantResolver.clean();
    }

    return result;
  }
}
