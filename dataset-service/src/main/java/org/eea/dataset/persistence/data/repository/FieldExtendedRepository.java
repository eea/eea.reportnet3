package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.interfaces.vo.dataset.FieldVO;


/**
 * The Interface FieldExtendedRepository.
 */
public interface FieldExtendedRepository {

  /**
   * Find by id field schema with tag ordered.
   *
   * @param idPk the id pk
   * @param labelSchemaId the label schema id
   * @param searchValue the search value
   * @param conditionalSchemaId the conditional schema id
   * @param conditionalValue the conditional value
   * @param resultsNumber the results number
   * @return the list
   */
  List<FieldVO> findByIdFieldSchemaWithTagOrdered(String idPk, String labelSchemaId,
      String searchValue, String conditionalSchemaId, String conditionalValue,
      Integer resultsNumber);
}
