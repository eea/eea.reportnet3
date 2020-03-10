package org.eea.dataset.persistence.metabase.repository;

import org.eea.dataset.persistence.metabase.domain.ForeignRelations;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


public interface ForeignRelationsRepository extends CrudRepository<ForeignRelations, Long> {


  @Query("Select f.idDatasetDestination From ForeignRelations f Where f.idDatasetOrigin=:idDatasetOrigin And f.idPk=:idPk")
  Long findDatasetDestinationByOriginAndPk(@Param("idDatasetOrigin") Long datasetIdOrigin,
      @Param("idPk") String idPk);


}
