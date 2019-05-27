/**
 * 
 */
package org.eea.dataset.persistence.metabase.repository;

import java.util.Optional;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.springframework.data.repository.CrudRepository;

/**
 * The Interface PartitionDataSetMetabaseRepository.
 *
 * @author Mario Severa
 */
public interface PartitionDataSetMetabaseRepository
    extends CrudRepository<PartitionDataSetMetabase, Long> {
  // @Query("SELECT p FROM PartitionDataSetMetabase p WHERE p.idDataSet=?1 AND p.username=?2")
  Optional<PartitionDataSetMetabase> findFirstByIdDataSet_idAndUsername(Long idDataset,
      String username);

}
