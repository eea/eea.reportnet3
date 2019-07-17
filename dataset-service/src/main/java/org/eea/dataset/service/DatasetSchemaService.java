package org.eea.dataset.service;

import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;

/**
 * The Interface DataschemaService.
 */
public interface DatasetSchemaService {


  /**
   * Creates the data schema.
   *
   * @param datasetId the dataset id
   */
  void createDataSchema(Long datasetId);


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
