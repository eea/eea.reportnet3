package org.eea.dataset.service;

import org.eea.dataset.persistence.metabase.domain.DatasetTable;
import org.eea.interfaces.vo.dataset.DatasetTableVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaIdNameVO;

import java.util.List;

public interface DatasetTableService {

    DatasetTable findEntryByDatasetIdAndTableSchemaId(Long datasetId, String tableSchemaId);
    void saveOrUpdateDatasetTableEntry(DatasetTable datasetTable);

    Boolean icebergTableIsCreated(Long datasetId, String tableSchemaId);

    List<DatasetTable> getIcebergTablesByDatasetId(Long datasetId);

    List<DatasetTableVO> getIcebergTablesForDataflow(Long dataflowId, Long providerId, Long datasetId);

}
