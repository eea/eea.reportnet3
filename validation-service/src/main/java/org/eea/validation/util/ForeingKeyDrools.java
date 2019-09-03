package org.eea.validation.util;

import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.repository.FieldRepositoryImpl;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.clasesToQueryDrools.DataToQuery1;
import org.eea.validation.util.clasesToQueryDrools.DataToQuery11;
import org.eea.validation.util.clasesToQueryDrools.DataToQuery12;
import org.eea.validation.util.clasesToQueryDrools.DataToQuery2;
import org.eea.validation.util.clasesToQueryDrools.DataToQuery21;
import org.eea.validation.util.clasesToQueryDrools.DataToQuery22;
import org.eea.validation.util.clasesToQueryDrools.DataToQuery3;
import org.eea.validation.util.clasesToQueryDrools.DataToQuery31;
import org.eea.validation.util.clasesToQueryDrools.DataToQuery32;
import org.eea.validation.util.clasesToQueryDrools.DataToQuery4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The Class ValidationRuleDrools.
 */
@Component("foreingKeyDrools")
public class ForeingKeyDrools {


  /** The field repository impl. */
  private static FieldRepositoryImpl fieldRepositoryImpl;

  /**
   * Sets the dataset repository.
   *
   * @param fieldRepositoryImpl the new dataset repository
   */
  @Autowired
  private void setDatasetRepository(FieldRepositoryImpl fieldRepositoryImpl) {
    ForeingKeyDrools.fieldRepositoryImpl = fieldRepositoryImpl;
  }



  /** The validation service. */
  @Qualifier("proxyValidationService")
  private static ValidationService validationService;


  /**
   * Sets the dataset repository.
   *
   * @param validationService the new dataset repository
   */
  @Autowired
  private void setDatasetRepository(ValidationService validationService) {
    ForeingKeyDrools.validationService = validationService;
  }


  /**
   * Checks if is query data WDF protect.
   *
   * @param value the value
   * @return the boolean
   */
  public static Boolean isQueryDataWDFProtect(String value) {
    DataToQuery1 part1 = new DataToQuery1();
    DataToQuery11 part11 = new DataToQuery11();
    DataToQuery12 part12 = new DataToQuery12();
    DataToQuery2 part2 = new DataToQuery2();
    DataToQuery21 part21 = new DataToQuery21();
    DataToQuery22 part22 = new DataToQuery22();
    DataToQuery3 part3 = new DataToQuery3();
    DataToQuery31 part31 = new DataToQuery31();
    DataToQuery32 part32 = new DataToQuery32();
    DataToQuery4 part4 = new DataToQuery4();
    if (part1.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || part2.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || part3.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || part4.getListData().stream()
            .anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    if (part11.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || part21.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || part31.getListData().stream()
            .anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    if (part12.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || part22.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || part32.getListData().stream()
            .anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }


  public static Boolean isInSameRecord(String value, RecordValue record, Integer position,
      String valueToHave) {

    if (record.getFields().get(position).getValue().equalsIgnoreCase(valueToHave)) {
      return true;
    }
    return false;
  }
}
