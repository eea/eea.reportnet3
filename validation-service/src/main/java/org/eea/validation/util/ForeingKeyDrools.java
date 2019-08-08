package org.eea.validation.util;

import org.eea.validation.persistence.data.repository.FieldRepositoryImpl;
import org.eea.validation.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The Class ValidationRuleDrools.
 */
@Component("foreingKeyDrools")
public class ForeingKeyDrools {

  /** The dataset repository. */
  private static FieldRepositoryImpl fieldRepositoryImpl;

  @Autowired
  private void setDatasetRepository(FieldRepositoryImpl fieldRepositoryImpl) {
    ForeingKeyDrools.fieldRepositoryImpl = fieldRepositoryImpl;
  }


  @Qualifier("proxyValidationService")
  private static ValidationService validationService;


  @Autowired
  private void setDatasetRepository(ValidationService validationService) {
    ForeingKeyDrools.validationService = validationService;
  }

  /**
   * Query.
   *
   * @param value the value
   * @param fieldSchema the field schema
   * @param nameDataset the name dataset
   * @return the boolean
   */
  public static Boolean queryGetAllFieldValue(String value, String fieldSchema, Long idDataset) {
    if (!"".equals(fieldSchema) && null != fieldSchema) {
      return validationService.findReferenceDrools(value, idDataset, fieldSchema);
    } else {
      return true;
    }
  }


}
