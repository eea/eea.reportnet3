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
  public static Boolean isIntervalYear(String value, Integer init, Integer end, Boolean interval) {
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
   * @param moreLess the more less
   * @return the boolean
   */
  public static Boolean actualDateCompare(String dateToCompare, String moreLess) {
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date dateDrools = null;
    try {
      dateDrools = sdf.parse(dateToCompare);
    } catch (ParseException e) {
      return true;
    }
    if ((moreLess.trim().equalsIgnoreCase("MORE") && actualDate.before(dateDrools))) {
      return false;
    }
    if (moreLess.trim().equalsIgnoreCase("MORE/EQUALS")
        && (actualDate.before(dateDrools) || actualDate.equals(dateDrools))) {
      return false;
    }
    if (moreLess.trim().equalsIgnoreCase("LESS") && actualDate.after(dateDrools)) {
      return false;
    }
    if (moreLess.trim().equalsIgnoreCase("LESS/EQUALS") && actualDate.after(dateDrools)
        || actualDate.equals(dateDrools)) {
      return false;
    }
    return true;
  }
}

