package org.eea.dataset.persistence.metabase.repository;

import javax.transaction.Transactional;
import org.eea.dataset.persistence.metabase.domain.ForeignRelations;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


public interface ForeignRelationsRepository extends CrudRepository<ForeignRelations, Long> {


  @Query("Select f.idDatasetDestination.id From ForeignRelations f Where f.idDatasetOrigin.id=:idDatasetOrigin And f.idPk=:idPk")
  Long findDatasetDestinationByOriginAndPk(@Param("idDatasetOrigin") Long datasetIdOrigin,
      @Param("idPk") String idPk);
  
  @Transactional
  @Modifying
  @Query("Delete From ForeignRelations f Where f.idDatasetOrigin.id=:idDatasetOrigin And f.idDatasetDestination.id= :idDatasetDestination And f.idPk=:idPk")
  void deleteFKByOriginDestinationAndPk(@Param("idDatasetOrigin") Long datasetIdOrigin,@Param("idDatasetDestination") Long datasetIdDestination,
      @Param("idPk") String idPk);


}
