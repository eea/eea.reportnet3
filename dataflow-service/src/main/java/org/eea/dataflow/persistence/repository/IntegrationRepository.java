package org.eea.dataflow.persistence.repository;

import java.util.List;
import org.eea.dataflow.persistence.domain.Integration;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


public interface IntegrationRepository extends CrudRepository<Integration, Long> {


  @Query("SELECT i FROM Integration i JOIN i.internalParameters p WHERE p.parameter= :param AND p.value= :paramValue")
  List<Integration> findByInternalOperationParameter(@Param("param") String internalParameter,
      @Param("paramValue") String paramValue);

}
