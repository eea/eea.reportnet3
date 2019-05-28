/**
 * 
 */
package org.eea.dataset.persistence.metabase.repository;

import org.eea.dataset.persistence.metabase.domain.TableCollection;
import org.eea.dataset.persistence.metabase.domain.TableHeadersCollection;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Mario Severa
 *
 */
public interface DataSetMetabaseTableCollection extends CrudRepository<TableCollection, Long> {


  Iterable<TableCollection> findAllByDataSetId(Long datasetId);


  Iterable<TableHeadersCollection> findTableHeadersCollectionsById(Long tableCollectionId);

}
