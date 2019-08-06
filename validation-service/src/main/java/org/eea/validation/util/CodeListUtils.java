package org.eea.validation.util;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * The Class ValidationRuleDrools.
 */
@Component("codeListUtils")
public class CodeListUtils {

  /** The Constant listBWDStatus. */
  final static List<String> listBWDStatus = Arrays.asList("0", "1", "2", "3", "4");

  /** The Constant listBWDPeriodType. */
  final static List<String> listBWDPeriodType =
      Arrays.asList("bathingSeason", "shortTermPollution", "abnormalSituation", "qualityChanges",
          "bathingProhibition", "inaccessible", "cyanobacteriaBloom", "other");

  /** The Constant listBWDSampleStatus. */
  final static List<String> listBWDSampleStatus = Arrays.asList("missingSample", "preSeasonSample",
      "shortTermPollutionSample", "replacementSample");

  /** The Constant listBWDObservationStatus. */
  final static List<String> listBWDObservationStatus =
      Arrays.asList("missingValue", "confirmedValue", "limitOfDetectionValue");



  /**
   * Code list validate.
   *
   * @param value the value
   * @param codeList the code list
   * @return the boolean
   */
  public static Boolean codeListValidate(String value, String codeList) {
    switch (codeList) {
      case "BWDStatus":
        return codeListBWDStatus(value);
      case "BWDPeriodType":
        return codeListBWDPeriodType(value);
      case "BWDSampleStatus":
        return codeListBWDSampleStatus(value);
      case "BWDObservationStatus":
        return codeListBWDObservationStatus(value);
      default:
        return false;
    }
  }

  /**
   * Code list BWD status.
   *
   * @param value the value
   * @return the boolean
   */
  private static Boolean codeListBWDStatus(String value) {
    if (listBWDStatus.stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }

  /**
   * Code list BWD period type.
   *
   * @param value the value
   * @return the boolean
   */
  private static Boolean codeListBWDPeriodType(String value) {
    if (listBWDPeriodType.stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }

  /**
   * Code list BWD sample status.
   *
   * @param value the value
   * @return the boolean
   */
  private static Boolean codeListBWDSampleStatus(String value) {
    if (listBWDSampleStatus.stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }

  /**
   * Code list BWD observation status.
   *
   * @param value the value
   * @return the boolean
   */
  private static Boolean codeListBWDObservationStatus(String value) {
    if (listBWDObservationStatus.stream()
        .anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }

}

