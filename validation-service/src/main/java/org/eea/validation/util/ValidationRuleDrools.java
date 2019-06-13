package org.eea.validation.util;

import java.util.ArrayList;
import java.util.Date;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
import org.eea.validation.persistence.data.domain.DatasetValidation;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValidation;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.domain.Validation;

/**
 * The Class ValidationRuleDrools.
 */
public class ValidationRuleDrools {

  /**
   * Fill validation.
   *
   * @param dataSetVO the data set VO
   * @param message the message
   * @param typeError the type error
   * @param ruleId the rule id
   */
  public static void fillValidation(DatasetValue dataSetValue, String message, String typeError,
      String ruleId) {
    Validation newValidation =
        createValidationObject(message, typeError, ruleId, TypeEntityEnum.DATASET);
    DatasetValidation datasetValidation = new DatasetValidation();
    datasetValidation.setValidation(newValidation);
    dataSetValue.getDatasetValidations().add(datasetValidation);
  }

  /**
   * Fill validation.
   *
   * @param tableVO the table VO
   * @param message the message
   * @param typeError the type error
   * @param ruleId the rule id
   */
  public static void fillValidation(TableValue tableValue, String message, String typeError,
      String ruleId) {
    Validation newValidation =
        createValidationObject(message, typeError, ruleId, TypeEntityEnum.TABLE);
    TableValidation tableValidation = new TableValidation();
    tableValidation.setValidation(newValidation);
    tableValue.getTableValidations().add(tableValidation);
  }

  /**
   * Fill validation.
   *
   * @param fieldVO the field VO
   * @param message the message
   * @param typeError the type error
   * @param ruleId the rule id
   */
  public static void fillValidation(FieldValue fieldValue, String message, String typeError,
      String ruleId) {
    Validation newValidation =
        createValidationObject(message, typeError, ruleId, TypeEntityEnum.FIELD);
    FieldValidation fieldValidation = new FieldValidation();
    fieldValidation.setValidation(newValidation);
    if (null == fieldValue.getFieldValidations()) {
      fieldValue.setFieldValidations(new ArrayList<>());
      fieldValue.getFieldValidations().add(fieldValidation);
    }

  }

  /**
   * Fill validation.
   *
   * @param recordVO the record VO
   * @param message the message
   * @param typeError the type error
   * @param ruleId the rule id
   */
  public static void fillValidation(RecordValue recordValue, String message, String typeError,
      String ruleId) {
    Validation newValidation =
        createValidationObject(message, typeError, ruleId, TypeEntityEnum.RECORD);
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setValidation(newValidation);
    recordValue.getRecordValidations().add(recordValidation);
  }

  /**
   * Creates the validation object.
   *
   * @param message the message
   * @param typeError the type error
   * @param ruleId the rule id
   * @param typeEntityEnum the type entity enum
   * @return the validation VO
   */
  private static Validation createValidationObject(String message, String typeError, String ruleId,
      TypeEntityEnum typeEntityEnum) {
    Validation newValidation = new Validation();
    newValidation.setLevelError(
        typeError.equalsIgnoreCase("warning") ? TypeErrorEnum.WARNING : TypeErrorEnum.ERROR);
    newValidation.setMessage(message);
    newValidation.setIdRule(ruleId);
    newValidation.setValidationDate(new Date().toString());
    newValidation.setTypeEntity(typeEntityEnum);
    return newValidation;
  }



}
