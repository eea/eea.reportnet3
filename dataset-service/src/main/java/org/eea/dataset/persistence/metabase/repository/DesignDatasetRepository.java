/*
 * 
 */
package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import java.util.Optional;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * The Interface DesignDatasetRepository.
 */
public interface DesignDatasetRepository extends CrudRepository<DesignDataset, Long> {


  /**
   * Find by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<DesignDataset> findByDataflowId(Long dataflowId);

  /**
   * Find first by dataset schema.
   *
   * @param datasetSchema the dataset schema
   * @return the optional
   */
  Optional<DesignDataset> findFirstByDatasetSchema(String datasetSchema);

  /**
   * Findby dataset schema list.
   *
   * @param datasetSchemas the dataset schemas
   * @return the list
   */
  @Query(value = "select d from DesignDataset d where d.datasetSchema IN :datasetSchemas")
  List<DesignDataset> findbyDatasetSchemaList(@Param("datasetSchemas") List<String> datasetSchemas);

  /**
   * Exist dataset desing.
   *
   * @param datasetId the dataset id
   * @return true, if successful
   */
  @Query(value = "select count(d) > 0 from DesignDataset d where d.id IN :datasetId")
  boolean existDatasetDesing(Long datasetId);
}
