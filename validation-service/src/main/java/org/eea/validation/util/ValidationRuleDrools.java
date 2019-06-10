package org.eea.validation.util;

import java.util.Date;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationVO;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;

public class ValidationRuleDrools {

  private static final String Boolean = null;

  public static void fillValidation(DataSetVO dataSetVO, String message, String typeError,
      String ruleId) {
    ValidationVO newValidation =
        createValidationObject(message, typeError, ruleId, TypeEntityEnum.DATASET);
    dataSetVO.getValidations().add(newValidation);
  }

  public static void fillValidation(TableVO tableVO, String message, String typeError,
      String ruleId) {
    ValidationVO newValidation =
        createValidationObject(message, typeError, ruleId, TypeEntityEnum.TABLE);
    tableVO.getValidations().add(newValidation);
  }

  public static void fillValidation(FieldVO fieldVO, String message, String typeError,
      String ruleId) {
    ValidationVO newValidation =
        createValidationObject(message, typeError, ruleId, TypeEntityEnum.FIELD);
    fieldVO.getValidations().add(newValidation);
  }

  public static void fillValidation(RecordVO recordVO, String message, String typeError,
      String ruleId) {
    ValidationVO newValidation =
        createValidationObject(message, typeError, ruleId, TypeEntityEnum.RECORD);
    recordVO.getValidations().add(newValidation);
  }

  private static ValidationVO createValidationObject(String message, String typeError,
      String ruleId, TypeEntityEnum typeEntityEnum) {
    ValidationVO newValidation = new ValidationVO();
    newValidation.setLevelError(
        typeError.equalsIgnoreCase("warning") ? TypeErrorEnum.WARNING : TypeErrorEnum.ERROR);
    newValidation.setMessage(message);
    newValidation.setIdRule(ruleId);
    newValidation.setValidationDate(new Date().toString());
    newValidation.setTypeEntity(typeEntityEnum);
    return newValidation;
  }

  public Boolean devolucionTesteo(Boolean testeo) {
    return testeo;
  }
}
