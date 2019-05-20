/**
 * 
 */
package org.eea.dataset.metabase.repository;

import org.eea.dataset.metabase.domain.PartitionDataSetMetabase;
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
