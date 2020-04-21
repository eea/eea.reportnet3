package org.eea.validation.util;

import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.validation.persistence.schemas.rule.Rule;

/**
 * The Class ValidationRuleDrools.
 */
public class AutomaticRules {

  // we use that class to create a specifies rule for any of diferent automatic validation


  /**
   * Creates the required rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   * @return the rule
   */
  public static Rule createRequiredRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, String description) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "isBlank(value)",
        "The value must not be missing or empty", ErrorTypeEnum.ERROR.getValue(), shortCode,
        description);
  }


  /**
   * Creates the number integer automatic rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   * @return the rule
   */
  public static Rule createNumberIntegerAutomaticRule(String referenceId,
      EntityTypeEnum typeEntityEnum, String nameRule, String shortCode, String description) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "isNumberInteger(value)",
        "The field must be a valid number without decimals", ErrorTypeEnum.ERROR.getValue(),
        shortCode, description);
  }

  /**
   * Creates the number decimal automatic rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   * @return the rule
   */
  public static Rule createNumberDecimalAutomaticRule(String referenceId,
      EntityTypeEnum typeEntityEnum, String nameRule, String shortCode, String description) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "isNumberDecimal(value)",
        "The field must be a valid number", ErrorTypeEnum.ERROR.getValue(), shortCode, description);
  }

  /**
   * Creates the automatic date rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   * @return the rule
   */
  public static Rule createDateAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, String description) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "isDateYYYYMMDD(value)",
        "The field must be a valid date(YYYYMMDD) ", ErrorTypeEnum.ERROR.getValue(), shortCode,
        description);
  }



  /**
   * Creates the automatic boolean rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   * @return the rule
   */
  public static Rule createBooleanAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, String description) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "isBoolean(value)",
        "The field must be TRUE OR FALSE", ErrorTypeEnum.ERROR.getValue(), shortCode, description);
  }

  /**
   * Creates the automatic lat rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   * @return the rule
   */
  public static Rule createLatAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, String description) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "isCordenateLat(value)",
        "The field must be a valid Lat(beetween -90 and 90)", ErrorTypeEnum.ERROR.getValue(),
        shortCode, description);
  }

  /**
   * Creates the automatic long rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   * @return the rule
   */
  public static Rule createLongAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, String description) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "isCordenateLong(value)",
        "The field must be a valid Longitude(beetween -180 and 180)",
        ErrorTypeEnum.ERROR.getValue(), shortCode, description);
  }


  /**
   * Creates the codelist automatic rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param codelistItems the code list items
   * @param shortCode the short code
   * @param description the description
   * @return the list
   */
  public static List<Rule> createCodelistAutomaticRule(String referenceId,
      EntityTypeEnum typeEntityEnum, String nameRule, String codelistItems, String shortCode,
      String description) {
    List<Rule> ruleList = new ArrayList();
    // PART INSENSITIVE
    ruleList.add(composeRule(referenceId, typeEntityEnum, nameRule,
        "isCodelistInsensitive(value,'" + codelistItems + "')",
        "The value must be avaliable value in the codelist", ErrorTypeEnum.ERROR.getValue(),
        shortCode, description));
    return ruleList;
  }


  /**
   * Creates the PK automatic rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   * @param tableSchemaId the table schema id
   * @param datasetId the dataset id
   * @return the rule
   */
  public static Rule createPKAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, String description, String tableSchemaId, Long datasetId) {
    Rule rule = composeRule(tableSchemaId, typeEntityEnum, nameRule,
        "isfieldPK(datasetId,'" + referenceId + "',", "The value must be based on criteria.",
        ErrorTypeEnum.ERROR.getValue(), shortCode, description);
    // we add the rule data to take the message if the user edit the rule
    StringBuilder whenCondition = new StringBuilder(rule.getWhenCondition());
    whenCondition = whenCondition.append("'").append(rule.getRuleId().toString()).append("')");
    rule.setWhenCondition(whenCondition.toString());
    rule.setReferenceFieldSchemaPKId(new ObjectId(referenceId));
    return rule;
  }

  /**
   * Creates the url automatic rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   * @return the rule
   */
  public static Rule createUrlAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, String description) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "isURL(value)",
        "The value does not follow the expected syntax for a valid URL.",
        ErrorTypeEnum.ERROR.getValue(), shortCode, description);
  }

  /**
   * Creates the phone automatic rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   * @return the rule
   */
  public static Rule createPhoneAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, String description) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "isPhone(value)",
        "The value does not follow the expected syntax for a valid phone number.",
        ErrorTypeEnum.ERROR.getValue(), shortCode, description);
  }

  /**
   * Creates the email automatic rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   * @return the rule
   */
  public static Rule createEmailAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, String description) {
    return composeRule(referenceId, typeEntityEnum, nameRule, "isEmail(value)",
        "The field must be a valid email format.", ErrorTypeEnum.ERROR.getValue(), shortCode,
        description);
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
   * @param shortCode the short code
   * @param description the description
   * @return the rule
   */
  private static Rule composeRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String whenCondition, String thenCondition0, String thenCondition1,
      String shortCode, String description) {
    final Rule rule = new Rule();
    List<String> thenCondition = new ArrayList<>();
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
