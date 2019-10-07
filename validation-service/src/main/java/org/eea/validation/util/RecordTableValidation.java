package org.eea.validation.util;

import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
import org.eea.validation.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("recordTableValidation")
public class RecordTableValidation {

  /**
   * The validation service.
   */
  @Qualifier("proxyValidationService")
  private static ValidationService validationService;

  @Autowired
  private void setDatasetRepository(ValidationService validationService) {
    RecordTableValidation.validationService = validationService;
  }


  ////////////////////////// PART OF CHARACTERIZTION////////////////////////////////
  public static Boolean RD03ARule(Long idDataset, String idSchemaGroupIdentifier,
      String idSchemaBathingWaterIdentifier) {

    String RD03ARule = "with characterisation as("
        + "select groupIdentifier.groupIdentifier as groupIdentifier,"
        + " bathingWaterIdentifier.bathingWaterIdentifier as bathingWaterIdentifier,"
        + "groupIdentifier.record as recordId from( select v.value as groupIdentifier, v.id_record as record "
        + "from dataset_" + idDataset + ".field_value v where v.id_field_schema ='"
        + idSchemaGroupIdentifier + "' and v.value !='') as groupIdentifier "
        + " inner join( select v.value as bathingWaterIdentifier, v.id_record as record "
        + "from dataset_" + idDataset + ".field_value v where v.id_field_schema ='"
        + idSchemaBathingWaterIdentifier + "' and v.value !='') as bathingWaterIdentifier "
        + "on groupIdentifier.record = bathingWaterIdentifier.record) " + " select a.recordId "
        + " from characterisation a join characterisation b "
        + " on a.groupIdentifier = b.bathingWaterIdentifier;";

    String MessageError =
        "The group identifier is identical to an existing bathing water identifier.";

    return validationService.tableRecordRIds(RD03ARule, MessageError, TypeErrorEnum.ERROR,
        "Characterisation");

  }

  public static Boolean RD03BRule(Long idDataset, Long datasetLegazy) {

    String RD03BRule = "  with characterization as(" + " SELECT "
        + "        (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c42') as bathingWaterIdentifier, "
        + "        (select         " + "        case " + "            when dataset_" + idDataset
        + ".is_numeric(field_value.value)= true"
        + "                then cast (field_value.value as INTEGER)" + "            when dataset_"
        + idDataset + ".is_numeric(field_value.value)= false"
        + "                then cast('0000' as INTEGER)" + "            end       "
        + "        from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c39') as season,"
        + "        (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c4b') as groupIdentifier,"
        + "        rv.id as idrecord" + "        FROM dataset_" + idDataset + ".record_value rv),"
        + "        LEGAZY_CHAR as(SELECT" + "        (select field_value.value from dataset_"
        + datasetLegazy
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d95c46d39786d35d95e2aca') as bathingWaterIdentifier, "
        + "        (select " + "        case " + "            when dataset_" + idDataset
        + ".is_numeric(field_value.value)= true"
        + "                then cast (field_value.value as INTEGER)" + "            when dataset_"
        + idDataset + ".is_numeric(field_value.value)= false"
        + "                then cast('0000' as INTEGER)" + "            end       "
        + "            from dataset_" + datasetLegazy
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d95c468822c6b00d3cba2fc') as season,"
        + "        (select field_value.value from dataset_" + datasetLegazy
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d95c4737194d80d25e67b4b') as groupIdentifier"
        + "        ,rv.id as idrecord" + "        FROM dataset_" + datasetLegazy
        + ".record_value rv)" + "" + "    " + " " + "select a.idrecord" + "from characterization a "
        + "inner join LEGAZY_CHAR b" + "ON a.bathingWaterIdentifier = b.bathingWaterIdentifier "
        + "AND (a.season) = cast(b.season as INTEGER)"
        + "AND coalesce(a.groupIdentifier,'') != coalesce(b.groupIdentifier,'')";

    String MessageError =
        "The group identifier of the bathing water has changed since last season. ";
    return validationService.tableRecordRIds(RD03BRule, MessageError, TypeErrorEnum.WARNING,
        "Characterisation");

  }

