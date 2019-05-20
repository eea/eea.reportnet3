/**
 * 
 */
package org.eea.dataset.persistance.metabase.repository;

import org.eea.dataset.persistance.metabase.domain.PartitionDataSetMetabase;
import org.springframework.data.repository.CrudRepository;

/**
 * The Interface PartitionDataSetMetabaseRepository.
 *
 * @author Mario Severa
 */
public interface PartitionDataSetMetabaseRepository
    extends CrudRepository<PartitionDataSetMetabase, Integer> {

}
