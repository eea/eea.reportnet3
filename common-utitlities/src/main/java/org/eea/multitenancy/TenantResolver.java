package org.eea.multitenancy;

import java.util.Map;
import org.eea.exception.EEARuntimeException;
import org.eea.thread.ThreadPropertiesManager;

/**
 * The type Tenant resolver.
 */
public final class TenantResolver extends ThreadPropertiesManager {



  /**
   * The Constant DEFAULT_TENANT. Bust be default
   */
  public static String initTenant = "dataset_1";


  /**
   * Sets tenant name.
   *
   * @param tenantName the tenant name
   */
  public static void setTenantName(String tenantName) {
    setVariable("datasetName", tenantName);
  }

  /**
   * Gets tenant name.
   *
   * @return the tenant name
   */
  public static String getTenantName() {
    if ("".equals(initTenant)) {// application is started already
      if (null == thread.get() || thread.get().isEmpty()) {
        throw new EEARuntimeException("Error, connection id is null or empty");
      }
    }

    return null == thread.get() || thread.get().isEmpty() ? initTenant
        : thread.get().get("datasetName").toString();


  }

  /**
   * Cleans the tenant name stored in ThreadLocal.
   */
  public static void clean() {
    Map<String, Object> properties = thread.get();
    if (null != properties) {
      properties.remove("datasetName");
    }
  }
}
