package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.interfaces.vo.dataset.FieldVO;

/**
 * The Interface DatasetExtendedRepository.
 */
public interface FieldExtendedRepository {



  /**
   * Query find by field schema and value.
   *
   * @param idFieldSchema the id field schema
   * @param idsList the ids list
   * @param datasetId the dataset id
   * @return the list
   */
  List<FieldValue> queryFindByFieldSchemaAndValue(String idFieldSchema, List<String> idsList,
      Long datasetId);


  /**
   * Query find value.
   *
   * @param idFieldSchema1 the id field schema 1
   * @param idFieldSchema2 the id field schema 2
   * @param datasetId the dataset id
   * @param idsList the ids list
   * @return the list
   */
  List<FieldVO> queryFindValue(String idFieldSchema1, String idFieldSchema2, String datasetId,
      List<String> idsList);



}
