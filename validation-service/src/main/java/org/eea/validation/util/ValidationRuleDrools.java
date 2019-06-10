package org.eea.validation.util;

import java.util.Date;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.DatasetValidationVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.FieldValidationVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.RecordValidationVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.TableValidationVO;
import org.eea.interfaces.vo.dataset.ValidationVO;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;

public class ValidationRuleDrools {

  private static final String Boolean = null;

  public static void fillValidation(DataSetVO dataSetVO, String message, String typeError,
      String ruleId) {
    ValidationVO newValidation =
        createValidationObject(message, typeError, ruleId, TypeEntityEnum.DATASET);
    DatasetValidationVO datasetValidation = new DatasetValidationVO();
    datasetValidation.setValidation(newValidation);
    dataSetVO.getDatasetValidations().add(datasetValidation);
  }

  public static void fillValidation(TableVO tableVO, String message, String typeError,
      String ruleId) {
    ValidationVO newValidation =
        createValidationObject(message, typeError, ruleId, TypeEntityEnum.TABLE);
    TableValidationVO tableValidationVO = new TableValidationVO();
    tableValidationVO.setValidation(newValidation);
    tableVO.getTableValidations().add(tableValidationVO);
  }

  public static void fillValidation(FieldVO fieldVO, String message, String typeError,
      String ruleId) {
    ValidationVO newValidation =
        createValidationObject(message, typeError, ruleId, TypeEntityEnum.FIELD);
    FieldValidationVO fieldValidationVO = new FieldValidationVO();
    fieldValidationVO.setValidation(newValidation);
    fieldVO.getFieldValidations().add(fieldValidationVO);
  }

  public static void fillValidation(RecordVO recordVO, String message, String typeError,
      String ruleId) {
    ValidationVO newValidation =
        createValidationObject(message, typeError, ruleId, TypeEntityEnum.RECORD);
    RecordValidationVO recordValidationVO= new RecordValidationVO();
    recordValidationVO.setValidation(newValidation);
     recordVO.getRecordValidations().add(recordValidationVO);
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
