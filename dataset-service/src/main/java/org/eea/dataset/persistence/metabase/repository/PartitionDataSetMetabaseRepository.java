/**
 * 
 */
package org.eea.dataset.persistence.metabase.repository;

import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.springframework.data.repository.CrudRepository;
import com.google.common.base.Optional;

/**
 * The Interface PartitionDataSetMetabaseRepository.
 *
 * @author Mario Severa
 */
public interface PartitionDataSetMetabaseRepository
    extends CrudRepository<PartitionDataSetMetabase, Integer> {
  public Optional<PartitionDataSetMetabase> findOneByIdDataSetAndUsername(String idDataset,
      String username);

}
