package org.eea.multitenancy;

/**
 * The type Tenant resolver.
 */
public class TenantResolver {


  /**
   * The tenant.
   */
  private static ThreadLocal<String> tenant = new ThreadLocal<>();

  /**
   * The Constant DEFAULT_TENANT.
   */
  private static final String DEFAULT_TENANT = "dataset_1";

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
    return null == tenant.get() || tenant.get().isEmpty() ? DEFAULT_TENANT : tenant.get();
  }

  /**
   * Cleans the tenant name stored in ThreadLocal.
   */
  public static void clean() {
    tenant.remove();
  }
}
