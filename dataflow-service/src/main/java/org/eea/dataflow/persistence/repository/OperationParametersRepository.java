package org.eea.dataflow.persistence.repository;

import org.eea.dataflow.persistence.domain.Integration;
import org.eea.dataflow.persistence.domain.OperationParameters;
import org.springframework.data.repository.CrudRepository;


/**
 * The Interface OperationParametersRepository.
 */
public interface OperationParametersRepository extends CrudRepository<OperationParameters, Long> {

  /**
   * Delete by integration.
   *
   * @param integration the integration
   */
  void deleteByIntegration(Integration integration);

}
