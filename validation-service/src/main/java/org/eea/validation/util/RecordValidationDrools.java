package org.eea.validation.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.eea.validation.persistence.data.repository.FieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("recordValidationDrools")
public class RecordValidationDrools {


  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

  private static final Integer MAXIMUN_VALUE_INTESTINAL = 500;

  private static final Integer MAXIMUN_VALUE_ESCHERICHIA = 300;
  /** The field repository impl. */
  private static FieldRepository fieldRepository;

  /**
   * Sets the dataset repository.
   *
   * @param fieldRepositoryImpl the new dataset repository
   */
  @Autowired
  private void setDatasetRepository(FieldRepository fieldRepositoryImpl) {
    RecordValidationDrools.fieldRepository = fieldRepositoryImpl;
  }

  private static String returnValueIdRecordAndIdFieldSchema(Long idRecord, String idFieldSchema) {
    return fieldRepository.findByIdAndIdFieldSchema(idRecord, idFieldSchema);
  }


  public static Boolean periodTypeValidation(Long idRecord, String idFieldSchemaPeriodeType,
      String idFieldShemaStartDate, String idFieldSchemaEnddate) {

    String periodeType =
        returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaPeriodeType).trim();
    String startDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaStartDate).trim();
    String endDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaEnddate).trim();

    if ("shortTermPollution".equalsIgnoreCase(periodeType) && !"".equalsIgnoreCase(startDate)
        && !"".equalsIgnoreCase(endDate)) {
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

      Long dias = ((dateend.getTime() - dateInit.getTime()) / 86400000);

      if (dias >= 3) {
        return false;
      }

    }

    return true;
  }



  public static Boolean startDateGoodDatesOrder(Long idRecord, String idFieldShemaStartDate,
      String idFieldSchemaEnddate) {

    String startDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaStartDate).trim();
    String endDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaEnddate).trim();

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

  public static Boolean unknownDateStart(Long idRecord, String idFieldSchemaPeriodeType,
      String idFieldShemaStartDate) {

    String periodeType =
        returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaPeriodeType).trim();
    String startDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaStartDate).trim();

    if (!"".equalsIgnoreCase(periodeType) && !"".equalsIgnoreCase(startDate)) {
      if (!periodeType.equalsIgnoreCase("qualityChanges")
          && !periodeType.equalsIgnoreCase("abnormalSituation")
          && startDate.equalsIgnoreCase("9999-12-31")) {
        return false;
      }
    }

    return true;
  }


  public static Boolean sameYearValidation(Long idRecord, String idFieldShemaDate,
      String idFieldSchemaSeason) {

    String season = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaSeason).trim();
    String date = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaDate).trim();

    if (!"".equalsIgnoreCase(season) && !"".equalsIgnoreCase(date)) {

      try {
        Integer.valueOf(season);
      } catch (Exception e) {
        return true;
      }
      try {
        Date dateCalculate = sdf.parse(date);
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

  public static Boolean bathingSeasonDurationValidation(Long idRecord,
      String idFieldShemaPeriodType, String idFieldShemaStartDate, String idFieldSchemaEnddate) {

    String periodeType =
        returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaPeriodType).trim();
    String startDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaStartDate).trim();
    String endDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaEnddate).trim();


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

  public static Boolean startAndEndUnknown(Long idRecord, String idFieldShemaStartDate,
      String idFieldSchemaEnddate) {
    String startDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaStartDate).trim();
    String endDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaEnddate).trim();

    if ("9999-12-31".equalsIgnoreCase(startDate) && "9999-12-31".equalsIgnoreCase(endDate)) {
      return false;
    }
    return true;
  }

  public static Boolean endUnknownPeriod(Long idRecord, String idFieldShemaPeriodType,
      String idFieldSchemaEnddate) {
    String periodeType =
        returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaPeriodType).trim();
    String endDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaEnddate).trim();

    if (("abnormalSituation".equalsIgnoreCase(periodeType)
        || "bathingProhibition".equalsIgnoreCase(periodeType)
            && "9999-12-31".equalsIgnoreCase(endDate))) {
      return false;
    }
    return true;
  }



  /////////////////////// MONITORING PART//////////////////////////////////////////////

  // 1
  public static Boolean sameYearValidatioMonitoring(Long idRecord, String idFieldShemaDate,
      String idFieldSchemaSeason) {

    String season = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaSeason).trim();
    String date = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaDate).trim();

    if (!"".equalsIgnoreCase(season.trim()) && !"".equalsIgnoreCase(date)) {

      try {
        Integer.valueOf(season);
      } catch (Exception e) {
        return true;
      }
      try {
        Date dateCalculate = sdf.parse(date);
      } catch (ParseException e) {
        return true;
      }
      if (!date.trim().substring(0, 4).equalsIgnoreCase(season)) {
        return false;
      }
    }

    return true;
  }

  // 2
  public static Boolean maxValueintestinal(Long idRecord,
      String idFieldShemaIntestinalEnterococciValue,
      String idFieldSchemaIntestinalEnterococciStatus) {

    String intestinalEnterococciValue =
        returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaIntestinalEnterococciValue)
            .trim();
    String intestinalEnterococciStatus =
        returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaIntestinalEnterococciStatus)
            .trim();
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


  // 3
  public static Boolean maxValueEscherichiaColi(Long idRecord,
      String idFieldShemaEscherichiaColiEnterococciValue,
      String idFieldSchemaEscherichiaColiEnterococciStatus) {

    String echerichiaColiEnterococciValue =
        returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaEscherichiaColiEnterococciValue)
            .trim();
    String echerichiaColiEnterococciStatus =
        returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaEscherichiaColiEnterococciStatus)
            .trim();

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


  public static Boolean generalStatusValidation(Long idRecord,
      String idFieldShemaIntestinalEnterococciStatus, String idFieldSchemaEscherichiaColiStatus,
      String idFieldSchemaSampleStatus) {

    String intestinalEnterococciStatus =
        returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaIntestinalEnterococciStatus)
            .trim();
    String escherichiaColiStatus =
        returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaEscherichiaColiStatus).trim();
    String sampleStatus =
        returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaSampleStatus).trim();



    if ("missingValue".equalsIgnoreCase(intestinalEnterococciStatus)
        && "missingValue".equalsIgnoreCase(escherichiaColiStatus)
        && !"missingSample".equalsIgnoreCase(sampleStatus) && !"".equalsIgnoreCase(sampleStatus)) {
      return false;
    }
    return true;
  }
  // 5

  // 6

  // 7


  // 8
  public static Boolean intestinalEnterococciValueValidation(Long idRecord,
      String idFieldShemaIntestinalEnterococciValue,
      String idFieldSchemIntestinalEnterococciStatus) {

    String intestinalEnterococciValue =
        returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaIntestinalEnterococciValue)
            .trim();
    String intestinalEnterococciStatus =
        returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemIntestinalEnterococciStatus)
            .trim();

    if (!"".equalsIgnoreCase(intestinalEnterococciValue)
        && !"".equalsIgnoreCase(intestinalEnterococciStatus)) {

      if ("0".equalsIgnoreCase(intestinalEnterococciValue)
          && !"missingValue".equalsIgnoreCase(intestinalEnterococciStatus)) {
        return false;
      }
    }

    return true;
  }

  // 9
  public static Boolean escherichiaColiStatusValidation(Long idRecord,
      String idFieldShemaEscherichiaColiValue, String idFieldSchemaEscherichiaColiStatus) {

    String escherichiaColiValue =
        returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaEscherichiaColiValue).trim();
    String escherichiaColiStatus =
        returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaEscherichiaColiStatus).trim();

    if (!"".equalsIgnoreCase(escherichiaColiValue) && !"".equalsIgnoreCase(escherichiaColiStatus)) {

      if ("0".equalsIgnoreCase(escherichiaColiValue)
          && !"missingValue".equalsIgnoreCase(escherichiaColiStatus)) {
        return false;
      }
    }

    return true;
  }


}
