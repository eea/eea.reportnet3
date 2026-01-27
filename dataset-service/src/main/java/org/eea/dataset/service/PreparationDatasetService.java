package org.eea.dataset.service;

import java.util.List;
import org.eea.interfaces.vo.dataset.PreparationDatasetVO;

/**
 * Service for preparation datasets.
 */
public interface PreparationDatasetService {

    /**
     * List preparation datasets by dataflow id (optionally provider scoped).
     *
     * @param dataflowId the dataflow id
     * @param providerId the provider id (optional)
     * @return list of preparation datasets
     */
    List<PreparationDatasetVO> findPreparationDatasets(
            Long dataflowId,
            Long providerId);

    /**
     * Create a new preparation dataset.
     *
     * @param dataflowId the dataflow id
     * @param parentDatasetId the parent dataset id
     * @param preparationDatasetVO the preparation dataset data
     */
    void createPreparationDataset(
            Long dataflowId,
            Long parentDatasetId,
            PreparationDatasetVO preparationDatasetVO);

    /**
     * Delete a preparation dataset by id.
     *
     * @param preparationDatasetId the preparation dataset id
     */
    void deletePreparationDatasetById(Long preparationDatasetId);

    /**
     * Delete all preparation datasets by dataflow id and provider id.
     *
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     */
    void deleteAllPreparationDatasetsByProviderAndDataflowId(
            Long dataflowId,
            Long providerId);
}
