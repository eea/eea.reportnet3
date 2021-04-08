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
   * @param tableName the origin name
   */
  public static void fillValidation(DatasetValue dataSetValue, String message, String typeError,
      String ruleId, String tableName, String shortCode, String fieldName) {
    Validation newValidation = createValidationObject(message, typeError, ruleId,
        EntityTypeEnum.DATASET, tableName, shortCode);
    DatasetValidation datasetValidation = new DatasetValidation();
    datasetValidation.setValidation(newValidation);
    if (null == dataSetValue.getDatasetValidations()) {
      dataSetValue.setDatasetValidations(new ArrayList<>());
    }
    datasetValidation.setDatasetValue(dataSetValue);
    dataSetValue.getDatasetValidations().add(datasetValidation);
  }

  /**
   * Fill validation.
   *
   * @param tableValue the table value
   * @param message the message
   * @param typeError the type error
   * @param ruleId the rule id
   * @param tableName the origin name
   */
  public static void fillValidation(TableValue tableValue, String message, String typeError,
      String ruleId, String tableName, String shortCode, String fieldName) {
    Validation newValidation = createValidationObject(message, typeError, ruleId,
        EntityTypeEnum.TABLE, tableName, shortCode);
    TableValidation tableValidation = new TableValidation();
    tableValidation.setValidation(newValidation);
    if (null == tableValue.getTableValidations()) {
      tableValue.setTableValidations(new ArrayList<>());
    }
    tableValidation.setTableValue(tableValue);
    tableValue.getTableValidations().add(tableValidation);
  }

  /**
   * Fill validation.
   *
   * @param fieldValue the field value
   * @param message the message
   * @param typeError the type error
   * @param ruleId the rule id
   * @param tableName the origin name
   */
  public static void fillValidation(FieldValue fieldValue, String message, String typeError,
      String ruleId, String tableName, String shortCode, String fieldName) {

    Validation newValidation = createValidationObject(message, typeError, ruleId,
        EntityTypeEnum.FIELD, tableName, shortCode);
    newValidation.setFieldName(fieldName);
    FieldValidation fieldValidation = new FieldValidation();
    fieldValidation.setValidation(newValidation);
    if (null == fieldValue.getFieldValidations()) {
      fieldValue.setFieldValidations(new ArrayList<>());
    }
    fieldValidation.setFieldValue(fieldValue);
    fieldValue.getFieldValidations().add(fieldValidation);
  }

  /**
   * Fill validation.
   *
   * @param recordValue the record value
   * @param message the message
   * @param typeError the type error
   * @param ruleId the rule id
   * @param tableName the origin name
   */
  public static void fillValidation(RecordValue recordValue, String message, String typeError,
      String ruleId, String tableName, String shortCode, String fieldName) {
    Validation newValidation = createValidationObject(message, typeError, ruleId,
        EntityTypeEnum.RECORD, tableName, shortCode);
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setValidation(newValidation);
    if (null == recordValue.getRecordValidations()) {
      recordValue.setRecordValidations(new ArrayList<>());
    }
    recordValidation.setRecordValue(recordValue);
    recordValue.getRecordValidations().add(recordValidation);
  }

  /**
   * Creates the validation object.
   *
   * @param message the message
   * @param typeError the type error
   * @param ruleId the rule id
   * @param typeEntityEnum the type entity enum
   * @param tableName the origin name
   * @return the validation VO
   */
  private static Validation createValidationObject(String message, String typeError, String ruleId,
      EntityTypeEnum typeEntityEnum, String tableName, String shortCode) {
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
    newValidation.setTableName(tableName);
    newValidation.setShortCode(shortCode);
    return newValidation;
  }

}
