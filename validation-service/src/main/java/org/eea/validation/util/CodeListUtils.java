package org.eea.validation.util;

/**
 * The Class CodeListUtils.
 */
public class CodeListUtils {

  /**
   * Code list validate. That clase check if the value are avaliable inside the array of values,
   * with case sensitive and insensitive
   *
   * @param value the value
   * @param codeListItems the code list items
   * @param sensitive the sensitive
   * @return the boolean
   */
  public static Boolean codeListValidate(final String value, String codeListItems,
      final boolean sensitive) {
    Boolean codeList = false;
    // we can validation helper and put in memory the codeList
    codeListItems = codeListItems.replace("[", "");
    codeListItems = codeListItems.replace("]", "");
    String[] arrayItems = codeListItems.split(",");

    if (Boolean.TRUE.equals(sensitive)) {
      for (int i = 0; i < arrayItems.length; i++) {
        if (arrayItems[i].trim().equals(value)) {
          codeList = Boolean.TRUE;
        }
      }
    } else {
      for (int i = 0; i < arrayItems.length; i++) {
        if (arrayItems[i].trim().equalsIgnoreCase(value)) {
          codeList = Boolean.TRUE;
        }
      }
    }

    return codeList;
  }

}

