package org.eea.validation.multitenancy;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.apache.commons.lang3.StringUtils;
import org.eea.validation.service.ValidationService;


/**
 * The type Proxy validation service.
 */
public class ProxyValidationServiceImpl implements InvocationHandler {

  /** The validation service. */
  private ValidationService validationService;

  /**
   * Instantiates a new Proxy validation service.
   *
   * @param ValidationService the validation service
   */
  public ProxyValidationServiceImpl(ValidationService validationService) {
    this.validationService = validationService;
  }

  /**
   * Invoke.
   *
   * @param proxy the proxy
   * @param method the method
   * @param args the args
   * @return the object
   * @throws Throwable the throwable
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Annotation[][] annotations = method.getParameterAnnotations();
    String datasetId = "";
    Boolean continueLoop = true;
    for (int i = 0; i < annotations.length && continueLoop; i++) {
      // annotated parameter, search @datasetId annotated parameter if any
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
    Object result = null;
    try {
      result = method.invoke(validationService, args);
    } finally {
      TenantResolver.clean();
    }

    return result;
  }
}
