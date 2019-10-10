package org.eea.validation.util;

import org.eea.validation.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The Class DatasetValidations.
 */
@Component("datasetValidations")
public class DatasetValidations {


  /** The validation service. */
  @Qualifier("proxyValidationService")
  private static ValidationService validationService;

  @Autowired
  private void setDatasetRepository(ValidationService validationService) {
    DatasetValidations.validationService = validationService;
  }

  // # DO02 # The SeasonalPeriod file does not contain the bathing season for a
  // bathingWaterIdentifier and season reported in the Characterisation file. #

  /**
   * Dataset validation DO 02.
   *
   * @param idDataset the id dataset
   * @return the boolean
   */
  public static Boolean datasetValidationDO02(Long idDataset) {
    String DO02 = "WITH Characterisation AS " + "    (SELECT "
        + "    (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c39') as season, "
        + "    (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c42') as bathingWaterIdentifier "
        + "    FROM dataset_" + idDataset + ".record_value rv), " + " RecordsBathingSeason as( "
        + "    select fv.id_record as id_record " + "    FROM dataset_" + idDataset
        + ".Field_Value fv " + "    WHERE    fv.value in('bathingSeason','delisted') "
        + "    and fv.id_field_schema='5d5cfa24d201fb6084d90c8e'), " + "SeasonalPeriod AS ( "
        + "    SELECT " + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where  field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90c7c') as season, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90c8e') as periodType, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90c97') as startDate, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90ca0') as endDate, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where  field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90c85') as bathingWaterIdentifier, "
        + "    fv.id_record " + "    FROM dataset_" + idDataset
        + ".Field_Value fv, RecordsBathingSeason rv WHERE fv.id_record=rv.id_record) "
        + "SELECT a.bathingWaterIdentifier, a.season " + "FROM Characterisation a "
        + "LEFT JOIN SeasonalPeriod b " + "ON a.bathingWaterIdentifier = b.bathingWaterIdentifier "
        + "AND a.season = b.season " + "WHERE b.bathingWaterIdentifier is null "
        + "and a.bathingWaterIdentifier is not null";

    return validationService.datasetValidationDO02Query(DO02);
  }

  // # DO03 # The MonitoringResult file does not contain samples for a bathingWaterIdentifier and
  // season reported in the SeasonalPeriod file. #

  /**
   * Dataset validation DO 03.
   *
   * @param idDataset the id dataset
   * @return the boolean
   */
  public static Boolean datasetValidationDO03(Long idDataset) {

    String DO03 = "WITH MonitoringResult AS (" + "    SELECT "
        + "    (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90cbf') as season, "
        + "    (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90cc8') as bathingWaterIdentifier, "
        + "    (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90cd1') as sampleDate "
        + "    FROM dataset_" + idDataset + ".record_value rv), " + "RecordsBathingSeason as( "
        + "    select fv.id_record as id_record " + " FROM dataset_" + idDataset
        + ".Field_Value fv " + "     WHERE " + "     fv.value='bathingSeason' "
        + "     and fv.id_field_schema='5d5cfa24d201fb6084d90c8e'), " + "SeasonalPeriod AS ("
        + "    SELECT " + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where  field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90c7c') as season, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90c8e') as periodType, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90c97') as startDate, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90ca0') as endDate, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where  field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90c85') as bathingWaterIdentifier, "
        + "    fv.id_record " + "    FROM dataset_" + idDataset
        + ".Field_Value fv, RecordsBathingSeason rv " + "     WHERE"
        + "     fv.id_record=rv.id_record " + ") " + "SELECT a.bathingWaterIdentifier,a.season "
        + "FROM SeasonalPeriod a " + "LEFT JOIN MonitoringResult b "
        + "ON a.bathingWaterIdentifier = b.bathingWaterIdentifier " + " AND a.season = b.season "
        + " AND b.sampleDate between a.startDate and a.endDate "
        + "GROUP BY a.bathingWaterIdentifier, a.season,a.periodType "
        + "HAVING count(b.bathingWaterIdentifier) = 0";

    return validationService.datasetValidationDO03Query(DO03);
  }

  // # DC01A # The Characterisation file contain a record for an unknown bathing water. #

  /**
   * Dataset validation DC 01 A.
   *
   * @param idDataset the id dataset
   * @return the boolean
   */
  public static Boolean datasetValidationDC01A(Long idDataset) {
    String DC01A = "WITH Characterisation AS ( " + "    SELECT "
        + "    (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c39') as season, "
        + "    (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c42') as bathingWaterIdentifier "
        + "    FROM dataset_" + idDataset + ".record_value rv), " + "Recordswise_spatial as( "
        + "    select fv.id_record as id_record " + "    FROM dataset_" + idDataset
        + ".Field_Value fv " + "    WHERE fv.id_field_schema='5d5e907738a84f33484e5127'),   "
        + "wise_spatial AS ( " + "    SELECT "
        + "    (select field_value.VALUE from dataset_18.field_value field_value where  field_value.id_record=rv.id_record and field_value.id_field_schema='5d5e907738a84f33484e511e') as cYear, "
        + "    (select field_value.VALUE from dataset_18.field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5e907738a84f33484e5127') as thematicIdIdentifier, "
        + "    (select field_value.VALUE from dataset_18.field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5e907738a84f33484e5130') as zoneType, "
        + "    (select field_value.VALUE from dataset_18.field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5e907738a84f33484e5139') as statusCode, "
        + "    (select field_value.VALUE from dataset_18.field_value field_value where  field_value.id_record=rv.id_record and field_value.id_field_schema='5d5e907738a84f33484e5142') as URI, "
        + "    (select field_value.VALUE from dataset_18.field_value field_value where  field_value.id_record=rv.id_record and field_value.id_field_schema='5d5e907738a84f33484e514b') as countryCode, "
        + "    fv.id_record " + "    FROM dataset_" + idDataset
        + ".Field_Value fv, Recordswise_spatial rv " + "     WHERE fv.id_record=rv.id_record) "
        + "SELECT a.bathingWaterIdentifier " + "FROM Characterisation a "
        + "LEFT JOIN wise_spatial b " + "ON a.bathingWaterIdentifier = b.thematicIdIdentifier  "
        + "WHERE b.thematicIdIdentifier IS null " + "    and a.bathingWaterIdentifier IS not null ";
    return validationService.datasetValidationDC01AQuery(DC01A);
  }

  // # DC01B # The Characterisation file contain a record for a deprecated bathing water. #
  /**
   * Dataset validation DC 01 B.
   *
   * @param idDataset the id dataset
   * @return the boolean
   */
  public static Boolean datasetValidationDC01B(Long idDataset) {
    String DC01B = "WITH Characterisation AS ( " + "    SELECT "
        + "    (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c39') as season, "
        + "    (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c42') as bathingWaterIdentifier "
        + "    FROM dataset_" + idDataset + ".record_value rv), " + "Recordswise_spatial as( "
        + "    select fv.id_record as id_record " + "    FROM dataset_" + idDataset
        + ".Field_Value fv " + "    WHERE fv.id_field_schema='5d5e907738a84f33484e5127'), "
        + "wise_spatial AS ( " + "    SELECT "
        + "    (select field_value.VALUE from dataset_18.field_value field_value where  field_value.id_record=rv.id_record and field_value.id_field_schema='5d5e907738a84f33484e511e') as cYear, "
        + "    (select field_value.VALUE from dataset_18.field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5e907738a84f33484e5127') as thematicIdIdentifier, "
        + "    (select field_value.VALUE from dataset_18.field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5e907738a84f33484e5130') as zoneType, "
        + "    (select field_value.VALUE from dataset_18.field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5e907738a84f33484e5139') as statusCode, "
        + "    (select field_value.VALUE from dataset_18.field_value field_value where  field_value.id_record=rv.id_record and field_value.id_field_schema='5d5e907738a84f33484e5142') as URI, "
        + "    (select field_value.VALUE from dataset_18.field_value field_value where  field_value.id_record=rv.id_record and field_value.id_field_schema='5d5e907738a84f33484e514b') as countryCode, "
        + "    fv.id_record " + "    FROM dataset_" + idDataset
        + ".Field_Value fv, Recordswise_spatial rv " + "     WHERE fv.id_record=rv.id_record) "
        + "SELECT b.thematicIdIdentifier,b.URI " + "FROM Characterisation a "
        + "LEFT JOIN wise_spatial b " + "ON a.bathingWaterIdentifier = b.thematicIdIdentifier "
        + "AND b.statusCode not in ('deprecated','retired','superseded') "
        + "WHERE b.thematicIdIdentifier IS NOT NULL ";
    return validationService.datasetValidationDC01BQuery(DC01B);
  }

  // # DC02 # The SeasonalPeriod file contains a record for a bathingWaterIdentifier and season not
  // reported in the Characterisation file. #
  /**
   * Dataset validation DC 02.
   *
   * @param idDataset the id dataset
   * @return the boolean
   */
  public static Boolean datasetValidationDC02(Long idDataset) {

    String DC02 = "WITH Characterisation AS ( " + "SELECT "
        + "    (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c39') as season, "
        + "    (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c42') as bathingWaterIdentifier "
        + "    FROM dataset_" + idDataset + ".record_value rv), " + "RecordsBathingSeason as( "
        + "    select fv.id_record as id_record    FROM dataset_" + idDataset
        + ".Field_Value fv    WHERE " + "    fv.value='bathingSeason' "
        + "    and fv.id_field_schema='5d5cfa24d201fb6084d90c8e'), " + "SeasonalPeriod AS ( "
        + "    SELECT " + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where  field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90c7c') as season, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90c8e') as periodType, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90c97') as startDate, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90ca0') as endDate, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where  field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90c85') as bathingWaterIdentifier, "
        + "    fv.id_record    FROM dataset_" + idDataset
        + ".Field_Value fv, RecordsBathingSeason rv " + "     WHERE fv.id_record=rv.id_record) "
        + "SELECT a.bathingWaterIdentifier, a.season " + "FROM Characterisation a "
        + "LEFT JOIN SeasonalPeriod b " + "ON a.bathingWaterIdentifier = b.bathingWaterIdentifier "
        + "   AND a.season = b.season " + "   AND b.periodType = 'bathingSeason' "
        + "WHERE b.bathingWaterIdentifier is null and a.bathingWaterIdentifier is not null ";
    return validationService.datasetValidationDC02Query(DC02);
  }

  // # DC03 # The MonitoringResult file contains samples for a bathingWaterIdentifier and season not
  // reported in the SeasonalPeriod file. #
  /**
   * Dataset validation DC 03.
   *
   * @param idDataset the id dataset
   * @return the boolean
   */
  public static Boolean datasetValidationDC03(Long idDataset) {
    String DC03 = "WITH MonitoringResult AS (    SELECT "
        + "    (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90cbf') as season, "
        + "    (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90cc8') as bathingWaterIdentifier, "
        + "    (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90cd1') as sampleDate "
        + "    FROM dataset_" + idDataset + ".record_value rv), " + "RecordsBathingSeason as( "
        + "    select fv.id_record as id_record FROM dataset_" + idDataset
        + ".Field_Value fv     WHERE "
        + "     fv.value='bathingSeason'     and fv.id_field_schema='5d5cfa24d201fb6084d90c8e'), "
        + "SeasonalPeriod AS (    SELECT " + "    (select field_value.VALUE from dataset_"
        + idDataset
        + ".field_value field_value where  field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90c7c') as season, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90c8e') as periodType, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90c97') as startDate, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90ca0') as endDate, "
        + "    (select field_value.VALUE from dataset_" + idDataset
        + ".field_value field_value where  field_value.id_record=rv.id_record and field_value.id_field_schema='5d5cfa24d201fb6084d90c85') as bathingWaterIdentifier, "
        + "    fv.id_record    FROM dataset_" + idDataset
        + ".Field_Value fv, RecordsBathingSeason rv " + "     WHERE     fv.id_record=rv.id_record) "
        + "     SELECT a.bathingWaterIdentifier, a.season, a.sampleDate "
        + "FROM MonitoringResult a " + "LEFT JOIN SeasonalPeriod b "
        + "ON a.bathingWaterIdentifier = b.bathingWaterIdentifier " + "   AND a.season = b.season "
        + "   AND b.periodType = 'bathingSeason' "
        + "WHERE b.bathingWaterIdentifier IS null and  a.bathingWaterIdentifier is not null ";

    return validationService.datasetValidationDC03Query(DC03);
  }

  public static Boolean datasetValidationDC02B(Long idDataset) {
    String DC02B = " WITH SeasonalPeriod as("
        + "select bathingWaterIdentifierTable.bathingWaterIdentifier as BW_IDENT,"
        + "seasonTable.season as SEASON," + "periodeTypeTable.periodeType as PERIODE_TYPE FROM ("
        + "select v.value as bathingWaterIdentifier, v.id_record as record_id from dataset_"
        + idDataset + ".field_value v "
        + "where v.id_field_schema = '5d5cfa24d201fb6084d90c85') as bathingWaterIdentifierTable "
        + "inner join(" + "select v.value as season, v.id_record as record_id" + " from dataset_"
        + idDataset + ".field_value v "
        + "where v.id_field_schema = '5d5cfa24d201fb6084d90c7c') as seasonTable "
        + "on bathingWaterIdentifierTable.record_id = seasonTable.record_id inner join("
        + "select v.value as periodeType, v.id_record as record_id from dataset_" + idDataset
        + ".field_value v "
        + "where v.id_field_schema = '5d5cfa24d201fb6084d90c8e') as periodeTypeTable "
        + "on seasonTable.record_id = periodeTypeTable.record_id) "
        + "SELECT a.BW_IDENT, a.SEASON FROM seasonalPeriod a "
        + "WHERE a.PERIODE_TYPE != 'delisted' "
        + "and exists(select b.PERIODE_TYPE from seasonalPeriod b "
        + "where b.PERIODE_TYPE = 'delisted' AND a.BW_IDENT = b.BW_IDENT and a.SEASON = b.SEASON)";

    return validationService.datasetValidationDC03Query(DC02B);
  }

  public static Boolean ruleEM01(Long idDataset) {
    String EM01 = "with Characterisation as( " + "SELECT      "
        + "(select v.value  as season from dataset_" + idDataset
        + ".field_value v where v.id_record=rv.id and v.id_field_schema='5d5cfa24d201fb6084d90c39') as season, "
        + "(select v.value as bathingWaterIdentifier from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90c42') as bathingWaterIdentifier, "
        + "(select v.value as sampleDate from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90c4b') as groupIdentifier, "
        + "(select v.value as intestinalEnterococciValue from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90c54') as qualityClass, "
        + "(select v.value as escherichiaColiValue from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90c5d') as geographicalConstraint, "
        + "(select v.value as sampleStatus from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90c66') as link,  "
        + "(select v.value as intestinalEnterococciStatus from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90c6f') as Remarks "
        + "FROM dataset_" + idDataset + ".record_value rv) " + "select * from ( "
        + "select c.season,c.bathingWaterIdentifier,c.groupIdentifier,c.qualityClass,c.geographicalConstraint,c.link,c.Remarks "
        + "from Characterisation c "
        + "group by c.season,c.bathingWaterIdentifier,c.groupIdentifier,c.qualityClass,c.geographicalConstraint,c.link,c.Remarks "
        + "having count(*) >1   " + ") as tabledata where tabledata is not null ";
    return validationService.datasetValidationDC03Query(EM01);
  }

  public static Boolean ruleEM02(Long idDataset) {
    String EM02 = "with SeasonalPeriod as( " + "SELECT      "
        + "(select v.value  as season from dataset_" + idDataset
        + ".field_value v where v.id_record=rv.id and v.id_field_schema='5d5cfa24d201fb6084d90c7c') as season, "
        + "(select v.value as bathingWaterIdentifier from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90c85') as bathingWaterIdentifier, "
        + "(select v.value as sampleDate from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90c8e') as periodType, "
        + "(select v.value as intestinalEnterococciValue from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90c97') as startDate, "
        + "(select v.value as escherichiaColiValue from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90ca0') as endDate, "
        + "(select v.value as sampleStatus from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90ca9') as managementMeasures,  "
        + "(select v.value as intestinalEnterococciStatus from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90cb2') as Remarks "
        + "FROM dataset_" + idDataset + ".record_value rv) " + "select * from ( "
        + "select s.season,s.bathingWaterIdentifier,s.periodType,s.startDate,s.endDate,s.managementMeasures,s.Remarks "
        + "from SeasonalPeriod s "
        + "group by s.season,s.bathingWaterIdentifier,s.periodType,s.startDate,s.endDate,s.managementMeasures,s.Remarks "
        + "having count(*) >1   " + ") as tabledata where tabledata is not null ";
    return validationService.datasetValidationDC03Query(EM02);
  }

  public static Boolean ruleEM03(Long idDataset) {
    String EM03 = "with MonitoringResult as( " + "SELECT      "
        + "(select v.value  as season from dataset_" + idDataset
        + ".field_value v where v.id_record=rv.id and v.id_field_schema='5d5cfa24d201fb6084d90cbf') as season, "
        + "(select v.value as bathingWaterIdentifier from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90cc8') as bathingWaterIdentifier, "
        + "(select v.value as sampleDate from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90cd1') as sampleDate, "
        + "(select v.value as intestinalEnterococciValue from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90cda') as intestinalEnterococciValue, "
        + "(select v.value as escherichiaColiValue from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90ce3') as escherichiaColiValue, "
        + "(select v.value as sampleStatus from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90cec') as sampleStatus,  "
        + "(select v.value as intestinalEnterococciStatus from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90cf5') as intestinalEnterococciStatus, "
        + "(select v.value as escherichiaColiStatus from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90cfe') as escherichiaColiStatus, "
        + "(select v.value as remarks from dataset_" + idDataset
        + ".field_value v where v.id_record = rv.id and v.id_field_schema = '5d5cfa24d201fb6084d90d07') as remarks "
        + "FROM dataset_" + idDataset + ".record_value rv) " + "select * from ( "
        + "select m.season,m.bathingWaterIdentifier,m.sampleDate,m.intestinalEnterococciValue,m.escherichiaColiValue,m.sampleStatus,m.intestinalEnterococciStatus, m.escherichiaColiStatus,m.remarks "
        + "from MonitoringResult m "
        + "group by m.season,m.bathingWaterIdentifier,m.sampleDate,m.intestinalEnterococciValue,m.escherichiaColiValue,m.sampleStatus,m.intestinalEnterococciStatus, m.escherichiaColiStatus,m.remarks "
        + "having count(*) >1   " + ") as tabledata where tabledata is not null";
    return validationService.datasetValidationDC03Query(EM03);
  }
}
