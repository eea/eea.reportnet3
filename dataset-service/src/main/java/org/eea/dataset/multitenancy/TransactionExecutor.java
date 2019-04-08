package org.eea.dataset.multitenancy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.eea.dataset.controller.DataSetControllerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The type Transaction executor.
 */
@Component
public class TransactionExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(DataSetControllerImpl.class);
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /**
   * Ensure that the method passed as parameter is executed pointing to the right connection
   *
   * @param <T> the type parameter
   * @param executable the executable
   * @param datasetId the dataset id
   * @param methodToExecute the method to execute
   * @param params the params
   *
   * @return the t
   */
  public <T> T executeInTransaction(Object executable, String datasetId,
      String methodToExecute,
      Object... params) {

    Method[] declaredMethods = executable.getClass().getDeclaredMethods();
    Method executingMethod = null;
    for (Method method : declaredMethods) {
      if (method.getName().equals(methodToExecute)) {
        //better use this approach than instantiating the method itself
        //There are problem instantiating if the methods receives a List as input parameter but it's passed and ArrayList
        executingMethod = method;
        break;
      }
    }
    TenantResolver.setTenantName(
        datasetId);//necessary if the method is annotated with @Transactional due to the precedence of Aspects controlling it
    Object result = null;
    try {
      result = executingMethod.invoke(executable, params);
    } catch (IllegalAccessException | InvocationTargetException e) {
      LOG_ERROR.error("Error invocating in transaction to method {}.{}",
          executable.getClass().getCanonicalName(), methodToExecute);
      throw new TransactionExecutionException(String
          .format("Error invocating in transaction to method %s.%s",
              executable.getClass().getCanonicalName(), methodToExecute), e);
    } finally {
      TenantResolver.clean();
    }

    return (T) result;
  }
}
