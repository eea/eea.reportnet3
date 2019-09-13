package org.eea.multitenancy;

import org.eea.exception.EEARuntimeException;

/**
 * The type Tenant resolver.
 */
public final class TenantResolver {


  /**
   * The tenant.
   */
  private static ThreadLocal<String> tenant = new ThreadLocal<>();

  /**
   * The Constant DEFAULT_TENANT. Bust be default
   */
  public static String initTenant = "dataset_1";

  /**
   * Instantiates a new tenant resolver.
   */
  private TenantResolver() {
    super();
  }

  /**
   * Sets tenant name.
   *
   * @param tenantName the tenant name
   */
  public static void setTenantName(String tenantName) {
    tenant.set(tenantName);
  }

  /**
   * Gets tenant name.
   *
   * @return the tenant name
   */
  public static String getTenantName() {
    if ("".equals(initTenant)) {//application is started already
      if (null == tenant.get() || tenant.get().isEmpty()) {
        throw new EEARuntimeException("Error, connection id is null or empty");
      }
    }

    return null == tenant.get() || tenant.get().isEmpty() ? initTenant : tenant.get();

  }

  /**
   * Cleans the tenant name stored in ThreadLocal.
   */
  public static void clean() {
    tenant.remove();
  }
}
