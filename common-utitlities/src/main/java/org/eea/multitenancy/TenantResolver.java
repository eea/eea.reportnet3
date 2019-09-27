package org.eea.multitenancy;

import org.eea.exception.EEARuntimeException;

/**
 * The type Tenant resolver.
 */
public final class TenantResolver {


  /**
   * The tenant.
   */
  private static InheritableThreadLocal<String> tenant = new InheritableThreadLocal<>();


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

    return null == tenant.get() || tenant.get().isEmpty() ? "" : tenant.get();

  }

  /**
   * Cleans the tenant name stored in ThreadLocal.
   */
  public static void clean() {
    tenant.remove();
  }
}
