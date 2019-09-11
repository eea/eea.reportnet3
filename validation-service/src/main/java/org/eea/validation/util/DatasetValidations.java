package org.eea.validation.util;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.eea.validation.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("datasetValidations")
public class DatasetValidations {


  @Qualifier("proxyValidationService")
  private static ValidationService validationService;

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private void setDatasetRepository(ValidationService validationService) {
    DatasetValidations.validationService = validationService;
  }

  // # DO02 # The SeasonalPeriod file does not contain the bathing season for a
  // bathingWaterIdentifier and season reported in the Characterisation file. #
  public static Boolean datasetValidationDO02(Long idDataset) {
    String DO02 = "WITH Characterisation AS " + "    (SELECT "
        + "    (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c39') as season, "
        + "    (select field_value.value from dataset_" + idDataset
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5cfa24d201fb6084d90c42') as bathingWaterIdentifier "
        + "    FROM dataset_" + idDataset + ".record_value rv), " + " RecordsBathingSeason as( "
        + "    select fv.id_record as id_record " + "    FROM dataset_" + idDataset
        + ".Field_Value fv " + "    WHERE    fv.value='bathingSeason' "
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

  public static Boolean datasetValidationDO03(Long idDataset) {
    // # DO03 # The MonitoringResult file does not contain samples for a bathingWaterIdentifier and
    // season reported in the SeasonalPeriod file. #
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

  public static Boolean datasetValidationDC01A(Long idDataset) {
    // # DC01A # The Characterisation file contain a record for an unknown bathing water. #
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

  public static Boolean datasetValidationDC01B(Long idDataset) {
    // # DC01B # The Characterisation file contain a record for a deprecated bathing water. #
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


}
