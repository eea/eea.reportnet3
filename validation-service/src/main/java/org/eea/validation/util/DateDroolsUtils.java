package org.eea.validation.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.joda.time.LocalDate;

/**
 * The Class ValidationRuleDrools.
 */
public class DateDroolsUtils {


  /** The Constant actualYear. */
  final static LocalDate actualYear = new LocalDate();

  /** The Constant actualDate. */
  final static Date actualDate = new Date();


  // este metodo es complejo, si tiene interval a true, los años se restan al total que te digan, si
  // no es calcular entre 2 fechas
  /**
   * Checks if is interval year.
   *
   * @param value the value
   * @param init the init
   * @param end the end
   * @param interval the interval
   * @return the boolean
   */
  public static Boolean isIntervalYear(final String value, final Integer init, final Integer end,
      final Boolean interval) {
    Integer valueInt = null;
    try {
      valueInt = Integer.valueOf(value);
    } catch (NumberFormatException e) {
      return false;
    }
    if (Boolean.TRUE.equals(interval)) {
      if (valueInt < (actualYear.getYear() - init) || valueInt > actualYear.getYear()) {
        return false;
      }
    } else {
      if (valueInt < init || valueInt > end) {
        return false;
      }
    }
    return true;
  }


  /**
   * Actual date compare.
   *
   * @param dateToCompare the date to compare
   * @param condition the condition
   * @return the boolean
   */
  public static Boolean actualDateCompare(final String dateToCompare, final String condition) {
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date dateDrools = null;
    try {
      dateDrools = sdf.parse(dateToCompare);
    } catch (ParseException e) {
      return true;
    }
    if ((condition.equalsIgnoreCase("MORE") && actualDate.before(dateDrools))) {
      return false;
    }
    if (condition.equalsIgnoreCase("MORE/EQUALS")
        && (actualDate.before(dateDrools) || actualDate.equals(dateDrools))) {
      return false;
    }
    if (condition.equalsIgnoreCase("LESS") && actualDate.after(dateDrools)) {
      return false;
    }
    if (condition.equalsIgnoreCase("LESS/EQUALS")
        && (actualDate.after(dateDrools) || actualDate.equals(dateDrools))) {
      return false;
    }
    return true;
  }
}