  public static Boolean RD03CRule(Long idDataset, Long datasetLegazy) {

    String RD03CRule = "with characterization as(" + " SELECT "
        + "        (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c42') as bathingWaterIdentifier, "
        + "        (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c39') as season,"
        + "        (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c4b') as groupIdentifier,"
        + "    rv.id as idrecord" + "    FROM dataset_" + idDataset + ".record_value rv)," + "    "
        + "    BW_IMPORT as(" + "    SELECT" + "            (select field_value.value from dataset_"
        + datasetLegazy
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d95c724c47d87218a3c336d') as groupIdentifier, "
        + "            (select field_value.value from dataset_" + datasetLegazy
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d95c7294c9467a291f6e7f7') as members "
        + "    FROM dataset_" + datasetLegazy + ".record_value rv)" + "" + "" + "" + "" + "    "
        + " " + "select a.idrecord from characterization a " + "inner JOIN(select groupIdentifier,"
        + "string_agg(bathingWaterIdentifier, ', 'ORDER BY bathingWaterIdentifier) AS members "
        + "   from (SELECT DISTINCT bathingWaterIdentifier, groupIdentifier "
        + "        FROM characterization        " + "         WHERE groupIdentifier is not null) a "
        + "   group by groupIdentifier) b" + " on a.groupIdentifier = b.groupIdentifier "
        + "inner JOIN BW_IMPORT c" + " on a.groupIdentifier = c.groupIdentifier"
        + "where b.members != c.members";

    String MessageError = "The group members have changed.";
    return validationService.tableRecordRIds(RD03CRule, MessageError, TypeErrorEnum.ERROR,
        "Characterisation");

  }

  public static Boolean RD20BRule(Long idDataset) {

    String RD20BRule = "with MonitoringResult as(SELECT  season.season as season, "
        + "bathingWaterIdentifier.bathingWaterIdentifier as bathingWaterIdentifier,"
        + "sampleDate.sampleDate as sampleDate," + "sampleStatus.sampleStatus as sampleStatus,"
        + "season.record as recordId " + "from "
        + "(select v.value  as season , v.id_record as record " + "from dataset_" + idDataset
        + ".field_value v  where v.id_field_schema='5d5cfa24d201fb6084d90cbf') as season "
        + "inner join( " + "select v.value as bathingWaterIdentifier, v.id_record as record "
        + "from dataset_" + idDataset
        + ".field_value v where v.id_field_schema ='5d5cfa24d201fb6084d90cc8') as bathingWaterIdentifier "
        + "on season.record = bathingWaterIdentifier.record " + "inner join(  " + "select  "
        + "    case " + "            when dataset_" + idDataset + ".is_date(v.value)= true "
        + "                then cast (v.value as Date) " + "            when dataset_" + idDataset
        + ".is_date(v.value)= false " + "                then cast('1950-01-01' as Date) "
        + "            end " + "     as sampleDate, v.id_record as record  " + "from dataset_"
        + idDataset
        + ".field_value v where v.id_field_schema ='5d5cfa24d201fb6084d90cd1') as sampleDate "
        + "on bathingWaterIdentifier.record = sampleDate.record " + "inner join(  "
        + "select v.value as sampleStatus, v.id_record as record " + "from dataset_" + idDataset
        + ".field_value v where v.id_field_schema ='5d5cfa24d201fb6084d90cec') as sampleStatus "
        + "on sampleDate.record = sampleStatus.record), " + " SeasionalPeriod as( "
        + "SELECT  season.season as season, "
        + "bathingWaterIdentifier.bathingWaterIdentifier as bathingWaterIdentifier, "
        + "periodType.periodType as periodType, " + "startDate.startDate as startDate, "
        + "endDate.endDate as endDate, " + "season.record as recordId " + "from "
        + "(select v.value  as season , v.id_record as record  " + "from dataset_" + idDataset
        + ".field_value v  where v.id_field_schema='5d5cfa24d201fb6084d90c7c') as season "
        + "inner join(  " + "select v.value as bathingWaterIdentifier, v.id_record as record  "
        + "from dataset_" + idDataset
        + ".field_value v where v.id_field_schema ='5d5cfa24d201fb6084d90c85') as bathingWaterIdentifier "
        + "on season.record = bathingWaterIdentifier.record " + "inner join(  "
        + "select v.value as periodType, v.id_record as record  " + "from dataset_" + idDataset
        + ".field_value v where v.id_field_schema ='5d5cfa24d201fb6084d90c8e') as periodType "
        + "on bathingWaterIdentifier.record = periodType.record " + "inner join(  " + "select  "
        + "    case " + "       when dataset_" + idDataset + ".is_date(v.value)= true "
        + "                then cast (v.value as Date) " + "            when dataset_" + idDataset
        + ".is_date(v.value)= false " + "                then cast('1970-01-01' as Date) "
        + "            end " + "    as startDate, v.id_record as record  " + "from dataset_"
        + idDataset
        + ".field_value v where v.id_field_schema ='5d5cfa24d201fb6084d90c97') as startDate "
        + "on periodType.record = startDate.record " + "inner join(  " + "select  "
        + "        case " + "            when dataset_" + idDataset + ".is_date(v.value)= true "
        + "                then cast (v.value as Date) " + "            when dataset_" + idDataset
        + ".is_date(v.value)= false " + "                then cast('1970-01-01' as Date) "
        + "            end " + "    as endDate, v.id_record as record  " + "from dataset_"
        + idDataset
        + ".field_value v where v.id_field_schema ='5d5cfa24d201fb6084d90ca0') as endDate "
        + "on startDate.record = endDate.record) " + "select a.recordId "
        + "from MonitoringResult a join SeasionalPeriod b "
        + "on a.bathingWaterIdentifier = b.bathingWaterIdentifier " + "and a.season = b.season "
        + "and b.periodType='shortTermPollution' "
        + "and a.sampleDate  between b.startDate AND b.endDate "
        + "and a.sampleStatus not in ('missingSample','shortTermPollutionSample','confirmationSample')";

    String MessageError =
        "The sample was taken during a short-term pollution event, but the sampleStatus is not 'missingSample','shortTermPollutionSample' or 'confirmationSample'";

    return validationService.tableRecordRIds(RD20BRule, MessageError, TypeErrorEnum.ERROR,
        "MonitoringResult");

  }

