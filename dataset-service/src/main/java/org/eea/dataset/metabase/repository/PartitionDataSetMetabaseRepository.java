/**
 * 
 */
package org.eea.dataset.metabase.repository;

import org.eea.dataset.metabase.domain.PartitionDataSetMetabase;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Mario Severa
 *
 */
public interface PartitionDataSetMetabaseRepository
    extends CrudRepository<PartitionDataSetMetabase, Integer> {

}
