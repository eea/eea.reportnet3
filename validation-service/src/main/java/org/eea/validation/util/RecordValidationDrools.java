package org.eea.validation.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.eea.validation.persistence.data.repository.FieldRepository;
import org.eea.validation.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The Class RecordValidationDrools.
 */
@Component("recordValidationDrools")
public class RecordValidationDrools {

  /** The validation service. */
  @Qualifier("proxyValidationService")
  private static ValidationService validationService;

  @Autowired
  private void setDatasetRepository(ValidationService validationService) {
    RecordValidationDrools.validationService = validationService;
  }

  /** The Constant sdf. */
  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

  /** The Constant MAXIMUN_VALUE_INTESTINAL. */
  private static final Integer MAXIMUN_VALUE_INTESTINAL = 500;

  /** The Constant MAXIMUN_VALUE_ESCHERICHIA. */
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

  /**
   * Return value id record and id field schema.
   *
   * @param idRecord the id record
   * @param idFieldSchema the id field schema
   * @return the string
   */
  private static String returnValueIdRecordAndIdFieldSchema(Long idRecord, String idFieldSchema) {
    return fieldRepository.findByIdAndIdFieldSchema(idRecord, idFieldSchema);
  }


  /**
   * Period type validation.
   *
   * @param idRecord the id record
   * @param idFieldSchemaPeriodeType the id field schema periode type
   * @param idFieldShemaStartDate the id field shema start date
   * @param idFieldSchemaEnddate the id field schema enddate
   * @return the boolean
   */
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



  /**
   * Start date good dates order.
   *
   * @param idRecord the id record
   * @param idFieldShemaStartDate the id field shema start date
   * @param idFieldSchemaEnddate the id field schema enddate
   * @return the boolean
   */
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

  /**
   * Unknown date start.
   *
   * @param idRecord the id record
   * @param idFieldSchemaPeriodeType the id field schema periode type
   * @param idFieldShemaStartDate the id field shema start date
   * @return the boolean
   */
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


  /**
   * Same year validation.
   *
   * @param idRecord the id record
   * @param idFieldShemaDate the id field shema date
   * @param idFieldSchemaSeason the id field schema season
   * @return the boolean
   */
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

  /**
   * Bathing season duration validation.
   *
   * @param idRecord the id record
   * @param idFieldShemaPeriodType the id field shema period type
   * @param idFieldShemaStartDate the id field shema start date
   * @param idFieldSchemaEnddate the id field schema enddate
   * @return the boolean
   */
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

  /**
   * Start and end unknown.
   *
   * @param idRecord the id record
   * @param idFieldShemaStartDate the id field shema start date
   * @param idFieldSchemaEnddate the id field schema enddate
   * @return the boolean
   */
  public static Boolean startAndEndUnknown(Long idRecord, String idFieldShemaStartDate,
      String idFieldSchemaEnddate) {
    String startDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldShemaStartDate).trim();
    String endDate = returnValueIdRecordAndIdFieldSchema(idRecord, idFieldSchemaEnddate).trim();

