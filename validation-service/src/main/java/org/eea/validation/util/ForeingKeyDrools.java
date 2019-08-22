package org.eea.validation.util;

import org.apache.commons.lang.StringUtils;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.repository.FieldRepositoryImpl;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.clasesToQueryDrools.DataToQuery1;
import org.eea.validation.util.clasesToQueryDrools.DataToQuery2;
import org.eea.validation.util.clasesToQueryDrools.DataToQuery3;
import org.eea.validation.util.clasesToQueryDrools.DataToQuery4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The Class ValidationRuleDrools.
 */
@Component("foreingKeyDrools")
public class ForeingKeyDrools {


  /** The field repository impl. */
  private static FieldRepositoryImpl fieldRepositoryImpl;

  /**
   * Sets the dataset repository.
   *
   * @param fieldRepositoryImpl the new dataset repository
   */
  @Autowired
  private void setDatasetRepository(FieldRepositoryImpl fieldRepositoryImpl) {
    ForeingKeyDrools.fieldRepositoryImpl = fieldRepositoryImpl;
  }



  /** The validation service. */
  @Qualifier("proxyValidationService")
  private static ValidationService validationService;


  /**
   * Sets the dataset repository.
   *
   * @param validationService the new dataset repository
   */
  @Autowired
  private void setDatasetRepository(ValidationService validationService) {
    ForeingKeyDrools.validationService = validationService;
  }

  /**
   * Query get all field value.
   *
   * @param value the value
   * @param idFieldSchema the id field schema
   * @param record the record
   * @param recordCoordinate the record coordinate
   * @param columnCoordinate the column coordinate
   * @param idFieldSchemaReference the id field schema reference
   * @param idDatasetReference the id dataset reference
   * @param recordCoordinateReference the record coordinate reference
   * @param columnCoordinateReference the column coordinate reference
   * @return the boolean
   */
  public static Boolean queryGetAllFieldValue(String value, String idFieldSchema,
      RecordValue record, Long recordCoordinate, Long columnCoordinate,
      String idFieldSchemaReference, Long idDatasetReference, Long recordCoordinateReference,
      Long columnCoordinateReference) {

    Long idDataset = record.getTableValue().getDatasetId().getId();

    FieldValue fieldReference = new FieldValue();
    fieldReference.setColumnCoordinate(columnCoordinateReference);
    fieldReference.setRecordCoordinate(recordCoordinateReference);
    fieldReference.setIdFieldSchema(idFieldSchemaReference);

    if (!StringUtils.isBlank(idFieldSchema)) {
      return validationService.findReferenceDrools(value, idDataset, idFieldSchema,
          recordCoordinate, columnCoordinate, idDatasetReference, fieldReference);
    } else {
      return false;
    }
  }

  /**
   * Checks if is query data WDF protect.
   *
   * @param value the value
   * @return the boolean
   */
  public static Boolean isQueryDataWDFProtect(String value) {
    DataToQuery1 part1 = new DataToQuery1();
    DataToQuery2 part2 = new DataToQuery2();
    DataToQuery3 part3 = new DataToQuery3();
    DataToQuery4 part4 = new DataToQuery4();
    if (part1.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || part2.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || part3.getListData().stream().anyMatch(datoString -> datoString.equalsIgnoreCase(value))
        || part4.getListData().stream()
            .anyMatch(datoString -> datoString.equalsIgnoreCase(value))) {
      return true;
    }
    return false;
  }


  public static Boolean isInSameRecord(String value, RecordValue record, Integer position,
      String valueToHave) {

    if (record.getFields().get(position).getValue().equalsIgnoreCase(valueToHave)) {
      return true;
    }
    return false;
  }
}
