package org.eea.dataset.multitenancy;

public class TenantResolver {

  private static ThreadLocal<String> tenant = new ThreadLocal<>();

  private static final String DEFAULT_TENANT = "dataset_1";

  public static void setTenantName(String tenantName) {
    tenant.set(tenantName);
  }

  public static String getTenantName() {
    return null == tenant.get() || tenant.get().isEmpty() ? DEFAULT_TENANT : tenant.get();
  }

  public static void clean() {
    tenant.remove();
  }
}
