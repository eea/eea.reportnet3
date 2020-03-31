package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.springframework.data.repository.CrudRepository;

/**
 * The Interface DataSetMetabaseRepository.
 *
 *
 */
public interface ReportingDatasetRepository extends CrudRepository<ReportingDataset, Long> {

  /**
   * Find by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<ReportingDataset> findByDataflowId(Long dataflowId);

  /**
   * Find by dataset schema.
   *
   * @param schemaId the schema id
   * @return the list
   */
  List<ReportingDataset> findByDatasetSchema(String schemaId);

}
