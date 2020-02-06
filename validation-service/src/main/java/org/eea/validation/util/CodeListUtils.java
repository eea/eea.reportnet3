package org.eea.validation.util;
import java.util.Arrays;
import java.util.List;
/**
 * The Class ValidationRuleDrools.
 */

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
  public static Boolean codeListValidate(final String value, final String codeList) {
    switch (codeList) {
      case "BWDStatus":
        return listBWDStatus.stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value));
      case "BWDPeriodType":
        return listBWDPeriodType.stream()
            .anyMatch(datoString -> datoString.equalsIgnoreCase(value));
      case "BWDSampleStatus":
        return listBWDSampleStatus.stream()
            .anyMatch(datoString -> datoString.equalsIgnoreCase(value));
      case "BWDObservationStatus":
        return listBWDObservationStatus.stream()
            .anyMatch(datoString -> datoString.equalsIgnoreCase(value));
      default:
        return false;
    }
  }

}

