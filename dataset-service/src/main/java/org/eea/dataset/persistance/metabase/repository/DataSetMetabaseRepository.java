/**
 * 
 */
package org.eea.dataset.persistance.metabase.repository;

import org.eea.dataset.persistance.metabase.domain.DataSetMetabase;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Mario Severa
 *
 */
public interface DataSetMetabaseRepository extends CrudRepository<DataSetMetabase, Integer> {

}
