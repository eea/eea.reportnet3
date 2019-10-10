package org.eea.validation.util;

import org.eea.thread.ThreadPropertiesManager;
import org.eea.validation.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The Class TableValidationDrools.
 */
@Component("tableValidationDrools")
public class TableValidationDrools {


  /**
   * The validation service.
   */
  @Qualifier("proxyValidationService")
  private static ValidationService validationService;

  @Autowired
  private void setDatasetRepository(ValidationService validationService) {
    TableValidationDrools.validationService = validationService;
  }

  ////////////////////////////////////////////////////////////////////
  ////////////////////////////////// charaterization //////////////////
  ////////////////////////////////////////////////////////////////////

  // --# DR01A # The Characterisation file contains records for more than one season. #
  // the boolean is to know if the validation has previous years

  /**
   * Rule DR 01 AB.
   *
   * @param idSchema the id schema
   * @param previous the previous
   * @param datasetId the dataset id
   *
   * @return the boolean
   */
  public static Boolean ruleDR01AB(String idSchema, Boolean previous, Long datasetId) {

//    String DR01AB = "select v.value from dataset_" + datasetId
//        + ".field_value v where v.id_field_schema = '" + idSchema + "' group by v.value";

    return true;
    // return validationService.tableValidationDR01ABQuery(DR01AB, previous);
  }

  // # DU01A # The Characterisation file contains more than one record for the
  // bathingWaterIdentifier.

  /**
   * Rule DU 01 A.
   *
   * @param idSchema the id schema
   * @param datasetId the dataset id
   *
   * @return the boolean
   */
  public static Boolean ruleDU01A(String idSchema, Long datasetId) {

    String ruleDU01A = "select v.value FROM dataset_" + datasetId + ".field_value v "
        + "where  v.id_field_schema = '" + idSchema + "' " + "group by v.value "
        + "having COUNT(*) >1";
    return validationService.tableValidationQueryNonReturnResult(ruleDU01A);
  }

  // # DU01B # The Characterisation file contains a groupIdentifier associated with single btahing
  // water. #

  /**
   * Rule DU 01 B.
   *
   * @param idSchema the id schema
   * @param datasetId the dataset id
   *
   * @return the boolean
   */
  public static Boolean ruleDU01B(String idSchema, Long datasetId) {

    String ruleDU01B =
        "select v.value FROM dataset_" + datasetId + ".field_value v where v.id_field_schema = '"
            + idSchema + "' group by v.value" + " having COUNT(*) = 1";
    return validationService.tableValidationQueryNonReturnResult(ruleDU01B);
  }

  // --# DR01A # The Characterisation file contains records for more than one season. #

  /**
   * Rule DO 01.
   *
   * @param idSchemaThematicIdIdentifier the id schema thematic id identifier
   * @param idSchemaStatusCode the id schema status code
   * @param idSchemaCountryCode the id schema country code
   * @param idSchemaBathingWaterIdentifier the id schema bathing water identifier
   * @param idDataset the id dataset
   * @param idDatasetToContribute the id dataset to contribute
   *
   * @return the boolean
   */
  public static Boolean ruleDO01(String idSchemaThematicIdIdentifier, String idSchemaStatusCode,
      String idSchemaCountryCode, String idSchemaBathingWaterIdentifier, Long idDataset,
      String idDatasetToContribute) {
    String countryCode = "''";
    if (null != ThreadPropertiesManager.getVariable("countryCode")
        && ThreadPropertiesManager.getVariable("countryCode").equals("")) {
      countryCode = ThreadPropertiesManager.getVariable("countryCode").toString();
    }
    String ruleDO01 =
        "with sparcial as( select dato1.thematicIdIdentifier as thematicIdIdentifier, "
            + "dato2.statusCode as statusCode, dato3.countryCode as countryCode "
            + "from( select v.value as thematicIdIdentifier, v.id_record as record " + "from "
            + idDatasetToContribute + ".field_value v where v.id_field_schema = '"
            + idSchemaThematicIdIdentifier + "') as dato1 "
            + "inner join( select v.value as statusCode, v.id_record as record from "
            + idDatasetToContribute + ".field_value v where v.id_field_schema = '"
            + idSchemaStatusCode + "') as dato2 "
            + "on  dato1.record = dato2.record inner join( select v.value as countryCode , v.id_record as record "
            + "FROM " + idDatasetToContribute + ".field_value v where v.id_field_schema = '"
            + idSchemaCountryCode + "') as dato3 on dato2.record = dato3.record ),"
            + "characterisation as ( select character1.bathingWaterIdentifier FROM ( "
            + "select v.value as bathingWaterIdentifier FROM dataset_" + idDataset
            + ".field_value v where v.id_field_schema = '" + idSchemaBathingWaterIdentifier
            + "')as character1 ) "
            + "select s.thematicIdIdentifier from  sparcial s left join characterisation c on "
            + "s.thematicIdIdentifier = c.bathingWaterIdentifier where "
            + "s.statusCode NOT in('experimental','retired','superseded') and s.countryCode in("
            + countryCode + ") " + "and c.bathingWaterIdentifier is null";

    return validationService.tableValidationQueryNonReturnResult(ruleDO01);
  }

