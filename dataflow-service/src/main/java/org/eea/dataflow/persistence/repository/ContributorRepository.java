package org.eea.dataflow.persistence.repository;

import javax.transaction.Transactional;
import org.eea.dataflow.persistence.domain.Contributor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * The interface Contributor repository.
 */
public interface ContributorRepository extends PagingAndSortingRepository<Contributor, Long> {


  /**
   * Removes the contributor from dataset.
   *
   * @param idDataflow the id dataflow
   * @param idContributor the id contributor
   */
  @Transactional
  @Modifying
  @Query(nativeQuery = true,
      value = "DELETE FROM contributor WHERE user_id=:idUser AND dataflowId=:idDataflow")
  void removeContributorFromDataset(@Param("idDataflow") Long idDataflow,
      @Param("idUser") String idContributor);


}
