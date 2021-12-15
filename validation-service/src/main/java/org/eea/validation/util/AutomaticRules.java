package org.eea.validation.util;

import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.enums.AutomaticRuleTypeEnum;
import org.eea.utils.LiteralConstants;
import org.eea.validation.persistence.schemas.rule.Rule;


/**
 * The Class AutomaticRules.
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
   *
   * @return the rule
   */
  public static Rule createRequiredRulePoint(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, AutomaticRuleTypeEnum automaticType, String description) {
    ObjectId ruleId = new ObjectId();
    return composeRule(ruleId, referenceId, typeEntityEnum, nameRule, "isBlankPoint(this)",
        "The value must not be missing or empty", ErrorTypeEnum.ERROR.getValue(), shortCode,
        automaticType, description, null);
  }

  /**
   * Creates the required rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   *
   * @return the rule
   */
  public static Rule createRequiredRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, AutomaticRuleTypeEnum automaticType, String description) {
    ObjectId ruleId = new ObjectId();
    return composeRule(ruleId, referenceId, typeEntityEnum, nameRule, "isBlank(value)",
        "The value must not be missing or empty", ErrorTypeEnum.ERROR.getValue(), shortCode,
        automaticType, description, null);
  }


  /**
   * Creates the number integer automatic rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   *
   * @return the rule
   */
  public static Rule createNumberIntegerAutomaticRule(String referenceId,
      EntityTypeEnum typeEntityEnum, String nameRule, String shortCode,
      AutomaticRuleTypeEnum automaticType, String description) {
    ObjectId ruleId = new ObjectId();
    return composeRule(ruleId, referenceId, typeEntityEnum, nameRule, "isNumberInteger(value)",
        "The value is not a valid whole number", ErrorTypeEnum.ERROR.getValue(), shortCode,
        automaticType, description, null);
  }

  /**
   * Creates the number decimal automatic rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   *
   * @return the rule
   */
  public static Rule createNumberDecimalAutomaticRule(String referenceId,
      EntityTypeEnum typeEntityEnum, String nameRule, String shortCode,
      AutomaticRuleTypeEnum automaticType, String description) {
    ObjectId ruleId = new ObjectId();
    return composeRule(ruleId, referenceId, typeEntityEnum, nameRule, "isNumberDecimal(value)",
        "The value is not a valid whole or decimal number", ErrorTypeEnum.ERROR.getValue(),
        shortCode, automaticType, description, null);
  }

  /**
   * Creates the automatic date rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   *
   * @return the rule
   */
  public static Rule createDateAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, AutomaticRuleTypeEnum automaticType, String description) {
    ObjectId ruleId = new ObjectId();
    return composeRule(ruleId, referenceId, typeEntityEnum, nameRule, "isDateYYYYMMDD(value)",
        "The value is not a valid date (YYYY-MM-DD)", ErrorTypeEnum.ERROR.getValue(), shortCode,
        automaticType, description, null);
  }

  /**
   * Creates the date time automatic rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   * @return the rule
   */
  public static Rule createDateTimeAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, AutomaticRuleTypeEnum automaticType, String description) {
    ObjectId ruleId = new ObjectId();
    return composeRule(ruleId, referenceId, typeEntityEnum, nameRule, "isDateTime(value)",
        "The value is not a valid datetime YYYY-MM-DDTHH:mm:ss[Z]", ErrorTypeEnum.ERROR.getValue(),
        shortCode, automaticType, description, null);
  }


  /**
   * Creates the automatic boolean rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   *
   * @return the rule
   */
  public static Rule createBooleanAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, AutomaticRuleTypeEnum automaticType, String description) {
    ObjectId ruleId = new ObjectId();
    return composeRule(ruleId, referenceId, typeEntityEnum, nameRule, "isBoolean(value)",
        "The field must be TRUE or FALSE", ErrorTypeEnum.ERROR.getValue(), shortCode, automaticType,
        description, null);
  }

  /**
   * Creates the automatic lat rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   *
   * @return the rule
   */
  public static Rule createLatAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, AutomaticRuleTypeEnum automaticType, String description) {
    ObjectId ruleId = new ObjectId();
    return composeRule(ruleId, referenceId, typeEntityEnum, nameRule, "isCordenateLat(value)",
        "The field must be a valid latitude (between -90 and 90)", ErrorTypeEnum.ERROR.getValue(),
        shortCode, automaticType, description, null);
  }

  /**
   * Creates the automatic long rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   *
   * @return the rule
   */
  public static Rule createLongAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, AutomaticRuleTypeEnum automaticType, String description) {
    ObjectId ruleId = new ObjectId();
    return composeRule(ruleId, referenceId, typeEntityEnum, nameRule, "isCordenateLong(value)",
        "The field must be a valid longitude (between -180 and 180)",
        ErrorTypeEnum.ERROR.getValue(), shortCode, automaticType, description, null);
  }


  /**
   * Creates the codelist automatic rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param singleCodeListItems the single code list items
   * @param shortCode the short code
   * @param description the description
   * @return the list
   */
  public static List<Rule> createCodelistAutomaticRule(String referenceId,
      EntityTypeEnum typeEntityEnum, String nameRule, List<String> singleCodeListItems,
      String shortCode, AutomaticRuleTypeEnum automaticType, String description) {
    ObjectId ruleId = new ObjectId();
    List<Rule> ruleList = new ArrayList<>();
    // PART INSENSITIVE
    // we create the new list to send with ;
    String codelist = "";
    for (int i = 0; i < singleCodeListItems.size(); i++) {
      if (i == 0) {
        codelist = singleCodeListItems.get(0);
      } else {
        codelist =
            new StringBuilder(codelist).append("; ").append(singleCodeListItems.get(i)).toString();
      }
    }
    ruleList.add(composeRule(ruleId, referenceId, typeEntityEnum, nameRule,
        "isCodelistInsensitive(value,'[" + codelist + "]')",
        "The value is not a valid member of the codelist", ErrorTypeEnum.ERROR.getValue(),
        shortCode, automaticType, description, null));
    return ruleList;
  }

  /**
   * Creates the multi select codelist automatic rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param codelistItems the codelist items
   * @param shortCode the short code
   * @param description the description
   *
   * @return the list
   */
  public static List<Rule> createMultiSelectCodelistAutomaticRule(String referenceId,
      EntityTypeEnum typeEntityEnum, String nameRule, List<String> codelistItems, String shortCode,
      AutomaticRuleTypeEnum automaticType, String description) {
    ObjectId ruleId = new ObjectId();
    List<Rule> ruleList = new ArrayList<>();
    // PART INSENSITIVE
    // we create the new list to send with ;
    String codelist = "";
    for (int i = 0; i < codelistItems.size(); i++) {
      if (i == 0) {
        codelist = codelistItems.get(0);
      } else {
        codelist = new StringBuilder(codelist).append("; ").append(codelistItems.get(i)).toString();
      }
    }
    ruleList.add(composeRule(ruleId, referenceId, typeEntityEnum, nameRule,
        "isMultiSelectCodelistValidate(value,'[" + codelist + "]')",
        "The value is not a valid member of the codelist", ErrorTypeEnum.ERROR.getValue(),
        shortCode, automaticType, description, null));
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
   * @param pkMustBeUsed the pk must be used
   *
   * @return the rule
   */
  public static Rule createFKAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, AutomaticRuleTypeEnum automaticType, String description,
      String tableSchemaId, boolean pkMustBeUsed) {
    ObjectId ruleId = new ObjectId();
    String errorMsg = null;
    if (pkMustBeUsed) {
      errorMsg = "Omission - does not contain an expected record based on set criteria.";
    } else {
      errorMsg = "The value is not a valid member of the referenced list.";
    }
    Rule rule = composeRule(ruleId, tableSchemaId, typeEntityEnum, nameRule,
        "isfieldFK(datasetId,'" + referenceId + "',", errorMsg, ErrorTypeEnum.ERROR.getValue(),
        shortCode, automaticType, description, null);
    // we add the rule data to take the message if the user edit the rule
    StringBuilder whenCondition = new StringBuilder(rule.getWhenCondition());
    whenCondition = whenCondition.append("'").append(rule.getRuleId().toString()).append("',")
        .append(String.valueOf(pkMustBeUsed)).append(")");
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
   *
   * @return the rule
   */
  public static Rule createUrlAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, AutomaticRuleTypeEnum automaticType, String description) {
    ObjectId ruleId = new ObjectId();
    return composeRule(ruleId, referenceId, typeEntityEnum, nameRule, "isURL(value)",
        "The value does not follow the expected syntax for a valid URL",
        ErrorTypeEnum.ERROR.getValue(), shortCode, automaticType, description, null);
  }

  /**
   * Creates the phone automatic rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   *
   * @return the rule
   */
  public static Rule createPhoneAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, AutomaticRuleTypeEnum automaticType, String description) {
    ObjectId ruleId = new ObjectId();
    return composeRule(ruleId, referenceId, typeEntityEnum, nameRule, "isPhone(value)",
        "The value does not follow the expected syntax for a valid phone number",
        ErrorTypeEnum.ERROR.getValue(), shortCode, automaticType, description, null);
  }

  /**
   * Creates the email automatic rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   *
   * @return the rule
   */
  public static Rule createEmailAutomaticRule(String referenceId, EntityTypeEnum typeEntityEnum,
      String nameRule, String shortCode, AutomaticRuleTypeEnum automaticType, String description) {
    ObjectId ruleId = new ObjectId();
    return composeRule(ruleId, referenceId, typeEntityEnum, nameRule, "isEmail(value)",
        "The value does not follow the expected syntax for a valid email",
        ErrorTypeEnum.ERROR.getValue(), shortCode, automaticType, description, null);
  }


  /**
   * Creates the geometry automatic rule.
   *
   * @param typeData the type data
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   * @return the rule
   */
  public static Rule createGeometryAutomaticRule(DataType typeData, String referenceId,
      EntityTypeEnum typeEntityEnum, String nameRule, String shortCode,
      AutomaticRuleTypeEnum automaticType, String description) {
    String error = "";
    if (DataType.POLYGON.equals(typeData)) {
      error = LiteralConstants.POLYGONERROR;
    } else {
      error = LiteralConstants.GEOMETRYERROR + typeData.toString().toLowerCase() + " {%reason%} ";
    }
    ObjectId ruleId = new ObjectId();
    return composeRule(ruleId, referenceId, typeEntityEnum, nameRule, "isGeometry(this)", error,
        ErrorTypeEnum.BLOCKER.getValue(), shortCode, automaticType, description, null);
  }

  /**
   * Creates the geometry automatic rule check EPSGSRID.
   *
   * @param typeData the type data
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   * @return the rule
   */
  public static Rule createGeometryAutomaticRuleCheckEPSGSRID(DataType typeData, String referenceId,
      EntityTypeEnum typeEntityEnum, String nameRule, String shortCode,
      AutomaticRuleTypeEnum automaticType, String description) {
    ObjectId ruleId = new ObjectId();
    return composeRule(ruleId, referenceId, typeEntityEnum, nameRule, "checkEPSGSRID(this)",
        "Unsupported SRID", ErrorTypeEnum.BLOCKER.getValue(), shortCode, automaticType, description,
        null);
  }



  /**
   * Creates the geometry automatic rule check geometries.
   *
   * @param typeData the type data
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param automaticType the automatic type
   * @param description the description
   * @return the rule
   */
  public static Rule createGeometryAutomaticRuleCheckGeometries(Long datasetId, Document document,
      DataType typeData, String referenceId, EntityTypeEnum typeEntityEnum, String nameRule,
      String shortCode, AutomaticRuleTypeEnum automaticType, String description) {
    ObjectId ruleId = new ObjectId();

    String message = "Geometry is not valid. Reason: {%reason%}";

    String whenCondition = "isSQLSentenceWithCode(this.datasetId.id, '" + ruleId.toString()
        + "', this.records.size > 0 && this.records.get(0) != null && this.records.get(0).dataProviderCode != null ? this.records.get(0).dataProviderCode : 'XX')";

    String fieldName = document.getString("headerName");

    String sql = "select * from ( select rv.id as record_id ,fv.id as %s_id,"
        + " public.ST_isValidReason(public.ST_SetSRID(public.ST_GeomFromGeoJSON(fv.value::json->'geometry'),"
        + " ((fv.value::json->'properties')::json->>'srid')::integer)) as reason,"
        + " public.ST_SetSRID(public.ST_GeomFromGeoJSON(fv.value::json->'geometry'), "
        + " ((fv.value::json->'properties')::json->>'srid')::integer) as value "
        + " from dataset_%s.field_value fv inner join dataset_%s.record_value rv "
        + " on rv.id = fv.id_record where fv.value <> '' and public.is_valid_json(fv.value) and geometry is null ) tableAux "
        + " where public.ST_isValid(value) = false;";

    String sqlResult = String.format(sql, fieldName, datasetId, datasetId);

    return composeRule(ruleId, referenceId, typeEntityEnum, nameRule, whenCondition, message,
        ErrorTypeEnum.BLOCKER.getValue(), shortCode, automaticType, description, sqlResult);
  }



  /**
   * Creates the geometry automatic rule check S ttransform.
   *
   * @param datasetId the dataset id
   * @param document the document
   * @param typeData the type data
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param automaticType the automatic type
   * @param description the description
   * @return the rule
   */
  public static Rule createGeometryAutomaticRuleCheckSTtransform(Long datasetId, Document document,
      DataType typeData, String referenceId, EntityTypeEnum typeEntityEnum, String nameRule,
      String shortCode, AutomaticRuleTypeEnum automaticType, String description) {
    ObjectId ruleId = new ObjectId();

    String message = "The transformation of the geometry has not worked because of: {%reason%}";

    String whenCondition = "isSQLSentenceWithCode(this.datasetId.id, '" + ruleId.toString()
        + "', this.records.size > 0 && this.records.get(0) != null && this.records.get(0).dataProviderCode != null ? this.records.get(0).dataProviderCode : 'XX')";

    String fieldName = document.getString("headerName");

    String sql =
        "select rv.id as record_id , fv.id as %s_id , fv.geometry_error as reason from dataset_%s.field_value fv inner join dataset_%s.record_value rv on rv.id = fv.id_record where fv.geometry_error is not null and fv.id_field_schema = '%s'";
    String sqlResult = String.format(sql, fieldName, datasetId, datasetId, referenceId);

    return composeRule(ruleId, referenceId, typeEntityEnum, nameRule, whenCondition, message,
        ErrorTypeEnum.BLOCKER.getValue(), shortCode, automaticType, description, sqlResult);
  }


  /**
   * Creates the unique constraint automatic rule.
   *
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param nameRule the name rule
   * @param shortCode the short code
   * @param description the description
   * @param message the message
   * @param uniqueId the unique id
   * @return the rule
   */
  public static Rule createUniqueConstraintAutomaticRule(String referenceId,
      EntityTypeEnum typeEntityEnum, String nameRule, String shortCode,
      AutomaticRuleTypeEnum automaticType, String description, String message, String uniqueId) {
    StringBuilder ruleString =
        new StringBuilder("isUniqueConstraint('").append(uniqueId).append("',");


    ObjectId ruleId = new ObjectId();
    Rule rule = composeRule(ruleId, referenceId, typeEntityEnum, nameRule, ruleString.toString(),
        "Uniqueness and multiplicity constraints - " + message, ErrorTypeEnum.ERROR.getValue(),
        shortCode, automaticType, description, null);

    StringBuilder whenCondition = new StringBuilder(rule.getWhenCondition());
    whenCondition = whenCondition.append("'").append(rule.getRuleId().toString()).append("')");
    rule.setWhenCondition(whenCondition.toString());
    rule.setReferenceFieldSchemaPKId(new ObjectId(referenceId));
    rule.setUniqueConstraintId(new ObjectId(uniqueId));
    return rule;
  }

  /**
   * Compose rule.
   * 
   * @param ruleId
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
  private static Rule composeRule(ObjectId ruleId, String referenceId,
      EntityTypeEnum typeEntityEnum, String nameRule, String whenCondition, String thenCondition0,
      String thenCondition1, String shortCode, AutomaticRuleTypeEnum automaticType,
      String description, String sql) {
    final Rule rule = new Rule();
    List<String> thenCondition = new ArrayList<>();
    rule.setRuleId(ruleId);
    rule.setReferenceId(new ObjectId(referenceId));
    rule.setAutomatic(true);
    rule.setEnabled(true);
    rule.setVerified(true);
    rule.setRuleName(nameRule);
    rule.setWhenCondition(whenCondition);
    thenCondition.add(thenCondition0);
    thenCondition.add(thenCondition1);
    rule.setThenCondition(thenCondition);
    rule.setType(typeEntityEnum);
    rule.setDescription(description);
    rule.setShortCode(shortCode);
    rule.setAutomaticType(automaticType);
    if (null != sql && !sql.isEmpty()) {
      rule.setSqlSentence(sql);
      rule.setExpressionText("");
    }
    return rule;
  }
}
