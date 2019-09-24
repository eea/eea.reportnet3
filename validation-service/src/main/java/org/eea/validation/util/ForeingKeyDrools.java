package org.eea.validation.util;

import org.eea.validation.service.ValidationService;
import org.eea.validation.util.querysdrools.DataToQuery1;
import org.eea.validation.util.querysdrools.DataToQuery11;
import org.eea.validation.util.querysdrools.DataToQuery11par1;
import org.eea.validation.util.querysdrools.DataToQuery12;
import org.eea.validation.util.querysdrools.DataToQuery12part1;
import org.eea.validation.util.querysdrools.DataToQuery1par1;
import org.eea.validation.util.querysdrools.DataToQuery2;
import org.eea.validation.util.querysdrools.DataToQuery21;
import org.eea.validation.util.querysdrools.DataToQuery21part1;
import org.eea.validation.util.querysdrools.DataToQuery22;
import org.eea.validation.util.querysdrools.DataToQuery22part1;
import org.eea.validation.util.querysdrools.DataToQuery2part1;
import org.eea.validation.util.querysdrools.DataToQuery3;
import org.eea.validation.util.querysdrools.DataToQuery31;
import org.eea.validation.util.querysdrools.DataToQuery31part1;
import org.eea.validation.util.querysdrools.DataToQuery32;
import org.eea.validation.util.querysdrools.DataToQuery32part1;
import org.eea.validation.util.querysdrools.DataToQuery3part1;
import org.eea.validation.util.querysdrools.DataToQuery4;
import org.eea.validation.util.querysdrools.DataToQuery41;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The Class ValidationRuleDrools.
 */
@Component("foreingKeyDrools")
public class ForeingKeyDrools {

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

    DataToQuery1par1 dataToQuery1part = new DataToQuery1par1();
    DataToQuery11par1 dataToQuery11par1 = new DataToQuery11par1();
    DataToQuery12part1 dataToQuery12part1 = new DataToQuery12part1();
    DataToQuery2part1 dataToQuery2part1 = new DataToQuery2part1();
    DataToQuery21part1 dtaToQuery21part1 = new DataToQuery21part1();
    DataToQuery22part1 dataToQuery22part1 = new DataToQuery22part1();
    DataToQuery3part1 dataToQuery3part1 = new DataToQuery3part1();
    DataToQuery31part1 dataToQuery31part1 = new DataToQuery31part1();
    DataToQuery32part1 dataToQuery32part1 = new DataToQuery32part1();
    DataToQuery41 dataToQuery41 = new DataToQuery41();



    if (dataToQuery1part.getListData().stream()
        .anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || dataToQuery11par1.getListData().stream()
            .anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || dataToQuery12part1.getListData().stream()
            .anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }

    if (dataToQuery2part1.getListData().stream()
        .anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || dtaToQuery21part1.getListData().stream()
            .anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || dataToQuery22part1.getListData().stream()
            .anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }

    if (dataToQuery3part1.getListData().stream()
        .anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || dataToQuery31part1.getListData().stream()
            .anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || dataToQuery32part1.getListData().stream()
            .anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || dataToQuery41.getListData().stream()
            .anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }

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



  public static Boolean ruleSVA(String value) {
    return true;
    // if ("".equalsIgnoreCase(value.trim())) {
    // return true;
    // }
    // Matcher expression = null;
    // try {
    // expression = dataValue.matcher(value);
    // } catch (Exception e) {
    // return false;
    // }
    // return expression.matches() == true ? true : false;
  }

}
