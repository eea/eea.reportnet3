package org.eea.validation.util;

/**
 * The Class CodelistUtils.
 */
public class CodelistUtils {

  /**
   * This class checks whether the values are available inside the array of code list items in a
   * sensitive or insenstive way
   *
   * @param value the value
   * @param codelistItems the code list items
   * @param sensitive the sensitive
   * @return the boolean
   */
  public static Boolean codelistValidate(final String value, String codelistItems,
      final boolean sensitive) {
    Boolean codelist = false;
    // we can validation helper and put in memory the codelist
    // we delete the 1 character and the last because we recieve a string with '[' ']' values
    final String[] arrayItems = codelistItems.substring(1, codelistItems.length() - 1).split(",");

    if (Boolean.TRUE.equals(sensitive)) {
      for (int i = 0; i < arrayItems.length; i++) {
        if (arrayItems[i].trim().equals(value)) {
          codelist = Boolean.TRUE;
        }
      }
    } else {
      for (int i = 0; i < arrayItems.length; i++) {
        if (arrayItems[i].trim().equalsIgnoreCase(value)) {
          codelist = Boolean.TRUE;
        }
      }
    }

    return codelist;
  }

}

