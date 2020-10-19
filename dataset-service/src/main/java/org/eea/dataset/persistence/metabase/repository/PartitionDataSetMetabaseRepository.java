package org.eea.dataset.persistence.metabase.repository;

import java.util.Optional;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * The Interface PartitionDataSetMetabaseRepository.
 */
public interface PartitionDataSetMetabaseRepository
    extends CrudRepository<PartitionDataSetMetabase, Long> {

  /**
   * Find first by id data set id and username.
   *
   * @param idDataset the id dataset
   * @param username the username
   * @return the optional
   */
  // @Query("SELECT p FROM PartitionDataSetMetabase p WHERE p.idDataSet=?1 AND p.username=?2")
  Optional<PartitionDataSetMetabase> findFirstByIdDataSet_idAndUsername(Long idDataset,
      String username);

  /**
   * Find first by id data set id.
   *
   * @param idDataset the id dataset
   * @return the optional
   */
  Optional<PartitionDataSetMetabase> findFirstByIdDataSet_id(Long idDataset);

  /**
   * Gets the id.
   *
   * @param datasetId the dataset id
   * @param user the user
   * @return the id
   */
  @Query("SELECT p.id FROM PartitionDataSetMetabase p JOIN p.idDataSet d WHERE d.id = :datasetId AND p.username = :user")
  Long getId(@Param("datasetId") Long datasetId, @Param("user") String user);
}