  public static Boolean RD20CRule(Long idDataset) {

    String RD20CRule = "WITH  " + "SeasonalPeriod AS (  " + "    SELECT  "
        + "        (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c7c') as season,  "
        + "        (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c85') as bathingWaterIdentifier, "
        + "        (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c8e') as periodType,  "
        + "        (select case  " + "            when dataset_" + idDataset
        + ".is_date(field_value.value)= true "
        + "                then cast (field_value.value as Date) " + "            when dataset_"
        + idDataset + ".is_date(field_value.value)= false "
        + "                then cast('1970-01-01' as Date) " + "            end "
        + "            from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c97') as startDate, "
        + "        (select case  " + "            when dataset_" + idDataset
        + ".is_date(field_value.value)= true "
        + "                then cast (field_value.value as Date) " + "            when dataset_"
        + idDataset + ".is_date(field_value.value)= false "
        + "                then cast('1970-01-01' as Date) " + "            end from dataset_"
        + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90ca0') as endDate,  "
        + "        (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90ca9') as managementMeasures, "
        + "        (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90cb2') as Remarks "
        + "    FROM dataset_" + idDataset + ".record_value rv), " + "RecordsBathingSeason as(  "
        + "    select fv.id_record as id_record FROM dataset_" + idDataset
        + ".Field_Value fv  WHERE   fv.value = 'confirmationSample' and fv.id_field_schema='5d5cfa24d201fb6084d90cec'), "
        + "MonitoringResult AS ( " + "    select " + "    (select field_value.VALUE from dataset_"
        + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cbf') as season,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cc8') as bathingWaterIdentifier,  "
        + "    (select case when dataset_" + idDataset + ".is_date(field_value.value)= true "
        + "                then cast (field_value.value as Date) " + "            when dataset_"
        + idDataset + ".is_date(field_value.value)= false "
        + "                then cast('1970-01-01' as Date) " + "            end "
        + "                from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cd1') as sampleDate,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cda') as intestinalEnterococciValue,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90ce3') as escherichiaColiValue,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cec') as sampleStatus,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cf5') as intestinalEnterococciStatus,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cfe') as escherichiaColiStatus, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90d07') as Remarks, "
        + "    fv.id_record as idrecord FROM dataset_" + idDataset
        + ".Field_Value fv,RecordsBathingSeason rv WHERE fv.id_record=rv.id_record), "
        + "vSamplesWithin7Days AS " + " (SELECT a.*, b.endDate " + "  FROM MonitoringResult a "
        + "  JOIN SeasonalPeriod b   "
        + "   ON a.bathingWaterIdentifier = b.bathingWaterIdentifier "
        + "   AND a.season = b.season " + "   AND b.periodType = 'shortTermPollution' "
        + "   AND (b.endDate - a.sampleDate) BETWEEN 1 AND 7  " + "  LEFT JOIN SeasonalPeriod c    "
        + "   ON a.bathingWaterIdentifier = c.bathingWaterIdentifier "
        + "   AND a.season = c.season " + "   AND b.periodType = 'shortTermPollution' "
        + "   AND a.sampleDate BETWEEN c.startDate AND c.endDate "
        + "  WHERE c.bathingWaterIdentifier is null), " + "vFirstSamplingDate AS "
        + " (SELECT bathingWaterIdentifier, season, min(sampleDate) sampleDate, endDate "
        + "  FROM vSamplesWithin7Days  " + "  GROUP BY bathingWaterIdentifier, season, endDate), "
        + "vFirstSample AS  " + " (select a.*  " + "  from vSamplesWithin7Days a "
        + "  join vFirstSamplingDate b "
        + "  on a.bathingWaterIdentifier = b.bathingWaterIdentifier " + "  and a.season = b.season "
        + "  and a.sampleDate = b.sampleDate) " + "  SELECT a.idrecord FROM vFirstSample a "
        + "  WHERE coalesce(sampleStatus,'') not in ('replacementSample','missingSample')";

