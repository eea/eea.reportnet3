package org.eea.validation.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The Class RecordValidationDrools.
 */
@Component("recordValidationDrools")
public class RecordValidationDrools {

  /**
   * The validation service.
   */
  @Qualifier("proxyValidationService")
  private static ValidationService validationService;

  @Autowired
  private void setDatasetRepository(ValidationService validationService) {
    RecordValidationDrools.validationService = validationService;
  }

  /** The Constant sdf. */

  /**
   * The Constant MAXIMUN_VALUE_INTESTINAL.
   */
  private static final Integer MAXIMUN_VALUE_INTESTINAL = 35000;

  /**
   * The Constant MAXIMUN_VALUE_ESCHERICHIA.
   */
  private static final Integer MAXIMUN_VALUE_ESCHERICHIA = 35000;

  /**
   * Period type validation.
   *
   * @param recordValue the record value
   * @param idFieldSchemaPeriodeType the id field schema periode type
   * @param idFieldShemaStartDate the id field shema start date
   * @param idFieldSchemaEnddate the id field schema enddate
   *
   * @return the boolean
   */
  public static Boolean periodTypeValidation(RecordValue recordValue,
      String idFieldSchemaPeriodeType, String idFieldShemaStartDate, String idFieldSchemaEnddate) {
    String endDate = "";
    String startDate = "";
    String periodeType = "";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setLenient(false);
    for (FieldValue fieldData : recordValue.getFields()) {

      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldSchemaPeriodeType)) {
        periodeType = fieldData.getValue();
      }
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldShemaStartDate)) {
        startDate = fieldData.getValue();
      }
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldSchemaEnddate)) {
        endDate = fieldData.getValue();
      }
    }
    if ("".equalsIgnoreCase(periodeType) && !"".equalsIgnoreCase(startDate)
        && !"".equalsIgnoreCase(endDate) && null != periodeType && null != startDate
        && null != endDate) {
      Date dateInit;
      Date dateend;
      try {
        dateInit = sdf.parse(startDate);
        dateend = sdf.parse(endDate);
      } catch (ParseException e) {
        return true;
      }

      if (dateInit.after(dateend)) {
        return true;
      }

      Long days = ((dateend.getTime() - dateInit.getTime()) / 86400000);

      if (days >= 3) {
        return false;
      }

    }

    return true;
  }


  /**
   * Start date good dates order.
   *
   * @param recordValue the record value
   * @param idFieldShemaStartDate the id field shema start date
   * @param idFieldSchemaEnddate the id field schema enddate
   *
   * @return the boolean
   */
  public static Boolean startDateGoodDatesOrder(RecordValue recordValue,
      String idFieldShemaStartDate, String idFieldSchemaEnddate) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setLenient(false);
    String endDate = "";
    String startDate = "";
    for (FieldValue fieldData : recordValue.getFields()) {
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldShemaStartDate)) {
        startDate = fieldData.getValue();
      }
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldSchemaEnddate)) {
        endDate = fieldData.getValue();
      }
    }

    if (!"".equalsIgnoreCase(startDate) && !"".equalsIgnoreCase(endDate)) {
      Date dateInit;
      Date dateend;
      try {

        dateInit = sdf.parse(startDate);
        dateend = sdf.parse(endDate);
      } catch (ParseException e) {
        return true;
      }

      if ("9999-12-31".equalsIgnoreCase(startDate)) {
        return true;
      }
      if (dateInit.after(dateend)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Unknown date start.
   *
   * @param recordValue the record value
   * @param idFieldSchemaPeriodeType the id field schema periode type
   * @param idFieldShemaStartDate the id field shema start date
   *
   * @return the boolean
   */
  public static Boolean unknownDateStart(RecordValue recordValue, String idFieldSchemaPeriodeType,
      String idFieldShemaStartDate) {

    String periodeType = "";
    String startDate = "";
    for (FieldValue fieldData : recordValue.getFields()) {
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldShemaStartDate)) {
        startDate = fieldData.getValue();
      }
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldSchemaPeriodeType)) {
        periodeType = fieldData.getValue();
      }
    }

    if (!"".equalsIgnoreCase(periodeType) && !"".equalsIgnoreCase(startDate)) {
      if (!periodeType.equalsIgnoreCase("qualityChanges")
          && !periodeType.equalsIgnoreCase("abnormalSituation")
          && startDate.equalsIgnoreCase("9999-12-31")) {
        return false;
      }
    }

    return true;
  }


  /**
   * Same year validation.
   *
   * @param recordValue the record value
   * @param idFieldShemaDate the id field shema date
   * @param idFieldSchemaSeason the id field schema season
   *
   * @return the boolean
   */
  public static Boolean sameYearValidation(RecordValue recordValue, String idFieldShemaDate,
      String idFieldSchemaSeason) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setLenient(false);
    String date = "";
    String season = "";
    for (FieldValue fieldData : recordValue.getFields()) {
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldShemaDate)) {
        date = fieldData.getValue();
      }
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldSchemaSeason)) {
        season = fieldData.getValue();
      }
    }
    if (!"".equalsIgnoreCase(season) && !"".equalsIgnoreCase(date)) {

      try {
        Integer.valueOf(season);
      } catch (Exception e) {
        return true;
      }
      try {
        sdf.parse(date);
      } catch (ParseException e) {
        return true;
      }
      if ("9999-12-31".equalsIgnoreCase(date)) {
        return true;
      }
      if (!date.substring(0, 4).equalsIgnoreCase(season)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Bathing season duration validation.
   *
   * @param recordValue the record value
   * @param idFieldSchemaPeriodeType the id field schema periode type
   * @param idFieldShemaStartDate the id field shema start date
   * @param idFieldSchemaEnddate the id field schema enddate
   *
   * @return the boolean
   */
  public static Boolean bathingSeasonDurationValidation(RecordValue recordValue,
      String idFieldSchemaPeriodeType, String idFieldShemaStartDate, String idFieldSchemaEnddate) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setLenient(false);
    String endDate = "";
    String startDate = "";
    String periodeType = "";
    for (FieldValue fieldData : recordValue.getFields()) {
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldSchemaPeriodeType)) {
        periodeType = fieldData.getValue();
      }
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldShemaStartDate)) {
        startDate = fieldData.getValue();
      }
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldSchemaEnddate)) {
        endDate = fieldData.getValue();
      }
    }

    if ("bathingSeason".equalsIgnoreCase(periodeType) && !"".equalsIgnoreCase(startDate)
        && !"".equalsIgnoreCase(endDate)) {

      Date dateInit;
      Date dateend;
      try {
        dateInit = sdf.parse(startDate);
        dateend = sdf.parse(endDate);
      } catch (ParseException e) {
        return true;
      }

      if ((dateend.getTime() - dateInit.getTime()) / (1000 * 60 * 60 * 24) > 365) {
        return false;
      }

    }

    return true;
  }

  /**
   * Start and end unknown.
   *
   * @param recordValue the record value
   * @param idFieldShemaStartDate the id field shema start date
   * @param idFieldSchemaEnddate the id field schema enddate
   *
   * @return the boolean
   */
  public static Boolean startAndEndUnknown(RecordValue recordValue, String idFieldShemaStartDate,
      String idFieldSchemaEnddate) {
    String endDate = "";
    String startDate = "";
    for (FieldValue fieldData : recordValue.getFields()) {
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldShemaStartDate)) {
        startDate = fieldData.getValue();
      }
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldSchemaEnddate)) {
        endDate = fieldData.getValue();
      }
    }
    if ("9999-12-31".equalsIgnoreCase(startDate) && "9999-12-31".equalsIgnoreCase(endDate)) {
      return false;
    }
    return true;
  }

  /**
   * End unknown period.
   *
   * @param recordValue the record value
   * @param idFieldShemaPeriodType the id field shema period type
   * @param idFieldSchemaEnddate the id field schema enddate
   *
   * @return the boolean
   */
  public static Boolean endUnknownPeriod(RecordValue recordValue, String idFieldShemaPeriodType,
      String idFieldSchemaEnddate) {

    String endDate = "";
    String periodeType = "";
    for (FieldValue fieldData : recordValue.getFields()) {
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldShemaPeriodType)) {
        periodeType = fieldData.getValue();
      }
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldSchemaEnddate)) {
        endDate = fieldData.getValue();
      }
    }

    if (("abnormalSituation".equalsIgnoreCase(periodeType)
        || "bathingProhibition".equalsIgnoreCase(periodeType))
        && "9999-12-31".equalsIgnoreCase(endDate)) {
      return false;
    }
    return true;
  }

  /////////////////////// MONITORING PART//////////////////////////////////////////////

  /**
   * Same year validatio monitoring.
   *
   * @param recordValue the record value
   * @param idFieldShemaDate the id field shema date
   * @param idFieldSchemaSeason the id field schema season
   *
   * @return the boolean
   */
  // 1
  public static Boolean sameYearValidatioMonitoring(RecordValue recordValue,
      String idFieldShemaDate, String idFieldSchemaSeason) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setLenient(false);
    String season = "";
    String date = "";
    for (FieldValue fieldData : recordValue.getFields()) {
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldSchemaSeason)) {
        season = fieldData.getValue();
      }
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldShemaDate)) {
        date = fieldData.getValue();
      }
    }

    if (!"".equalsIgnoreCase(season.trim()) && !"".equalsIgnoreCase(date)) {

      try {
        Integer.valueOf(season);
      } catch (Exception e) {
        return true;
      }
      try {
        sdf.parse(date);
      } catch (ParseException e) {
        return true;
      }

      if (!date.trim().substring(0, 4).equalsIgnoreCase(season)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Max valueintestinal.
   *
   * @param recordValue the record value
   * @param idFieldShemaIntestinalEnterococciValue the id field shema intestinal enterococci value
   * @param idFieldSchemaIntestinalEnterococciStatus the id field schema intestinal enterococci
   *        status
   *
   * @return the boolean
   */
  // 2
  public static Boolean maxValueintestinal(RecordValue recordValue,
      String idFieldShemaIntestinalEnterococciValue,
      String idFieldSchemaIntestinalEnterococciStatus) {

    String intestinalEnterococciValue = "";
    String intestinalEnterococciStatus = "";
    for (FieldValue fieldData : recordValue.getFields()) {
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldShemaIntestinalEnterococciValue)) {
        intestinalEnterococciValue = fieldData.getValue();
      }
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldSchemaIntestinalEnterococciStatus)) {
        intestinalEnterococciValue = fieldData.getValue();
      }
    }
    try {
      Integer.valueOf(intestinalEnterococciValue);
    } catch (Exception e) {
      return true;
    }

    if (Integer.valueOf(intestinalEnterococciValue) > MAXIMUN_VALUE_INTESTINAL
        && !"".equalsIgnoreCase(intestinalEnterococciStatus)
        && !"confirmedValue".equalsIgnoreCase(intestinalEnterococciStatus)) {
      return false;
    }
    return true;
  }


  /**
   * Max value escherichia coli.
   *
   * @param recordValue the record value
   * @param idFieldShemaEscherichiaColiEnterococciValue the id field shema escherichia coli
   *        enterococci value
   * @param idFieldSchemaEscherichiaColiEnterococciStatus the id field schema escherichia coli
   *        enterococci status
   *
   * @return the boolean
   */
  // 3
  public static Boolean maxValueEscherichiaColi(RecordValue recordValue,
      String idFieldShemaEscherichiaColiEnterococciValue,
      String idFieldSchemaEscherichiaColiEnterococciStatus) {

    String echerichiaColiEnterococciValue = "";
    String echerichiaColiEnterococciStatus = "";
    for (FieldValue fieldData : recordValue.getFields()) {
      if (fieldData.getIdFieldSchema()
          .equalsIgnoreCase(idFieldShemaEscherichiaColiEnterococciValue)) {
        echerichiaColiEnterococciValue = fieldData.getValue();
      }
      if (fieldData.getIdFieldSchema()
          .equalsIgnoreCase(idFieldSchemaEscherichiaColiEnterococciStatus)) {
        echerichiaColiEnterococciStatus = fieldData.getValue();
      }
    }
    try {
      Integer.valueOf(echerichiaColiEnterococciValue);
    } catch (Exception e) {
      return true;
    }

    if (Integer.valueOf(echerichiaColiEnterococciValue) > MAXIMUN_VALUE_ESCHERICHIA
        && !"".equalsIgnoreCase(echerichiaColiEnterococciStatus)
        && !"confirmedValue".equalsIgnoreCase(echerichiaColiEnterococciStatus)) {
      return false;
    }
    return true;
  }
  // 4


  /**
   * General status validation.
   *
   * @param recordValue the record value
   * @param idFieldShemaIntestinalEnterococciStatus the id field shema intestinal enterococci status
   * @param idFieldSchemaEscherichiaColiStatus the id field schema escherichia coli status
   * @param idFieldSchemaSampleStatus the id field schema sample status
   *
   * @return the boolean
   */
  public static Boolean generalStatusValidation(RecordValue recordValue,
      String idFieldShemaIntestinalEnterococciStatus, String idFieldSchemaEscherichiaColiStatus,
      String idFieldSchemaSampleStatus) {

    String intestinalEnterococciStatus = "";
    String escherichiaColiStatus = "";
    String sampleStatus = "";
    for (FieldValue fieldData : recordValue.getFields()) {
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldShemaIntestinalEnterococciStatus)) {
        intestinalEnterococciStatus = fieldData.getValue();
      }
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldSchemaEscherichiaColiStatus)) {
        escherichiaColiStatus = fieldData.getValue();
      }
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldSchemaSampleStatus)) {
        escherichiaColiStatus = fieldData.getValue();
      }
    }

    if ("missingValue".equalsIgnoreCase(intestinalEnterococciStatus)
        && "missingValue".equalsIgnoreCase(escherichiaColiStatus)
        && !"missingSample".equalsIgnoreCase(sampleStatus) && !"".equalsIgnoreCase(sampleStatus)) {
      return false;
    }
    return true;
  }

  // 5 RULES DONT COMPOSE

  // 6

  // 7


  /**
   * Intestinal enterococci value validation.
   *
   * @param recordValue the record value
   * @param idFieldShemaIntestinalEnterococciValue the id field shema intestinal enterococci value
   * @param idFieldSchemIntestinalEnterococciStatus the id field schem intestinal enterococci status
   *
   * @return the boolean
   */
  // 8
  public static Boolean intestinalEnterococciValueValidation(RecordValue recordValue,
      String idFieldShemaIntestinalEnterococciValue,
      String idFieldSchemIntestinalEnterococciStatus) {

    String intestinalEnterococciValue = "";
    String intestinalEnterococciStatus = "";
    for (FieldValue fieldData : recordValue.getFields()) {
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldShemaIntestinalEnterococciValue)) {
        intestinalEnterococciValue = fieldData.getValue();
      }
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldSchemIntestinalEnterococciStatus)) {
        intestinalEnterococciStatus = fieldData.getValue();
      }
    }
    if (!"".equalsIgnoreCase(intestinalEnterococciValue)
        && !"".equalsIgnoreCase(intestinalEnterococciStatus)) {

      if ("0".equalsIgnoreCase(intestinalEnterococciValue)
          && !"missingValue".equalsIgnoreCase(intestinalEnterococciStatus)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Escherichia coli status validation.
   *
   * @param recordValue the record value
   * @param idFieldShemaEscherichiaColiValue the id field shema escherichia coli value
   * @param idFieldSchemaEscherichiaColiStatus the id field schema escherichia coli status
   *
   * @return the boolean
   */
  // 9
  public static Boolean escherichiaColiStatusValidation(RecordValue recordValue,
      String idFieldShemaEscherichiaColiValue, String idFieldSchemaEscherichiaColiStatus) {

    String escherichiaColiValue = "";
    String escherichiaColiStatus = "";
    for (FieldValue fieldData : recordValue.getFields()) {
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldShemaEscherichiaColiValue)) {
        escherichiaColiValue = fieldData.getValue();
      }
      if (fieldData.getIdFieldSchema().equalsIgnoreCase(idFieldSchemaEscherichiaColiStatus)) {
        escherichiaColiStatus = fieldData.getValue();
      }
    }
    if (!"".equalsIgnoreCase(escherichiaColiValue) && !"".equalsIgnoreCase(escherichiaColiStatus)) {

      if ("0".equalsIgnoreCase(escherichiaColiValue)
          && !"missingValue".equalsIgnoreCase(escherichiaColiStatus)) {
        return false;
      }
    }

    return true;
  }

}
