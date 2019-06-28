package org.eea.validation.service;


import java.util.List;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.ErrorsValidationVO;
import org.eea.multitenancy.DatasetId;
import org.eea.validation.persistence.data.domain.DatasetValidation;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValidation;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.kie.api.runtime.KieSession;

/**
 * The Class ValidationService.
 */
public interface ValidationService {


  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   *
   * @throws EEAException the EEA exception
   */
  void validateDataSetData(@DatasetId Long datasetId) throws EEAException;


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
   * Gets the field errors.
   *
   * @param datasetId the dataset id
   * @param idValidations the id validations
   * @return the field errors
   */
  List<ErrorsValidationVO> getFieldErrors(@DatasetId Long datasetId, List<Long> idValidations);

  /**
   * Gets the record errors.
   *
   * @param datasetId the dataset id
   * @param idValidations the id validations
   * @return the record errors
   */
  List<ErrorsValidationVO> getRecordErrors(@DatasetId Long datasetId, List<Long> idValidations);

  /**
   * Gets the table errors.
   *
   * @param datasetId the dataset id
   * @param idValidations the id validations
   * @return the table errors
   */
  List<ErrorsValidationVO> getTableErrors(@DatasetId Long datasetId, List<Long> idValidations);

  /**
   * Gets the dataset errors.
   *
   * @param datasetId the dataset id
   * @param idValidations the id validations
   * @return the dataset errors
   */
  List<ErrorsValidationVO> getDatasetErrors(@DatasetId Long datasetId, DatasetValue dataset,
      List<Long> idValidations);

  /**
   * Gets the datase valuetby id.
   *
   * @param datasetId the dataset id
   * @return the datase valuetby id
   * @throws EEAException the EEA exception
   */
  DatasetValue getDatasetValuebyId(@DatasetId Long datasetId) throws EEAException;

  /**
   * Gets the find by id data set schema.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @return the find by id data set schema
   * @throws EEAException the EEA exception
   */
  DataSetSchema getfindByIdDataSetSchema(@DatasetId Long datasetId, ObjectId datasetSchemaId)
      throws EEAException;

}
