package org.eea.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class UtilityClass {

  private static final Logger LOG = LoggerFactory.getLogger(UtilityClass.class);

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

  /**
   * Copy file from in to out
   *
   * @param jobId the job id
   * @param response The response
   * @param file The file
   * @param fileName The file name
   * @throws IOException The exception
   */
  public static void copyFromInToOutAndDelete(Long jobId, HttpServletResponse response, File file, String fileName) throws IOException {
    try (FileInputStream in = new FileInputStream(file); OutputStream out = response.getOutputStream()) {
      IOUtils.copyLarge(in, out);
      out.flush();
    } catch (IOException e) {
      LOG.error("Unexpected error! Error in copying large etl exported file {} for jobId {}. Message: {}", fileName, jobId, e.getMessage());
      throw e;
    }
    finally {
      try {
        Thread.sleep(100); // Small delay to ensure stream is flushed before delete
        deleteFile(file, fileName);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }
    }
  }

  public static boolean containsOnlyLatinCharacters(String filename) {
    if (filename == null) {
      return false;
    }

    for (char c : filename.toCharArray()) {
      Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
      if (block != Character.UnicodeBlock.BASIC_LATIN) {
        return false;
      }
    }

    return true;
  }

  public static String extractNonLatinCharacters(String input) {
    if (input == null) {
      return "";
    }

    return input.chars()
            .mapToObj(c -> (char) c)
            .filter(c -> Character.UnicodeBlock.of(c) != Character.UnicodeBlock.BASIC_LATIN)
            .map(String::valueOf)
            .collect(Collectors.joining(", "));
  }

  /**
   * Delete the file after it's being downloaded
   * @param file The file
   * @param fileName The file name
   */
  private static void deleteFile(File file, String fileName) {
    try {
      FileUtils.forceDelete(file);
    } catch (IOException deleteEx) {
      LOG.error("Failed to delete file {} after stream close. Message: {}", fileName, deleteEx.getMessage());
    }
  }
}
