package org.eea.dataset.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eea.dataset.persistence.data.domain.RecordValue;
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
  void createDataSchema(Long datasetId, Long dataflowId);


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



  CompletableFuture pruebaAsync(List<RecordValue> taka);


  void pruebaTransactional(List<List<RecordValue>> taka);

}