  ////////////////////////////////////////////////////////////////////
  ////////////////////////////////// SeasonalPeriod //////////////////
  ////////////////////////////////////////////////////////////////////

  // # DU02A # The SeasonalPeriod file contains more than one record for the combination of
  // bathingWaterIdentifier, periodType, startDate and endDate. #

  /**
   * Rule DU 02 A.
   *
   * @param idSchemaBathingWaterIdentifierTable the id schema bathing water identifier table
   * @param idSchemaPeriodType the id schema period type
   * @param idSchemaStartDate the id schema start date
   * @param idSchemaEndDate the id schema end date
   * @param datasetId the dataset id
   *
   * @return the boolean
   */
  public static Boolean ruleDU02A(String idSchemaBathingWaterIdentifierTable,
      String idSchemaPeriodType, String idSchemaStartDate, String idSchemaEndDate, Long datasetId) {

    String ruleDU02A = "select bathingWaterIdentifierTable.bathingWaterIdentifier as BW_IDENT, "
        + "periodeTypeTable.periodType as PERIOD_TYPE, startdateTable.startDate as START_DATE, "
        + "endDateTable.enddate as END_DATE from( select v.value as bathingWaterIdentifier,"
        + " v.id_record as record_id from dataset_" + datasetId
        + ".field_value v where v.id_field_schema = '" + idSchemaBathingWaterIdentifierTable + "') "
        + "as bathingWaterIdentifierTable inner join( select v.value as periodType, v.id_record as record_id "
        + "from dataset_" + datasetId + ".field_value v where v.id_field_schema = '"
        + idSchemaPeriodType + "') as periodeTypeTable on "
        + "bathingWaterIdentifierTable.record_id = periodeTypeTable.record_id inner join( select v.value as startDate, "
        + "v.id_record as record_id from dataset_" + datasetId
        + ".field_value v where v.id_field_schema = '" + idSchemaStartDate + "') as "
        + "startdateTable on periodeTypeTable.record_id = startdateTable.record_id inner join( select v.value as endDate, "
        + "v.id_record as record_id from dataset_" + datasetId
        + ".field_value v where v.id_field_schema = '" + idSchemaEndDate + "') "
        + "as endDateTable on  startdateTable.record_id = endDateTable.record_id GROUP BY BW_IDENT,PERIOD_TYPE,START_DATE,END_DATE"
        + " having count(*) > 1";
    return validationService.tableValidationQueryNonReturnResult(ruleDU02A);
  }

