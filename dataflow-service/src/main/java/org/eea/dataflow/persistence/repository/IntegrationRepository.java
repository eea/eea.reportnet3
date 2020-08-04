package org.eea.dataflow.persistence.repository;

import java.util.List;
import org.eea.dataflow.persistence.domain.Integration;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * The Interface IntegrationRepository.
 */
public interface IntegrationRepository extends CrudRepository<Integration, Long> {

  /**
   * Find by internal operation parameter.
   *
   * @param internalParameter the internal parameter
   * @param paramValue the param value
   * @return the list
   */
  @Query("SELECT i FROM Integration i JOIN i.internalParameters p WHERE p.parameter= :param AND p.value= :paramValue")
  List<Integration> findByInternalOperationParameter(@Param("param") String internalParameter,
      @Param("paramValue") String paramValue);

  /**
   * Find first by dataset id and operation.
   *
   * @param datasetId the dataset id
   * @param operation the operation
   * @return the integration
   */
  @Query("SELECT i FROM Integration i JOIN i.internalParameters p WHERE i.operation=:operation AND p.parameter='datasetId' AND p.value=:datasetId")
  Integration findFirstByDatasetIdAndOperation(@Param("datasetId") String datasetId,
      @Param("operation") IntegrationOperationTypeEnum operation);
}
