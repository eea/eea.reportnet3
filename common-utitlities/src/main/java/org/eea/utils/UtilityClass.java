package org.eea.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class UtilityClass {

  private UtilityClass() {
    throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * Adding quotes to fields for dremio usage to avoid reserved keywords exception on the query
   *
   * @param fieldNames Field name or names seperated by comma
   * @return The field or fields quoted, seperated by comma
   */
  public static String addQuotesToFieldNames(String fieldNames) {
    if (fieldNames != null && !fieldNames.isBlank()) {
      fieldNames = Arrays.stream(fieldNames.split(",")) // Split by commas
          .map(String::trim)                        // Remove spaces around field names
          .filter(field -> !field.isBlank())        // Filter out empty or invalid field names
          .filter(field -> !field.contains("\"")) // Skip fields that already contain double quotes
          .map(field -> "\"" + field + "\"")  // Wrap each field in double quotes
          .collect(Collectors.joining(","));      // Join them back with commas
    }
    return fieldNames;
  }

}