  public static Boolean ruleDU02B(Long datasetId, Long datasetLegazy) {

    String ruleDU02B = "with vMultiple as( "
        + "select bathingWaterIdentifierTable.bathingWaterIdentifier as bathingWaterIdentifier, "
        + "seasonTable.season as season, " + "periodTypeTable.periodType as periodType, "
        + "COUNT(*) " + "from( "
        + "select v.value as bathingWaterIdentifier, v.id_record as record_id " + "from dataset_"
        + datasetId + ".field_value v  "
        + "where v.id_field_schema = '5d5cfa24d201fb6084d90c85') as bathingWaterIdentifierTable "
        + "inner join( " + "select v.value as season, v.id_record as record_id " + "from dataset_"
        + datasetId + ".field_value v  "
        + "where v.id_field_schema = '5d5cfa24d201fb6084d90c7c') as seasonTable "
        + "on bathingWaterIdentifierTable.record_id = seasonTable.record_id " + "inner join( "
        + "select v.value as periodType, v.id_record as record_id " + "from dataset_" + datasetId
        + ".field_value v  "
        + "where v.id_field_schema = '5d5cfa24d201fb6084d90c8e') as periodTypeTable "
        + "on seasonTable.record_id = periodTypeTable.record_id "
        + "GROUP BY bathingWaterIdentifier,season,periodType " + "having count(*) > 1) " + " "
        + ",      BW_LEGAZY as( " + " SELECT  " + "        (select field_value.value from dataset_"
        + datasetLegazy
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5e907738a84f33484e5108') as bathingWaterIdentifier,  "
        + "        (select field_value.value from dataset_" + datasetLegazy
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5e907738a84f33484e50ff') as season, "
        + "        (select field_value.value from dataset_" + datasetLegazy
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d5e907738a84f33484e5111') as periodType,  "
        + "        (select field_value.value from dataset_" + datasetLegazy
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d92fa1ae54a5b94f8afba86') as startDate, "
        + "        (select field_value.value from dataset_" + datasetLegazy
        + ".field_value field_value where field_value.id_record=rv.id and field_value.id_field_schema='5d92fa1ae54a5b94f8afba86') as endDate  "
        + "    FROM dataset_" + datasetLegazy + ".record_value rv), " + " " + " " + " "
        + "vCandidates as( select row_number() over(partition by a.bathingWaterIdentifier, a.season, a.periodType ORDER BY a.startDate, a.endDate) r, "
        + "             a.*  " + "from BW_LEGAZY a join vMultiple b "
        + "on a.bathingWaterIdentifier = b.bathingWaterIdentifier " + "and a.season =b.season "
        + "and a.periodType = b.periodType " + ") " + " " + "select a.* "
        + "from vCandidates a left join vCandidates n  "
        + "ON a.bathingWaterIdentifier = n.bathingWaterIdentifier AND a.season = n.season AND a.periodType = n.periodType AND (a.r + 1) = n.r "
        + "LEFT JOIN vCandidates p "
        + "ON a.bathingWaterIdentifier = p.bathingWaterIdentifier AND a.season = p.season AND a.periodType = p.periodType AND (a.r - 1) = p.r "
        + "where a.endDate >= n.startDate " + "OR  a.startDate <= p.endDate "
        + "ORDER BY a.bathingWaterIdentifier, a.season, a.periodType, a.r ";
    return validationService.tableValidationQueryNonReturnResult(ruleDU02B);
  }

  ////////////////////////////////////////////////////////////////////
  ////////////////////////////////// MonitoringResult //////////////////
  ////////////////////////////////////////////////////////////////////

  // # DU03 # The MonitoringResult file contains more than two records for the combination of
  // bathingWaterIdentifier and sampleDate. #

  /**
   * Rule DU 03.
   *
   * @param idSchemaBWIdent the id schema BW ident
   * @param idSchemaBWSampleDate the id schema BW sample date
   * @param datasetId the dataset id
   *
   * @return the boolean
   */
  public static Boolean ruleDU03(String idSchemaBWIdent, String idSchemaBWSampleDate,
      Long datasetId) {

    String DU03 = "select tabla1.bathingWaterIdentifier as campo1, tabla2.sampleDate as campo2 "
        + "from(select v.value as bathingWaterIdentifier,  v.id_record as record1 "
        + "FROM dataset_" + datasetId + ".field_value v " + "where v.id_field_schema = '"
        + idSchemaBWIdent + "') as tabla1 inner join("
        + "select v.value as sampleDate, v.id_record as record2 " + "FROM dataset_" + datasetId
        + ".field_value v " + "where v.id_field_schema = '" + idSchemaBWSampleDate + "') as tabla2 "
        + "on tabla1.record1 = tabla2.record2 " + "GROUP BY campo1,campo2 having count(*) > 1";

    return validationService.tableValidationQueryNonReturnResult(DU03);
  }


  public static Boolean ruleEmptyTable(Long datasetId, Long idTable) {

    String QUERY = "select count(*) from dataset_" + datasetId
        + ".record_value r where r.id_Table =" + idTable;

    return validationService.tableValidationQueryReturnResult(QUERY);
  }

}
