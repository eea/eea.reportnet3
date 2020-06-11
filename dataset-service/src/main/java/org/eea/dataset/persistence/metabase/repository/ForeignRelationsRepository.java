package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.eea.dataset.persistence.metabase.domain.ForeignRelations;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


/**
 * The Interface ForeignRelationsRepository.
 */
public interface ForeignRelationsRepository extends CrudRepository<ForeignRelations, Long> {


  /**
   * Find dataset destination by origin and pk.
   *
   * @param datasetIdOrigin the dataset id origin
   * @param idPk the id pk
   * @return the long
   */
  @Query("Select f.idDatasetDestination.id From ForeignRelations f Where f.idDatasetOrigin.id=:idDatasetOrigin And f.idPk=:idPk")
  List<Long> findDatasetDestinationByOriginAndPk(@Param("idDatasetOrigin") Long datasetIdOrigin,
      @Param("idPk") String idPk);

  /**
   * Delete FK by origin destination and pk.
   *
   * @param datasetIdOrigin the dataset id origin
   * @param datasetIdDestination the dataset id destination
   * @param idPk the id pk
   * @param idFkOrigin the id fk origin
   */
  @Transactional
  @Modifying
  @Query("Delete From ForeignRelations f Where f.idDatasetOrigin.id=:idDatasetOrigin And f.idDatasetDestination.id= :idDatasetDestination And f.idPk=:idPk And f.idFkOrigin=:idFkOrigin")
  void deleteFKByOriginDestinationAndPkAndIdFkOrigin(@Param("idDatasetOrigin") Long datasetIdOrigin,
      @Param("idDatasetDestination") Long datasetIdDestination, @Param("idPk") String idPk,
      @Param("idFkOrigin") String idFkOrigin);


  /**
   * Find first dataset destination by id dataset origin id and id pk and id fk origin.
   *
   * @param idDatasetOrigin the id dataset origin
   * @param idPk the id pk
   * @param idFkOrigin the id fk origin
   * @return the data set metabase
   */
  Optional<ForeignRelations> findFirstByIdDatasetOrigin_idAndIdPkAndIdFkOrigin(
      @Param("idDatasetOrigin") Long idDatasetOrigin, @Param("idPk") String idPk,
      @Param("idFkOrigin") String idFkOrigin);
}