    if ("9999-12-31".equalsIgnoreCase(startDate) && "9999-12-31".equalsIgnoreCase(endDate)) {
      return false;
    }
    return true;
  }

  /**
   * End unknown period.
   *
   * @param idRecord the id record
   * @param idFieldShemaPeriodType the id field shema period type
   * @param idFieldSchemaEnddate the id field schema enddate
   * @return the boolean
   */
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

  /**
   * Same year validatio monitoring.
   *
   * @param idRecord the id record
   * @param idFieldShemaDate the id field shema date
   * @param idFieldSchemaSeason the id field schema season
   * @return the boolean
   */
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

  /**
   * Max valueintestinal.
   *
   * @param idRecord the id record
   * @param idFieldShemaIntestinalEnterococciValue the id field shema intestinal enterococci value
   * @param idFieldSchemaIntestinalEnterococciStatus the id field schema intestinal enterococci
   *        status
   * @return the boolean
   */
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


  /**
   * Max value escherichia coli.
   *
   * @param idRecord the id record
   * @param idFieldShemaEscherichiaColiEnterococciValue the id field shema escherichia coli
   *        enterococci value
   * @param idFieldSchemaEscherichiaColiEnterococciStatus the id field schema escherichia coli
   *        enterococci status
   * @return the boolean
   */
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


  /**
   * General status validation.
   *
   * @param idRecord the id record
   * @param idFieldShemaIntestinalEnterococciStatus the id field shema intestinal enterococci status
   * @param idFieldSchemaEscherichiaColiStatus the id field schema escherichia coli status
   * @param idFieldSchemaSampleStatus the id field schema sample status
   * @return the boolean
   */
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


  /**
   * Intestinal enterococci value validation.
   *
   * @param idRecord the id record
   * @param idFieldShemaIntestinalEnterococciValue the id field shema intestinal enterococci value
   * @param idFieldSchemIntestinalEnterococciStatus the id field schem intestinal enterococci status
   * @return the boolean
   */
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

  /**
   * Escherichia coli status validation.
   *
   * @param idRecord the id record
   * @param idFieldShemaEscherichiaColiValue the id field shema escherichia coli value
   * @param idFieldSchemaEscherichiaColiStatus the id field schema escherichia coli status
   * @return the boolean
   */
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



  /**
   * Monitoring with period sample status.
   *
   * @param datasetId the dataset id
   * @return the boolean
   */
  public static Boolean monitoringWithPeriodSampleStatus(Long datasetId) {

    String QUERY =
        "with MonitoringResult as( select bathingWaterIdentifierTable.bathingWaterIdentifier as BW_IDENT, "
            + "seasonTable.season as SEASON, sampleDateTable.sampleDate as SAMPLE_DATE, "
            + "sampleStatusTable.sampleStatus as SAMPLE_STATUS, bathingWaterIdentifierTable.record_id as record_id "
            + "from( select v.value as bathingWaterIdentifier, v.id_record as record_id from dataset_"
            + datasetId + ".field_value "
            + "v where v.id_field_schema = '5d5cfa24d201fb6084d90cc8') as bathingWaterIdentifierTable inner join"
            + "( select v.value as season, v.id_record as record_id from dataset_" + datasetId
            + ".field_value v "
            + "where v.id_field_schema = '5d5cfa24d201fb6084d90cbf') as seasonTable on bathingWaterIdentifierTable.record_id = "
            + "seasonTable.record_id inner join( select v.value as "
            + "sampleDate, v.id_record as record_id from dataset_" + datasetId
            + ".field_value v where v.id_field_schema = "
            + "'5d5cfa24d201fb6084d90cd1') as sampleDateTable on seasonTable.record_id = sampleDateTable.record_id "
            + "inner join( select v.value as sampleStatus, v.id_record as record_id from dataset_"
            + datasetId + ".field_value v "
            + "where v.id_field_schema = '5d5cfa24d201fb6084d90cec') as sampleStatusTable on sampleDateTable.record_id = "
            + "sampleStatusTable.record_id) , SeasonalPeriod as( select bathingWaterIdentifierTable.bathingWaterIdentifier as BW_IDENT,"
            + " seasonTable.season as SEASON, periodeTypeTable.periodeType as PERIODE_TYPE,"
            + " startDateTable.startDate as START_DATE, endDateTable.endDate as "
            + "END_DATE from( select v.value as bathingWaterIdentifier, v.id_record as record_id from dataset_"
            + datasetId + ".field_value v "
            + "where v.id_field_schema = '5d5cfa24d201fb6084d90c85') as bathingWaterIdentifierTable inner join( select v.value as season, "
            + "v.id_record as record_id from dataset_" + datasetId
            + ".field_value v where v.id_field_schema = '5d5cfa24d201fb6084d90c7c') "
            + "as seasonTable on bathingWaterIdentifierTable.record_id = seasonTable.record_id inner join( select v.value as periodeType,"
            + " v.id_record as record_id from dataset_" + datasetId
            + ".field_value v where v.id_field_schema = '5d5cfa24d201fb6084d90c8e') "
            + "as periodeTypeTable on seasonTable.record_id = periodeTypeTable.record_id "
            + "inner join( select v.value as startDate, v.id_record as record_id from dataset_"
            + datasetId + ".field_value v where v.id_field_schema "
            + "= '5d5cfa24d201fb6084d90c97') as startDateTable on periodeTypeTable.record_id = startDateTable.record_id "
            + "inner join( select v.value as endDate, v.id_record as record_id from dataset_"
            + datasetId + ".field_value v where v.id_field_schema "
            + "= '5d5cfa24d201fb6084d90ca0') as endDateTable on startDateTable.record_id = endDateTable.record_id) select "
            + " m.record_id from  MonitoringResult m inner JOIN SeasonalPeriod s "
            + "on m.BW_IDENT = s.BW_IDENT and m.SEASON = s.SEASON and s.PERIODE_TYPE = 'shortTermPollution' and m.SAMPLE_DATE "
            + "between s.START_DATE and s.END_DATE and coalesce(m.SAMPLE_STATUS,'') != 'shortTermPollutionSample'";

    return validationService.tableValidationQueryPeriodMonitoring(QUERY);
  }
}
