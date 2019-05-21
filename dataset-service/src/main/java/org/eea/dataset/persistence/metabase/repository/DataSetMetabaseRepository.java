/**
 * 
 */
package org.eea.dataset.persistence.metabase.repository;

import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Mario Severa
 *
 */
public interface DataSetMetabaseRepository extends CrudRepository<DataSetMetabase, Integer> {

}
