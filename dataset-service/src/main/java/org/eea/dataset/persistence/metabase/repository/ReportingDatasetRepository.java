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


  /**
   * Find first by dataflow id and data provider id order by id asc.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the reporting dataset
   */
  ReportingDataset findFirstByDataflowIdAndDataProviderIdOrderByIdAsc(Long dataflowId,
      Long dataProviderId);

  /**
   * Find by dataflow id and data provider id.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the list
   */
  List<ReportingDataset> findByDataflowIdAndDataProviderId(Long dataflowId, Long dataProviderId);

  /**
   * Find by dataflow id in.
   *
   * @param dataflowIdList the dataflow id list
   * @return the list
   */
  List<ReportingDataset> findByDataflowIdIn(List<Long> dataflowIdList);

}
