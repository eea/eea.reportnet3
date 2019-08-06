package org.eea.validation.util;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * The Class ValidationRuleDrools.
 */
@Component("codeListUtils")
public class CodeListUtils {

  final static List<String> listBWDStatus = Arrays.asList("0", "1", "2", "3", "4");
  final static List<String> listBWDPeriodType =
      Arrays.asList("bathingSeason", "shortTermPollution", "abnormalSituation", "qualityChanges",
          "bathingProhibition", "inaccessible", "cyanobacteriaBloom", "other");
  final static List<String> listBWDSampleStatus = Arrays.asList("missingSample", "preSeasonSample",
      "shortTermPollutionSample", "replacementSample");
  final static List<String> listBWDObservationStatus =
      Arrays.asList("missingValue", "confirmedValue", "limitOfDetectionValue");



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

  private static Boolean codeListBWDStatus(String value) {
    if (listBWDStatus.stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }

  private static Boolean codeListBWDPeriodType(String value) {
    if (listBWDPeriodType.stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }

  private static Boolean codeListBWDSampleStatus(String value) {
    if (listBWDSampleStatus.stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }

  private static Boolean codeListBWDObservationStatus(String value) {
    if (listBWDObservationStatus.stream()
        .anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }

}

