package org.eea.validation.service;


import java.util.List;
import org.eea.validation.multitenancy.DatasetId;
import org.eea.validation.persistence.data.domain.DatasetValidation;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValidation;
import org.eea.validation.persistence.data.domain.TableValue;
import org.kie.api.runtime.KieSession;

/**
 * The Class ValidationService.
 */
public interface ValidationService {


  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   */
  void validateDataSetData(@DatasetId Long datasetId);

  /**
   * Run dataset validations.
   *
   * @param dataset the dataset
   * @return the dataset value
   */
  List<DatasetValidation> runDatasetValidations(DatasetValue dataset, KieSession kieSession);

  /**
   * Run table validations.
   *
   * @param list the list
   * @return the list
   */
  List<TableValidation> runTableValidations(List<TableValue> list, KieSession kieSession);

  /**
   * Run record validations.
   *
   * @param recordsPaged the records paged
   * @return the list
   */
  List<RecordValidation> runRecordValidations(List<RecordValue> recordsPaged,
      KieSession kieSession);

  /**
   * Run field validations.
   *
   * @param fields the fields
   * @return the list
   */
  List<FieldValidation> runFieldValidations(List<FieldValue> fields, KieSession kieSession);

  /**
   * Delete all validation.
   *
   * @param datasetId the dataset id
   */
  void deleteAllValidation(@DatasetId Long datasetId);

}
