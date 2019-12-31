package org.eea.validation.service;


import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.ErrorsValidationVO;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
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
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.springframework.data.domain.Pageable;

/**
 * The Class ValidationService.
 */
public interface ValidationService {


  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   * @param kieBase the kie session
   * @param pageable the pageable
   * @throws EEAException the EEA exception
   */
  void validateFields(@DatasetId Long datasetId, KieBase kieBase, Pageable pageable)
      throws EEAException;


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
   * Delete all validation.
   *
   * @param datasetId the dataset id
   */
  void deleteAllValidation(@DatasetId Long datasetId);


  /**
   * Validate data set.
   *
   * @param datasetId the dataset id
   * @param kieBase the kie base
   * @throws EEAException the EEA exception
   */
  void validateDataSet(@DatasetId Long datasetId, KieBase kieBase) throws EEAException;


  /**
   * Validate table.
   *
   * @param datasetId the dataset id
   * @param idTable the id table
   * @param kieBase the kie base
   * @throws EEAException the EEA exception
   */
  void validateTable(@DatasetId Long datasetId, Long idTable, KieBase kieBase) throws EEAException;


  /**
   * Validate record.
   *
   * @param datasetId the dataset id
   * @param kieBase the kie base
   * @param pageable the pageable
   * @throws EEAException the EEA exception
   */
  void validateRecord(@DatasetId Long datasetId, KieBase kieBase, Pageable pageable)
      throws EEAException;


  /**
   * Load rules knowledge base.
   *
   * @param datasetId the dataset id
   * @return the kie session
   * @throws EEAException the EEA exception
   */
  KieBase loadRulesKnowledgeBase(@DatasetId Long datasetId) throws EEAException;

  /**
   * Gets the record errors.
   *
   * @param datasetId the dataset id
   * @param idValidations the id validations
   * @return the record errors
   */
  Future<Map<Long, ErrorsValidationVO>> getRecordErrors(@DatasetId Long datasetId,
      List<Long> idValidations);

  /**
   * Gets the table errors.
   *
   * @param datasetId the dataset id
   * @param idValidations the id validations
   * @return the table errors
   */
  Future<Map<Long, ErrorsValidationVO>> getTableErrors(@DatasetId Long datasetId,
      List<Long> idValidations);

  /**
   * Gets the dataset errors.
   *
   * @param datasetId the dataset id
   * @param dataset the dataset
   * @param idValidations the id validations
   * @return the dataset errors
   */
  Future<Map<Long, ErrorsValidationVO>> getDatasetErrors(@DatasetId Long datasetId,
      DatasetValue dataset, List<Long> idValidations);

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


  /**
   * Gets the field errors.
   *
   * @param datasetId the dataset id
   * @param idValidations the id validations
   * @return the field errors
   */
  Future<Map<Long, ErrorsValidationVO>> getFieldErrors(@DatasetId Long datasetId,
      List<Long> idValidations);


  /**
   * Run field validations.
   *
   * @param field the field
   * @param kieSession the kie session
   * @return the list
   */
  List<FieldValidation> runFieldValidations(FieldValue field, KieSession kieSession);


  /**
   * Run record validations.
   *
   * @param record the record
   * @param kieSession the kie session
   * @return the list
   */
  List<RecordValidation> runRecordValidations(RecordValue record, KieSession kieSession);


  /**
   * Run table validations.
   *
   * @param table the table
   * @param kieSession the kie session
   * @return the list
   */
  List<TableValidation> runTableValidations(TableValue table, KieSession kieSession);



  /**
   * Force validations.
   *
   * @param datasetId the dataset id
   */
  void forceValidations(@DatasetId Long datasetId);

  /**
   * Count records dataset.
   *
   * @param datasetId the dataset id
   * @return the integer
   */
  Integer countRecordsDataset(@DatasetId Long datasetId);

  /**
   * Count fields dataset.
   *
   * @param datasetId the dataset id
   * @return the integer
   */
  Integer countFieldsDataset(@DatasetId Long datasetId);


  /**
   * Dataset validation DO 02 query.
   *
   * @param DO02 the do02
   * @return the boolean
   */
  Boolean datasetValidationDO02Query(String DO02);

  /**
   * Dataset validation DO 03 query.
   *
   * @param DO03 the do03
   * @return the boolean
   */
  Boolean datasetValidationDO03Query(String DO03);

  /**
   * Dataset validation DC 01 A query.
   *
   * @param DC01A the dc01a
   * @return the boolean
   */
  Boolean datasetValidationDC01AQuery(String DC01A);

  /**
   * Dataset validation DC 01 B query.
   *
   * @param DC01B the dc01b
   * @return the boolean
   */
  Boolean datasetValidationDC01BQuery(String DC01B);

  /**
   * Dataset validation DC 02 query.
   *
   * @param DC02 the dc02
   * @return the boolean
   */
  Boolean datasetValidationDC02Query(String DC02);

  /**
   * Dataset validation DC 03 query.
   *
   * @param DC03 the dc03
   * @return the boolean
   */
  Boolean datasetValidationDC03Query(String DC03);


  /**
   * Dataset validation DC 02 B query.
   *
   * @param DC03 the dc03
   * @return the boolean
   */
  Boolean datasetValidationDC02BQuery(String DC03);
  /// PART TABLE

  /**
   * Table validation DR 01 AB query.
   *
   * @param DR01A the dr01a
   * @param previous the previous
   * @return the boolean
   */
  Boolean tableValidationDR01ABQuery(String DR01A, Boolean previous);

  /**
   * Table validation query non return result.
   *
   * @param queryValidate the query validate
   * @return the boolean
   */
  Boolean tableValidationQueryNonReturnResult(String queryValidate);


  /**
   * Table validation query return result.
   *
   * @param queryValidate the query validate
   * @return the boolean
   */
  Boolean tableValidationQueryReturnResult(String queryValidate);


  /**
   * Table record R ids.
   *
   * @param queryValidate the query validate
   * @param MessageError the message error
   * @param typeError the type error
   * @param originName the origin name
   * @return the boolean
   */
  Boolean tableRecordRIds(String queryValidate, String MessageError, TypeErrorEnum typeError,
      String originName);
}
