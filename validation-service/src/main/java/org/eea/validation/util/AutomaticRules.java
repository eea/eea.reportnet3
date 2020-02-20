package org.eea.validation.util;

import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.validation.persistence.schemas.rule.Rule;

/**
 * The Class ValidationRuleDrools.
 */
public class AutomaticRules {

  // WE DEVIDE THAT CLASS TO BE EASY KNOW WHAT AUTOMATIC VALIDATION SHOULD BE FILLED

  private static final String LV_ERROR = "ERROR";


  /**
   * Creates the required rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @return the rule
   */
  public static Rule createRequiredRule(String referenceId, TypeEntityEnum typeEntityEnum,
      String nameRule) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "!isBlank(value)",
        "The field must be filled", LV_ERROR);
  }

  /**
   * Creates the automatic number rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @return the rule
   */
  public static Rule createAutomaticNumberRule(String referenceId, TypeEntityEnum typeEntityEnum,
      String nameRule) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "!isNumber(value)",
        "The field must be a valid number", LV_ERROR);
  }

  /**
   * Creates the automatic date rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @return the rule
   */
  public static Rule createAutomaticDateRule(String referenceId, TypeEntityEnum typeEntityEnum,
      String nameRule) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "!isDateYYYYMMDD(value)",
        "The field must be a valid date(YYYYMMDD) ", LV_ERROR);
  }



  /**
   * Creates the automatic boolean rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @return the rule
   */
  public static Rule createAutomaticBooleanRule(String referenceId, TypeEntityEnum typeEntityEnum,
      String nameRule) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "!isBoolean(value)",
        "The field must be TRUE OR FALSE", LV_ERROR);
  }

  /**
   * Creates the automatic lat rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @return the rule
   */
  public static Rule createAutomaticLatRule(String referenceId, TypeEntityEnum typeEntityEnum,
      String nameRule) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "!isCordenateLat(value)",
        "The field must be a valid Lat(beetween -90 and 90)", LV_ERROR);
  }

  /**
   * Creates the automatic long rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @return the rule
   */
  public static Rule createAutomaticLongRule(String referenceId, TypeEntityEnum typeEntityEnum,
      String nameRule) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "!isCordenateLong(value)",
        "The field must be a valid Longitude(beetween -180 and 180)", LV_ERROR);
  }

  /**
   * Creates the automatic codelist rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param codelistId the codelist id
   * @return the rule
   */
  public static Rule createAutomaticCodelistRule(String referenceId, TypeEntityEnum typeEntityEnum,
      String nameRule, String codelistId) {

    return composeRule(referenceId, typeEntityEnum, nameRule,
        "!isCodeList(value," + codelistId + ")",
        "The value must be avaliable value in the codelist", LV_ERROR);
  }

  /**
   * Compose rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param whenCondition the when condition
   * @param thenCondition0 the then condition 0
   * @param thenCondition1 the then condition 1
   * @return the rule
   */
  private static Rule composeRule(String referenceId, TypeEntityEnum typeEntityEnum,
      String nameRule, String whenCondition, String thenCondition0, String thenCondition1) {
    final Rule rule = new Rule();
    List<String> thenCondition = new ArrayList();
    rule.setRuleId(new ObjectId());
    rule.setReferenceId(new ObjectId(referenceId));
    rule.setAutomatic(Boolean.TRUE);
    rule.setEnabled(Boolean.TRUE);
    rule.setRuleName(nameRule);
    rule.setWhenCondition(whenCondition);
    thenCondition.add(thenCondition0);
    thenCondition.add(thenCondition1);
    rule.setThenCondition(thenCondition);
    rule.setType(typeEntityEnum);
    return rule;
  }
}
