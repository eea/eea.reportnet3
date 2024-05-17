package org.eea.dataset.service;

import org.eea.dataset.persistence.metabase.domain.DatasetTable;

public interface DatasetTableService {

    DatasetTable findEntryByDatasetIdAndTableSchemaId(Long datasetId, String tableSchemaId);
    void saveOrUpdateDatasetTableEntry(DatasetTable datasetTable);

    Boolean icebergTableIsCreated(Long datasetId, String tableSchemaId);

}
