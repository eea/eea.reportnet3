package org.eea.validation.util;

import java.util.ArrayList;
import java.util.Date;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
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
   * @param dataSetValue the data set value
   * @param message the message
   * @param typeError the type error
   * @param ruleId the rule id
   * @param originName the origin name
   */
  public static void fillValidation(DatasetValue dataSetValue, String message, String typeError,
      String ruleId, String originName) {
    Validation newValidation =
        createValidationObject(message, typeError, ruleId, EntityTypeEnum.DATASET, originName);
    DatasetValidation datasetValidation = new DatasetValidation();
    datasetValidation.setValidation(newValidation);
    if (null == dataSetValue.getDatasetValidations()) {
      dataSetValue.setDatasetValidations(new ArrayList<>());
    }

    dataSetValue.getDatasetValidations().add(datasetValidation);
  }

  /**
   * Fill validation.
   *
   * @param tableValue the table value
   * @param message the message
   * @param typeError the type error
   * @param ruleId the rule id
   * @param originName the origin name
   */
  public static void fillValidation(TableValue tableValue, String message, String typeError,
      String ruleId, String originName) {
    Validation newValidation =
        createValidationObject(message, typeError, ruleId, EntityTypeEnum.TABLE, originName);
    TableValidation tableValidation = new TableValidation();
    tableValidation.setValidation(newValidation);
    if (null == tableValue.getTableValidations()) {
      tableValue.setTableValidations(new ArrayList<>());
    }
    tableValue.getTableValidations().add(tableValidation);
  }

  /**
   * Fill validation.
   *
   * @param fieldValue the field value
   * @param message the message
   * @param typeError the type error
   * @param ruleId the rule id
   * @param originName the origin name
   */
  public static void fillValidation(FieldValue fieldValue, String message, String typeError,
      String ruleId, String originName) {

    Validation newValidation =
        createValidationObject(message, typeError, ruleId, EntityTypeEnum.FIELD, originName);
    FieldValidation fieldValidation = new FieldValidation();
    fieldValidation.setValidation(newValidation);
    if (null == fieldValue.getFieldValidations()) {
      fieldValue.setFieldValidations(new ArrayList<>());
    }

    fieldValue.getFieldValidations().add(fieldValidation);
  }

  /**
   * Fill validation.
   *
   * @param recordValue the record value
   * @param message the message
   * @param typeError the type error
   * @param ruleId the rule id
   * @param originName the origin name
   */
  public static void fillValidation(RecordValue recordValue, String message, String typeError,
      String ruleId, String originName) {
    Validation newValidation =
        createValidationObject(message, typeError, ruleId, EntityTypeEnum.RECORD, originName);
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setValidation(newValidation);
    if (null == recordValue.getRecordValidations()) {
      recordValue.setRecordValidations(new ArrayList<>());
    }
    recordValue.getRecordValidations().add(recordValidation);
  }

  /**
   * Creates the validation object.
   *
   * @param message the message
   * @param typeError the type error
   * @param ruleId the rule id
   * @param typeEntityEnum the type entity enum
   * @param originName the origin name
   * @return the validation VO
   */
  private static Validation createValidationObject(String message, String typeError, String ruleId,
      EntityTypeEnum typeEntityEnum, String originName) {
    Validation newValidation = new Validation();
    ErrorTypeEnum errorTypeEnum;
    if ((errorTypeEnum = ErrorTypeEnum.valueOf(typeError.toUpperCase())) == null) {
      errorTypeEnum = ErrorTypeEnum.ERROR;
    }

    newValidation.setLevelError(errorTypeEnum);
    newValidation.setMessage(message);
    newValidation.setIdRule(ruleId);
    newValidation.setValidationDate(new Date().toString());
    newValidation.setTypeEntity(typeEntityEnum);
    newValidation.setOriginName(originName);
    return newValidation;
  }

}
