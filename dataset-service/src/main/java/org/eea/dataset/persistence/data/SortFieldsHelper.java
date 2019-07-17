package org.eea.dataset.persistence.data;

/**
 * The type Sort fields helper.
 */
public class SortFieldsHelper {

  private static ThreadLocal<String> helper = new ThreadLocal<>();

  /**
   * Sets sorting field.
   *
   * @param field the field
   */
  public static void setSortingField(String field) {
    helper.set(field);
  }

  /**
   * Gets sorting field.
   *
   * @return the sorting field
   */
  public static String getSortingField() {
    return helper.get();
  }

  /**
   * Clean sorting field.
   */
  public static void cleanSortingField() {
    helper.remove();
  }
}
