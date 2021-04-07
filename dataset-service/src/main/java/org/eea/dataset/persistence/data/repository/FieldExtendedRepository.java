package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * The Interface DatasetExtendedRepository.
 */
public interface FieldExtendedRepository {



  /**
   * Find by field schema and value.
   *
   * @param fieldSchemaId the field schema id
   * @param idsList the ids list
   * @param datasetId the dataset id
   * @return the list
   */
  List<FieldValue> findByFieldSchemaAndValue(String fieldSchemaId, List<String> idsList,
      Long datasetId);



  /**
   * Find value.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @param datasetId the dataset id
   * @param idsList the ids list
   * @return the list
   */
  List<FieldVO> findValue(String fieldSchemaId1, String fieldSchemaId2, String datasetId,
      List<String> idsList);



  /**
   * Find by id field schema with tag ordered.
   *
   * @param idPk the id pk
   * @param labelSchemaId the label schema id
   * @param searchValue the search value
   * @param conditionalSchemaId the conditional schema id
   * @param conditionalValue the conditional value
   * @param dataTypePk the data type pk
   * @param resultsNumber the results number
   * @return the list
   * @throws DataIntegrityViolationException the data integrity violation exception
   */
  List<FieldVO> findByIdFieldSchemaWithTagOrdered(String idPk, String labelSchemaId,
      String searchValue, String conditionalSchemaId, String conditionalValue, DataType dataTypePk,
      Integer resultsNumber) throws DataIntegrityViolationException;
}
