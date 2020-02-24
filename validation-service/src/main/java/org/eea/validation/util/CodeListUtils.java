package org.eea.validation.util;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * The Class ValidationRuleDrools.
 */
@Component("codeListUtils")
public class CodeListUtils {


  /** The validation helper. */
  private static ValidationHelper validationHelper;

  @Autowired
  private void setValidationHelperRepository(ValidationHelper validationHelper) {
    CodeListUtils.validationHelper = validationHelper;
  }

  /**
   * Code list validate.
   *
   * @param value the value
   * @param idCodelist the id codelist
   * @return the boolean
   */
  public static Boolean codeListValidate(final String value, final Long idCodelist) {
    Boolean codeList = Boolean.FALSE;
    // we can validation helper and put in memory the codeList
    List<String> itemsCodelist = validationHelper.listItemsCodelist(idCodelist);
    // we find all the values avaliables of codelist and we find if the codelist is correct
    for (String item : itemsCodelist) {
      if (item.equalsIgnoreCase(value)) {
        codeList = Boolean.TRUE;
      }
    }
    return codeList;
  }

}

