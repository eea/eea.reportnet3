package org.eea.validation.service;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.ErrorsValidationVO;
import org.eea.interfaces.vo.dataset.GroupValidationVO;
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
import org.eea.validation.persistence.schemas.rule.Rule;
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
   * @param onlyEmptyFields the only empty fields
   * @throws EEAException the EEA exception
   */
  void validateFields(@DatasetId Long datasetId, KieBase kieBase, Pageable pageable,
      boolean onlyEmptyFields) throws EEAException;


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
   * @param sqlRule the sql rule
   * @throws EEAException the EEA exception
   */
  void validateTable(@DatasetId Long datasetId, Long idTable, KieBase kieBase, String sqlRule)
      throws EEAException;


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
   * @param rule the rule
   * @return the kie session
   * @throws EEAException the EEA exception
   */
  KieBase loadRulesKnowledgeBase(@DatasetId Long datasetId, Rule rule) throws EEAException;

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
   * Count empty fields dataset.
   *
   * @param datasetId the dataset id
   * @return the integer
   */
  Integer countEmptyFieldsDataset(@DatasetId Long datasetId);

  /**
   * Exports validation data file.
   *
   * @param datasetId the dataset id
   * @return the byte[]
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void exportValidationFile(@DatasetId Long datasetId) throws EEAException, IOException;


  /**
   * Download validation exported file.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @return the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  File downloadExportedFile(Long datasetId, String fileName) throws IOException;


  /**
   * Gets the rule message.
   *
   * @param dataset the dataset
   * @param errors the errors
   * @return the rule message
   */
  void getRuleMessage(DatasetValue dataset, List<GroupValidationVO> errors);

}
