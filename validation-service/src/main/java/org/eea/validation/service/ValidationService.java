package org.eea.validation.service;


import java.util.List;
import java.util.Map;
import org.eea.validation.persistence.data.domain.DatasetValidation;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValidation;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.rules.DataFlowRule;

/**
 * The Class ValidationService.
 */
public interface ValidationService {


  /**
   * Gets the rules by data flow id.
   *
   * @param idDataflow the id dataflow
   * @return the rules by data flow id
   */
  List<Map<String, String>> getRulesByDataFlowId(Long idDataflow);

  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   */
  void validateDataSetData(Long datasetId);

  /**
   * Save rule.
   *
   * @param dataFlowRules the data flow rules
   */
  void saveRule(DataFlowRule dataFlowRules);

  /**
   * Run dataset validations.
   *
   * @param dataset the dataset
   * @return the dataset value
   */
  List<DatasetValidation> runDatasetValidations(DatasetValue dataset);

  List<TableValidation> runTableValidations(List<TableValue> list);

  List<RecordValidation> runRecordValidations(List<RecordValue> recordsPaged);

  List<FieldValidation> runFieldValidations(List<FieldValue> fields);

}
