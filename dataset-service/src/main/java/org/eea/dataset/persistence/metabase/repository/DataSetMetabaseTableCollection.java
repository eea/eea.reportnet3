package org.eea.dataset.persistence.metabase.repository;

import org.eea.dataset.persistence.metabase.domain.TableCollection;
import org.eea.dataset.persistence.metabase.domain.TableHeadersCollection;
import org.springframework.data.repository.CrudRepository;

/**
 * The Interface DataSetMetabaseTableCollection.
 *
 * @author Mario Severa
 */
public interface DataSetMetabaseTableCollection extends CrudRepository<TableCollection, Long> {


  /**
   * Find all by data set id.
   *
   * @param datasetId the dataset id
   * @return the iterable
   */
  Iterable<TableCollection> findAllByDataSetId(Long datasetId);


  /**
   * Find table headers collections by id.
   *
   * @param tableCollectionId the table collection id
   * @return the iterable
   */
  Iterable<TableHeadersCollection> findTableHeadersCollectionsById(Long tableCollectionId);

}
