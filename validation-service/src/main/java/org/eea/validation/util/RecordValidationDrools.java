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

    String periodeType = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaPeriodeType);
    String startDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaStartDate);
    String endDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaEnddate);

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

    String startDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaStartDate);
    String endDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaEnddate);

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

    String periodeType = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaPeriodeType);
    String startDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaStartDate);

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

    String season = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaSeason);
    String date = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaDate);

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

    String periodeType = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaPeriodType);
    String startDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaStartDate);
    String endDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaEnddate);


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
    String startDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaStartDate);
    String endDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaEnddate);

    if ("9999-12-31".equalsIgnoreCase(startDate) && "9999-12-31".equalsIgnoreCase(endDate)) {
      return false;
    }
    return true;
  }

  public static Boolean endUnknownPeriod(Long idRecord, String idFieldShemaPeriodType,
      String idFieldSchemaEnddate) {
    String periodeType = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaPeriodType);
    String endDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaEnddate);

    if (("abnormalSituation".equalsIgnoreCase(periodeType)
        || "bathingProhibition".equalsIgnoreCase(periodeType))
        && "9999-12-31".equalsIgnoreCase(endDate)) {
      return false;
    }
    return true;
  }



  /////////////////////// MONITORING PART//////////////////////////////////////////////

  public static Boolean sameYearValidatioMonitoring(Long idRecord, String idFieldShemaDate,
      String idFieldSchemaSeason) {

    String season = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaSeason);
    String date = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaDate);

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
      if (!date.substring(0, 4).equalsIgnoreCase(season)) {
        return false;
      }
    }

    return true;
  }
}
