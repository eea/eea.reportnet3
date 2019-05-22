package org.eea.dataset.service;

import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;

/**
 * The Interface DataschemaService.
 */
public interface DataschemaService {

  /**
   * Creates the data schema.
   *
   * @param datasetName the dataset name
   */
  void createDataSchema(String datasetName);
  
  
  /**
   * Gets dataschema by id.
   *
   * @param dataschemaId the dataschema id
   * @return the data schema by id
   */
  DataSetSchemaVO getDataSchemaById(String dataschemaId);
  
  /**
   * Gets dataschema by id.
   *
   * @param idFlow the id flow
   * @return the data schema by id flow
   */
  DataSetSchemaVO getDataSchemaByIdFlow(Long idFlow);
  
  
}
