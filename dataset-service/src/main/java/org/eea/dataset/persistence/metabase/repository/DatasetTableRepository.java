package org.eea.dataset.persistence.metabase.repository;

import org.eea.dataset.persistence.metabase.domain.DatasetTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DatasetTableRepository extends JpaRepository<DatasetTable, Long> {

    /**
     *
     * Retrieves an entry based on datasetId and tableSchemaId
     *
     * @param datasetId the dataset id
     * @param tableSchemaId the table schema id
     * @return optional entry
     */
    Optional<DatasetTable> findByDatasetIdAndTableSchemaId(Long datasetId, String tableSchemaId);

    /**
     *
     * Retrieves entries by datasetId
     *
     * @param datasetId the dataset id
     * @param isIcebergTableCreated
     * @return list of entries
     */
    List<DatasetTable> findByDatasetIdAndIsIcebergTableCreated(Long datasetId, Boolean isIcebergTableCreated);

}