    String MessageError =
        "The sample is the first taken after a short-term pollution period but the sampleStatus is not 'replacementSample'.";

    return validationService.tableRecordRIds(RD20CRule, MessageError, TypeErrorEnum.ERROR,
        "MonitoringResult");

  }

  public static Boolean RD20DRule(Long idDataset) {

    String RD20DRule = "WITH  " + "SeasonalPeriod AS (  " + "    SELECT  "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c7c') as season,  "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c85') as bathingWaterIdentifier, "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c8e') as periodType,  "
        + "        (select  " + "            case when dataset_" + idDataset
        + " .is_date(field_value.value)= true "
        + "                then cast (field_value.value as Date) " + "            when dataset_"
        + idDataset + " .is_date(field_value.value)= false "
        + "                then cast('1950-01-01' as Date) " + "            end "
        + "        from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c97') as startDate, "
        + "        (select  " + "            case when dataset_" + idDataset
        + " .is_date(field_value.value)= true "
        + "                then cast (field_value.value as Date) " + "            when dataset_"
        + idDataset + " .is_date(field_value.value)= false "
        + "                then cast('1970-01-01' as Date) " + "            end "
        + "        from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90ca0') as endDate,  "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90ca9') as managementMeasures, "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90cb2') as Remarks "
        + "    FROM dataset_" + idDataset + " .record_value rv), " + "RecordsBathingSeason as(  "
        + "    select fv.id_record as id_record FROM dataset_" + idDataset
        + " .Field_Value fv  WHERE   fv.value not in ('preSeasonSample','missingSample') and fv.id_field_schema='5d5cfa24d201fb6084d90cec'), "
        + "MonitoringResult AS ( " + "    select " + "    (select field_value.VALUE from dataset_"
        + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cbf') as season,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cc8') as bathingWaterIdentifier,  "
        + "    (select  " + "         case when dataset_" + idDataset
        + " .is_date(field_value.value)= true "
        + "                then cast (field_value.value as Date) " + "            when dataset_"
        + idDataset + " .is_date(field_value.value)= false "
        + "                then cast('1980-01-01' as Date) " + "            end "
        + "    from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cd1') as sampleDate,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cda') as intestinalEnterococciValue,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90ce3') as escherichiaColiValue,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cec') as sampleStatus,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cf5') as intestinalEnterococciStatus,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cfe') as escherichiaColiStatus, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90d07') as Remarks, "
        + "    fv.id_record as idrecord  FROM dataset_" + idDataset
        + " .Field_Value fv,RecordsBathingSeason rv WHERE fv.id_record=rv.id_record) "
        + "SELECT a.idrecord " + "FROM MonitoringResult a " + "inner JOIN SeasonalPeriod b "
        + " ON a.bathingWaterIdentifier = b.bathingWaterIdentifier " + " AND a.season = b.season "
        + " AND b.periodType = 'bathingSeason' " + " AND a.sampleDate < b.startDate "
        + "LEFT JOIN SeasonalPeriod d " + " ON A.bathingWaterIdentifier = d.bathingWaterIdentifier "
        + " AND a.season = d.season " + " AND d.periodType != 'bathingSeason' "
        + " AND a.sampleDate between d.startDate AND d.endDate "
        + "WHERE d.bathingWaterIdentifier is null "
        + " AND coalesce(a.sampleStatus,'') not in ('preSeasonSample','missingSample')";

    String MessageError =
        "The sample date is before the start of the bathing season and the sampleStatus is not 'preSeasonSample'.";

    return validationService.tableRecordRIds(RD20DRule, MessageError, TypeErrorEnum.ERROR,
        "MonitoringResult");

  }

  public static Boolean RD20ERule(Long idDataset) {

    String RD20ERule = "WITH  " + "SeasonalPeriod AS (  " + "    SELECT  "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c7c') as season,  "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c85') as bathingWaterIdentifier, "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c8e') as periodType,  "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c97') as startDate, "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90ca0') as endDate,  "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90ca9') as managementMeasures, "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90cb2') as Remarks "
        + "    FROM dataset_" + idDataset + " .record_value rv), " + "RecordsBathingSeason as(  "
        + "    select fv.id_record as id_record FROM dataset_" + idDataset
        + " .Field_Value fv  WHERE   fv.value = 'shortTermPollutionSample' and fv.id_field_schema='5d5cfa24d201fb6084d90cec'), "
        + "MonitoringResult AS ( " + "    select " + "    (select field_value.VALUE from dataset_"
        + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cbf') as season,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cc8') as bathingWaterIdentifier,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cd1') as sampleDate,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cda') as intestinalEnterococciValue,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90ce3') as escherichiaColiValue,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cec') as sampleStatus,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cf5') as intestinalEnterococciStatus,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cfe') as escherichiaColiStatus, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90d07') as Remarks, "
        + "    fv.id_record as idrecord  FROM dataset_" + idDataset
        + " .Field_Value fv,RecordsBathingSeason rv WHERE fv.id_record=rv.id_record) "
        + "select a.idrecord " + "  FROM MonitoringResult a " + "  Left JOIN SeasonalPeriod b "
        + "  ON a.bathingWaterIdentifier= b.bathingWaterIdentifier " + "   AND a.season = b.season "
        + "   AND b.periodType = 'shortTermPollution' "
        + "   AND a.sampleDate BETWEEN b.startDate AND b.endDate "
        + "  WHERE b.bathingWaterIdentifier IS NULL "
        + "   AND a.sampleStatus = 'shortTermPollutionSample' " + "   group by a.idrecord "
        + "   having count(*)>1;";

    String MessageError =
        "The sample was not taken within a short-term pollution event and the sampleStatus is 'shortTermPollutionSample'";

    return validationService.tableRecordRIds(RD20ERule, MessageError, TypeErrorEnum.ERROR,
        "MonitoringResult");

  }

  public static Boolean RD20FRule(Long idDataset) {

    String RD20FRule = "WITH  " + "SeasonalPeriod AS (  " + "    SELECT  "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c7c') as season,  "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c85') as bathingWaterIdentifier, "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c8e') as periodType,  "
        + "        (select  " + "        case when dataset_" + idDataset
        + ".is_date(field_value.value)= true "
        + "                then cast (field_value.value as Date) " + "            when dataset_"
        + idDataset + ".is_date(field_value.value)= false "
        + "                then cast('1950-01-01' as Date) " + "            end "
        + "        from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c97') as startDate, "
        + "        (select  " + "        case when dataset_" + idDataset
        + ".is_date(field_value.value)= true "
        + "                then cast (field_value.value as Date) " + "            when dataset_"
        + idDataset + ".is_date(field_value.value)= false "
        + "                then cast('1970-01-01' as Date) " + "            end "
        + "        from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90ca0') as endDate,  "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90ca9') as managementMeasures, "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90cb2') as Remarks "
        + "    FROM dataset_" + idDataset + " .record_value rv), " + "RecordsBathingSeason as(  "
        + "    select fv.id_record as id_record FROM dataset_" + idDataset
        + " .Field_Value fv  WHERE   fv.value = 'preSeasonSample' and fv.id_field_schema='5d5cfa24d201fb6084d90cec'), "
        + "MonitoringResult AS ( " + "    select " + "    (select field_value.VALUE from dataset_"
        + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cbf') as season,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cc8') as bathingWaterIdentifier,  "
        + "    (select  " + "    case when dataset_" + idDataset
        + ".is_date(field_value.value)= true "
        + "                then cast (field_value.value as Date) " + "            when dataset_"
        + idDataset + ".is_date(field_value.value)= false "
        + "                then cast('1980-01-01' as Date) " + "            end "
        + "    from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cd1') as sampleDate,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cda') as intestinalEnterococciValue,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90ce3') as escherichiaColiValue,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cec') as sampleStatus,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cf5') as intestinalEnterococciStatus,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cfe') as escherichiaColiStatus, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90d07') as Remarks, "
        + "    fv.id_record as idrecord  FROM dataset_" + idDataset
        + " .Field_Value fv,RecordsBathingSeason rv WHERE fv.id_record=rv.id_record) "
        + "SELECT a.idrecord " + "  FROM MonitoringResult a " + "  JOIN SeasonalPeriod b "
        + "  ON a.bathingWaterIdentifier = b.bathingWaterIdentifier "
        + "   AND a.season = b.season " + "   AND b.periodType = 'bathingSeason' "
        + "   AND a.sampleDate BETWEEN b.startDate AND b.endDate "
        + "  WHERE a.sampleStatus = 'preSeasonSample'";

    String MessageError =
        "The sample was taken within the bathing season and the sampleStatus is 'preSeasonSample'. ";

    return validationService.tableRecordRIds(RD20FRule, MessageError, TypeErrorEnum.ERROR,
        "MonitoringResult");

  }

  public static Boolean RD20GRule(Long idDataset) {

    String RD20GRule = "WITH  " + "SeasonalPeriod AS (  " + "    SELECT  "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c7c') as season,  "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c85') as bathingWaterIdentifier, "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c8e') as periodType,  "
        + "        (select case  " + "            when dataset_" + idDataset
        + " .is_date(field_value.value)= true "
        + "                then cast (field_value.value as Date) " + "            when dataset_"
        + idDataset + " .is_date(field_value.value)= false "
        + "                then cast('1970-01-01' as Date) " + "            end "
        + "            from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c97') as startDate, "
        + "        (select case  " + "            when dataset_" + idDataset
        + " .is_date(field_value.value)= true "
        + "                then cast (field_value.value as Date) " + "            when dataset_"
        + idDataset + " .is_date(field_value.value)= false "
        + "                then cast('1970-01-01' as Date) " + "            end from dataset_"
        + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90ca0') as endDate,  "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90ca9') as managementMeasures, "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90cb2') as Remarks "
        + "    FROM dataset_" + idDataset + " .record_value rv), " + "RecordsBathingSeason as(  "
        + "    select fv.id_record as id_record FROM dataset_" + idDataset
        + " .Field_Value fv  WHERE   fv.value = 'confirmationSample' and fv.id_field_schema='5d5cfa24d201fb6084d90cec'), "
        + "MonitoringResult AS ( " + "    select " + "    (select field_value.VALUE from dataset_"
        + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cbf') as season,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cc8') as bathingWaterIdentifier,  "
        + "    (select case when dataset_" + idDataset + " .is_date(field_value.value)= true "
        + "                then cast (field_value.value as Date) " + "            when dataset_"
        + idDataset + " .is_date(field_value.value)= false "
        + "                then cast('1970-01-01' as Date) " + "            end "
        + "                from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cd1') as sampleDate,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cda') as intestinalEnterococciValue,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90ce3') as escherichiaColiValue,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cec') as sampleStatus,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cf5') as intestinalEnterococciStatus,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cfe') as escherichiaColiStatus, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90d07') as Remarks, "
        + "    fv.id_record as idrecord FROM dataset_" + idDataset
        + " .Field_Value fv,RecordsBathingSeason rv WHERE fv.id_record=rv.id_record) "
        + " SELECT a.idrecord " + "  FROM MonitoringResult a " + "  LEFT JOIN SeasonalPeriod b   "
        + "    ON a.bathingWaterIdentifier = b.bathingWaterIdentifier "
        + "    AND a.season = b.season " + "    AND b.periodType = 'shortTermPollution' "
        + "    AND ((b.endDate - a.sampleDate) BETWEEN 1 AND 7) "
        + "  WHERE b.bathingWaterIdentifier IS NULL";

    String MessageError =
        "The sample was not taken within 7 days of the end of a short-term pollution event and the sampleStatus is 'replacementSample'.";

    return validationService.tableRecordRIds(RD20GRule, MessageError, TypeErrorEnum.ERROR,
        "MonitoringResult");

  }

  public static Boolean RD20HRule(Long idDataset) {

    String RD20HRule = "WITH  " + "SeasonalPeriod AS (  " + "    SELECT  "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c7c') as season,  "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c85') as bathingWaterIdentifier, "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c8e') as periodType,  "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c97') as startDate, "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90ca0') as endDate,  "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90ca9') as managementMeasures, "
        + "        (select field_value.value from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90cb2') as Remarks "
        + "    FROM dataset_" + idDataset + " .record_value rv), " + "RecordsBathingSeason as(  "
        + "    select fv.id_record as id_record FROM dataset_" + idDataset
        + " .Field_Value fv  WHERE   fv.value = 'confirmationSample' and fv.id_field_schema='5d5cfa24d201fb6084d90cec'), "
        + "MonitoringResult AS ( " + "    select " + "    (select field_value.VALUE from dataset_"
        + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cbf') as season,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cc8') as bathingWaterIdentifier,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cd1') as sampleDate,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cda') as intestinalEnterococciValue,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90ce3') as escherichiaColiValue,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cec') as sampleStatus,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cf5') as intestinalEnterococciStatus,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cfe') as escherichiaColiStatus, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + " .field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90d07') as Remarks, "
        + "    fv.id_record as idrecord  FROM dataset_" + idDataset
        + " .Field_Value fv,RecordsBathingSeason rv WHERE fv.id_record=rv.id_record) "
        + "  SELECT a.idrecord " + "  FROM MonitoringResult a " + "  LEFT JOIN SeasonalPeriod b "
        + "    ON a.bathingWaterIdentifier = b.bathingWaterIdentifier "
        + "    AND a.season = b.season " + "    AND b.periodType = 'shortTermPollution' "
        + "    AND b.endDate =  a.sampleDate " + "  WHERE b.bathingWaterIdentifier IS NULL";

    String MessageError =
        "The sample was not taken in the last day of a short-term pollution event and the sampleStatus is 'confirmationSample'.";

    return validationService.tableRecordRIds(RD20HRule, MessageError, TypeErrorEnum.ERROR,
        "MonitoringResult");

  }

  public static Boolean RD12DRule(Long idDataset) {
    String RD12D = "";
    String MessageError =
        "The endDate is not before the startDate of the next seasonal period of the same type.";
    // return validationService.tableRecordRIds(RD12D, MessageError,
    // TypeErrorEnum.ERROR,"SeasonalPeriod");
    return true;
  }

  public static Boolean RD11ERule(Long idDataset) {
    String RD11E = " ";
    String MessageError =
        "The startDate is not after the endDate of the previous seasonal period of the same type.";
    // return validationService.tableRecordRIds(RD11E, MessageError,
    // TypeErrorEnum.ERROR,"SeasonalPeriod");
    return true;
  }

  public static Boolean RD11FRule(Long idDataset) {

    String RD11F = "";
    String MessageError =
        "The startDate of the bathing season is not after the endDate of the previous bathing season.";
    // return validationService.tableRecordRIds(RD11F, MessageError,
    // TypeErrorEnum.ERROR,"SeasonalPeriod");
    return true;
  }


  public static Boolean RD17BRule(Long idDataset) {

    String RD17B = "WITH  " + "SeasonalPeriod AS (  " + "    SELECT  " + "        (select  "
        + "        case " + "         when dataset_" + idDataset
        + ".is_numeric(field_value.value)= true "
        + "                then cast (field_value.value as NUMERIC) " + "            when dataset_"
        + idDataset + ".is_numeric(field_value.value)= false "
        + "                then cast('0000' as NUMERIC) " + "            end "
        + "        from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c7c') as season,  "
        + "        (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c85') as bathingWaterIdentifier, "
        + "        (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c8e') as periodType,  "
        + "        (select  " + "         case  " + "            when dataset_" + idDataset
        + ".is_date(field_value.value)= true "
        + "                then cast (field_value.value as Date) " + "            when dataset_"
        + idDataset + ".is_date(field_value.value)= false "
        + "                then cast('01/01/1970' as Date) " + "            end "
        + "        from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c97') as startDate, "
        + "        (select  " + "         case  " + "            when dataset_" + idDataset
        + ".is_date(field_value.value)= true "
        + "                then cast (field_value.value as Date) " + "            when dataset_"
        + idDataset + ".is_date(field_value.value)= false "
        + "                then cast('01/01/1970' as Date) " + "            end "
        + "        from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90ca0') as endDate "
        + "    FROM dataset_" + idDataset + ".record_value rv), " + "MonitoringResult AS ( "
        + "    select " + "    (select  " + "    case " + "         when dataset_" + idDataset
        + ".is_numeric(field_value.value)= true "
        + "                then cast (field_value.value as NUMERIC) " + "            when dataset_"
        + idDataset + ".is_numeric(field_value.value)= false "
        + "                then cast('0000' as NUMERIC) " + "            end " + "    from dataset_"
        + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cbf') as season,  "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cc8') as bathingWaterIdentifier,  "
        + "    (select  " + " case  " + "            when dataset_" + idDataset
        + ".is_date(field_value.value)= true "
        + "                then cast (field_value.value as Date) " + "            when dataset_"
        + idDataset + ".is_date(field_value.value)= false "
        + "                then cast('01/01/1970' as Date) " + "            end "
        + "    from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90cd1') as sampleDate, "
        + "    rv.id_record as idrecord   " + "    FROM dataset_" + idDataset + ".Field_Value rv) "
        + "     " + "    select a.idrecord  " + "from MonitoringResult a "
        + "left join SeasonalPeriod b "
        + "  on a.bathingWaterIdentifier = b.bathingWaterIdentifier  "
        + "  and b.periodType = 'bathingSeason' " + "  and a.season = b.season "
        + "  where a.bathingWaterIdentifier is not null "
        + "  and EXTRACT(YEAR FROM CAST(a.sampleDate AS DATE)) != cast(a.season as numeric) "
        + "  and EXTRACT(MONTH FROM CAST(a.sampleDate AS DATE)) -  EXTRACT(MONTH FROM CAST(b.startDate AS DATE)) > 1 "
        + "  and EXTRACT(MONTH FROM CAST(b.startDate AS DATE)) -  EXTRACT(MONTH FROM CAST(b.endDate AS DATE)) != 11";

    String MessageError = "The sampleDate is not in the same year as the bathing season. ";

    return validationService.tableRecordRIds(RD17B, MessageError, TypeErrorEnum.ERROR,
        "MonitoringResult");
  }

}
