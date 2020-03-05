package org.eea.validation.util;

import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.validation.persistence.schemas.rule.Rule;

/**
 * The Class ValidationRuleDrools.
 */
public class AutomaticRules {

  // we use that class to create a specifies rule for any of diferent automatic validation

  private static final String LV_ERROR = "ERROR";


  /**
   * Creates the required rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @return the rule
   */
  public static Rule createRequiredRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, String description) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "!isBlank(value)",
        "The value must not be missing or empty", LV_ERROR, shortCode, description);
  }

  /**
   * Creates the automatic number rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @return the rule
   */
  public static Rule createNumberAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, String description) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "!isNumber(value)",
        "The field must be a valid number", LV_ERROR, shortCode, description);
  }

  /**
   * Creates the automatic date rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @return the rule
   */
  public static Rule createDateAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, String description) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "!isDateYYYYMMDD(value)",
        "The field must be a valid date(YYYYMMDD) ", LV_ERROR, shortCode, description);
  }



  /**
   * Creates the automatic boolean rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @return the rule
   */
  public static Rule createBooleanAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, String description) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "!isBoolean(value)",
        "The field must be TRUE OR FALSE", LV_ERROR, shortCode, description);
  }

  /**
   * Creates the automatic lat rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @return the rule
   */
  public static Rule createLatAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, String description) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "!isCordenateLat(value)",
        "The field must be a valid Lat(beetween -90 and 90)", LV_ERROR, shortCode, description);
  }

  /**
   * Creates the automatic long rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @return the rule
   */
  public static Rule createLongAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, String description) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "!isCordenateLong(value)",
        "The field must be a valid Longitude(beetween -180 and 180)", LV_ERROR, shortCode,
        description);
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
  public static Rule createCodelistAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String codelistId, String shortCode, String description) {

    return composeRule(referenceId, typeEntityEnum, nameRule,
        "!isCodeList(value," + codelistId + ")",
        "The value must be avaliable value in the codelist", LV_ERROR, shortCode, description);
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
  private static Rule composeRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String whenCondition, String thenCondition0, String thenCondition1,
      String shortCode, String description) {
    final Rule rule = new Rule();
    List<String> thenCondition = new ArrayList<String>();
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
    rule.setDescription(description);
    rule.setShortCode(shortCode);
    return rule;
  }
}
