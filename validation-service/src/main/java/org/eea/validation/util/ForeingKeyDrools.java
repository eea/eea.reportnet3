package org.eea.validation.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.eea.multitenancy.TenantResolver;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.service.ValidationService;
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
import org.eea.validation.util.querysdrools.DataToQueryabc;
import org.eea.validation.util.querysdrools.DataToQueryd;
import org.eea.validation.util.querysdrools.DataToQueryh;
import org.eea.validation.util.querysdrools.DataToQuerylm;
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

  private final static Pattern dataValue = Pattern.compile("[A-Z]{2,}[0-9_-][^-_]${2,40}");

  private static DataToQueryabc abc = new DataToQueryabc();
  private static DataToQuery11 d = new DataToQuery11();
  private static DataToQuery1par1 d2 = new DataToQuery1par1();
  private static DataToQueryd d3 = new DataToQueryd();
  private static DataToQuery12 e = new DataToQuery12();
  private static DataToQuery12part1 e2 = new DataToQuery12part1();
  private static DataToQuery11par1 e3 = new DataToQuery11par1();
  private static DataToQuery2 e4 = new DataToQuery2();
  private static DataToQuery21 f = new DataToQuery21();
  private static DataToQuery2part1 f2 = new DataToQuery2part1();
  private static DataToQuery21part1 f3 = new DataToQuery21part1();
  private static DataToQuery22 f4 = new DataToQuery22();
  private static DataToQueryh h = new DataToQueryh();
  private static DataToQuery22part1 h2 = new DataToQuery22part1();
  private static DataToQuery3 i = new DataToQuery3();
  private static DataToQuery31 i2 = new DataToQuery31();
  private static DataToQuery3part1 i3 = new DataToQuery3part1();
  private static DataToQuery31part1 i4 = new DataToQuery31part1();
  private static DataToQuery32 i5 = new DataToQuery32();
  private static DataToQuerylm lm = new DataToQuerylm();
  private static DataToQuery32part1 npr = new DataToQuery32part1();
  private static DataToQuery4 s = new DataToQuery4();
  private static DataToQuery41 u = new DataToQuery41();


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

    if (StringUtils.isNotBlank(value)) {
      switch (value.toLowerCase().trim().substring(0, 1)) {
        case ("a"):
        case ("b"):
        case ("c"):
          return a(value);
        case ("d"):
          return d(value);
        case ("e"):
          return e(value);
        case ("f"):
          return f(value);
        case ("h"):
          return h(value);
        case ("i"):
          return i(value);
        case ("l"):
        case ("m"):
          return l(value);
        case ("n"):
        case ("p"):
        case ("r"):
          return n(value);
        case ("s"):
          return s(value);
        case ("u"):
          return u(value);
        default:
          return false;
      }
    }
    return false;
  }


  private static Boolean u(String value) {
    if (u.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }


  private static Boolean s(String value) {
    if (s.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }


  private static Boolean n(String value) {
    if (npr.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }


  private static Boolean l(String value) {
    if (lm.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }


  private static Boolean i(String value) {
    if (i.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || i2.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || i3.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || i4.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || i5.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }


  private static Boolean h(String value) {
    if (h.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || h2.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }


  private static Boolean f(String value) {
    if (f.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || f2.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || f3.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || f4.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }


  private static Boolean e(String value) {
    if (e.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || e2.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || e3.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || e4.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }


  private static Boolean d(String value) {
    if (d.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || d2.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || d3.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }


  private static Boolean a(String value) {
    if (abc.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;

  }

  /**
   * Checks if is in same record.
   *
   * @param value the value
   * @param record the record
   * @param position the position
   * @param valueToHave the value to have
   * @return the boolean
   */
  public static Boolean isInSameRecord(String value, RecordValue record, Integer position,
      String valueToHave) {

    if (record.getFields().get(position).getValue().equalsIgnoreCase(valueToHave)) {
      return true;
    }
    return false;
  }

  public static Boolean ruleSVA(String value) {

    if ("".equalsIgnoreCase(value.trim())) {
      return true;
    }
    String countryCode = TenantResolver.getVariable("countryCode").toString().replace("'", "");
    if (!countryCode.equals(value.substring(0, 2))) {
      return false;
    } else {
      value = value.substring(2);
    }
    Matcher expression = null;
    try {
      expression = dataValue.matcher(value);
    } catch (Exception e) {
      return false;
    }
    return expression.matches() == Boolean.TRUE ? Boolean.TRUE : false;
  }
}
