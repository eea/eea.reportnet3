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
   * Find first by operation and parameter and value.
   *
   * @param operation the operation
   * @param parameter the parameter
   * @param value the value
   * @return the integration
   */
  @Query("SELECT i FROM Integration i JOIN i.internalParameters p WHERE i.operation=:operation AND p.parameter=:parameter AND p.value=:value")
  Integration findFirstByOperationAndParameterAndValue(
      @Param("operation") IntegrationOperationTypeEnum operation,
      @Param("parameter") String parameter, @Param("value") String value);

  /**
   * Find operation by id.
   *
   * @param id the id
   * @return the integration operation type enum
   */
  @Query("SELECT i.operation FROM Integration i WHERE i.id=:id")
  IntegrationOperationTypeEnum findOperationById(@Param("id") Long id);
}
