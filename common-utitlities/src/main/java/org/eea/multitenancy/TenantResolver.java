package org.eea.multitenancy;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;

/**
 * The type Tenant resolver.
 */
public final class TenantResolver extends ThreadPropertiesManager {


  /**
   * Sets tenant name.
   *
   * @param tenantName the tenant name
   */
  public static void setTenantName(String tenantName) {
    setVariable(LiteralConstants.DATASET_NAME, tenantName);
  }

  /**
   * Gets tenant name.
   *
   * @return the tenant name
   */
  public static String getTenantName() {
    Map<String, Object> properties = thread.get();
    String datasetName = "";
    if (null != properties && !properties.isEmpty()) {
      Object value = properties.get(LiteralConstants.DATASET_NAME);
      if (null != value && StringUtils.isNotEmpty(value.toString())) {
        datasetName = value.toString();
      }
    }

    return datasetName;

  }

  /**
   * Cleans the tenant name stored in ThreadLocal.
   */
  public static void clean() {
    Map<String, Object> properties = thread.get();
    if (null != properties) {
      properties.remove(LiteralConstants.DATASET_NAME);
    }
  }
}
