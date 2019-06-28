package org.eea.validation.service;


import java.util.List;
import org.eea.exception.EEAException;
import org.eea.multitenancy.DatasetId;
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
   * @param kieSession the kie session
   * @throws EEAException the EEA exception
   */
  void validateFields(@DatasetId Long datasetId, KieSession kieSession) throws EEAException;


  /**
   * Run dataset validations.
   *
   * @param dataset the dataset
   * @param kieSession the kie session
   *
   * @return the list
   */
  List<DatasetValidation> runDatasetValidations(DatasetValue dataset, KieSession kieSession);

  /**
   * Run table validations.
   *
   * @param list the list
   * @param kieSession the kie session
   *
   * @return the list
   */
  List<TableValidation> runTableValidations(List<TableValue> list, KieSession kieSession);

  /**
   * Run record validations.
   *
   * @param recordsPaged the records paged
   * @param kieSession the kie session
   *
   * @return the list
   */
  List<RecordValidation> runRecordValidations(List<RecordValue> recordsPaged,
      KieSession kieSession);

  /**
   * Run field validations.
   *
   * @param fields the fields
   * @param kieSession the kie session
   *
   * @return the list
   */
  List<FieldValidation> runFieldValidations(List<FieldValue> fields, KieSession kieSession);

  /**
   * Delete all validation.
   *
   * @param datasetId the dataset id
   */
  void deleteAllValidation(@DatasetId Long datasetId);


  /**
   * Validate data set.
   *
   * @param datasetId the dataset id
   * @param kieSession the kie session
   * @throws EEAException the EEA exception
   */
  void validateDataSet(@DatasetId Long datasetId, KieSession kieSession) throws EEAException;


  /**
   * Validate table.
   *
   * @param datasetId the dataset id
   * @param kieSession the kie session
   * @throws EEAException the EEA exception
   */
  void validateTable(@DatasetId Long datasetId, KieSession kieSession) throws EEAException;


  /**
   * Validate record.
   *
   * @param datasetId the dataset id
   * @param kieSession the kie session
   * @throws EEAException the EEA exception
   */
  void validateRecord(@DatasetId Long datasetId, KieSession kieSession) throws EEAException;


  /**
   * Load rules knowledge base.
   *
   * @param datasetId the dataset id
   * @return the kie session
   * @throws EEAException the EEA exception
   */
  KieSession loadRulesKnowledgeBase(@DatasetId Long datasetId) throws EEAException;

}
