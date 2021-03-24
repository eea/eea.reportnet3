package org.eea.validation.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Class ValidationDroolsUtils.
 */
public class ValidationDroolsUtils {

  /**
   * The Constant REG_EXP_URL.
   */
  private static final String REG_EXP_URL =
      "^(sftp:\\/\\/www\\.|sftp:\\/\\/|ftp:\\/\\/www\\.|ftp:\\/\\/|http:\\/\\/www\\.|https:\\/\\/www\\.|http:\\/\\/|https:\\/\\/)?[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,63}(:[0-9]{1,5})?(\\/.*)?$";

  /**
   * The Constant REG_EXP_EMAIL.
   */
  private static final String REG_EXP_EMAIL = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

  /**
   * The Constant REG_EXP_PHONE.
   */
  private static final String REG_EXP_PHONE = "^(\\(?\\+?[0-9]*\\)?)?[0-9_\\- \\(\\)]*$";

  /**
   * This class checks whether the values are available inside the array of code list items in a
   * sensitive or insenstive way.
   *
   * @param value the value
   * @param codelistItems the code list items
   * @param sensitive the sensitive
   *
   * @return the boolean
   */
  public static Boolean codelistValidate(final String value, String codelistItems,
      final boolean sensitive) {
    Boolean validationResult = false;
    // we delete the first character and the last one because we receive a string surrounded by []
    // symbols
    final String[] arrayItems = codelistItems.split(";");
    for (int i = 0; i < arrayItems.length; i++) {
      if (arrayItems[i].trim().equalsIgnoreCase(value)) {
        validationResult = Boolean.TRUE;
      }
    }
    return validationResult;
  }


  /**
   * Multi select codelist validate.
   *
   * @param value the value
   * @param codelistItems the codelist items
   *
   * @return the boolean
   */
  public static Boolean multiSelectCodelistValidate(final String value, String codelistItems) {
    Boolean validationResult = false;
    // this , checks if the user upload a file and in this field, the field have a , in the last
    // position(that is really dificult to happend but we put it just in case)
    if (',' != value.charAt(value.length() - 1)) {
      // we delete the first character and the last one because we receive a string with [] values
      final List<String> arrayValue = Arrays.asList(value.split(";"));
      // we trim the codelistValues to clean blank spaces
      arrayValue.replaceAll(String::trim);
      final List<String> arrayItems = Arrays.asList(codelistItems.split(";"));
      // we trim the values of database to clean blank spaces
      arrayItems.replaceAll(String::trim);
      if (!arrayValue.isEmpty() && arrayItems.containsAll(arrayValue)) {
        validationResult = true;
      }
    }
    return validationResult;
  }


  /**
   * Validate reg expression. This method put one regExpression to validate if the data in drools
   * have the correct format.
   *
   * @param value the value
   * @param regExp the reg exp
   *
   * @return the boolean
   */
  public static Boolean validateRegExpression(final String value, final String regExp) {
    // we put a value to validate depends of the expression we recieve in regExp
    String compiler = null;
    switch (regExp) {
      case "REG_EXP_EMAIL":
        compiler = REG_EXP_EMAIL;
        break;
      case "REG_EXP_PHONE":
        compiler = REG_EXP_PHONE;
        break;
      case "REG_EXP_URL":
        compiler = REG_EXP_URL;
        break;
      default:
        compiler = "";
        break;
    }
    Boolean validationResult = false;
    Pattern pattern = Pattern.compile(compiler);
    Matcher mather = pattern.matcher(value);
    if (mather.find()) {
      validationResult = Boolean.TRUE;
    }
    return validationResult;
  }
}

