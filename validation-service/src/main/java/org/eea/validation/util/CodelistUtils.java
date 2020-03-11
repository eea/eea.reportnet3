package org.eea.validation.util;

import io.netty.util.internal.StringUtil;

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
    Boolean validationResult = false;
    // we delete the first character and the last one because we receive a string with [] values
    if (!StringUtil.isNullOrEmpty(value)) {
      final String[] arrayItems = codelistItems.substring(1, codelistItems.length() - 1).split(",");

      if (Boolean.TRUE.equals(sensitive)) {
        for (int i = 0; i < arrayItems.length; i++) {
          if (arrayItems[i].trim().equals(value)) {
            validationResult = Boolean.TRUE;
          }
        }
      } else {
        for (int i = 0; i < arrayItems.length; i++) {
          if (arrayItems[i].trim().equalsIgnoreCase(value)) {
            validationResult = Boolean.TRUE;
          }
        }
      }
    } else {
      validationResult = Boolean.TRUE;
    }
    return validationResult;
  }

}

