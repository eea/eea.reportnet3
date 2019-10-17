package org.eea.dataset.service;

import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;

/**
 * The Interface DataschemaService.
 */
public interface DatasetSchemaService {


  /**
   * Creates the data schema.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   */
  void createDataSchema(Long datasetId, Long dataflowId);

  ObjectId createEmptyDataSetSchema(Long idDataFlow, String nameDataSetSchema);


  /**
   * Gets dataschema by id.
   *
   * @param dataschemaId the dataschema id
   *
   * @return the data schema by id
   */
  DataSetSchemaVO getDataSchemaById(String dataschemaId);

  /**
   * Gets dataschema by id. If addRules is true, the whole schema including rules will be retrieved
   * Otherwise only the schema (table, records, fields and dataset) will be retrieved
   *
   * @param idFlow the id flow
   * @param addRules the add rules
   *
   * @return the data schema by id flow
   */
  DataSetSchemaVO getDataSchemaByIdFlow(Long idFlow, Boolean addRules);


}
