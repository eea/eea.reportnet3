package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import org.eea.dataset.persistence.metabase.domain.PreparationDataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * The Interface PreparationDatasetRepository.
 */
public interface PreparationDatasetRepository
        extends JpaRepository<PreparationDataset, Long> {

    /**
     * Find all preparation datasets by dataflow id.
     *
     * @param dataflowId the dataflow id
     * @return the list of preparation datasets
     */
    @Query("SELECT p FROM PreparationDataset p "
            + "WHERE p.dataflowId = :dataflowId")
    List<PreparationDataset> findByDataflowId(
            @Param("dataflowId") Long dataflowId);

    /**
     * Find all preparation datasets by dataflow id and provider id.
     *
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @return the list of preparation datasets
     */
    @Query("SELECT p FROM PreparationDataset p "
            + "WHERE p.dataflowId = :dataflowId "
            + "AND p.providerId = :providerId")
    List<PreparationDataset> findByDataflowIdAndProviderId(
            @Param("dataflowId") Long dataflowId,
            @Param("providerId") Long providerId);

    /**
     * Check if a preparation dataset exists by dataflow, provider and code.
     *
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param code the preparation code
     * @return true if exists
     */
    boolean existsByDataflowIdAndProviderIdAndCode(
            Long dataflowId,
            Long providerId,
            String code);
}
