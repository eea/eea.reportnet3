package org.eea.dataset.service;

import org.eea.dataset.persistence.metabase.domain.DatasetTable;
import org.eea.interfaces.vo.dataset.DatasetTableVO;
import org.eea.interfaces.vo.dataset.schemas.DatasetEditingStatusVO;

import java.util.List;

public interface DatasetTableService {

    DatasetTable findEntryByDatasetIdAndTableSchemaId(Long datasetId, String tableSchemaId);
    void saveOrUpdateDatasetTableEntry(DatasetTable datasetTable);

    Boolean icebergTableIsCreated(Long datasetId, String tableSchemaId);

    List<DatasetTable> getIcebergTablesByDatasetId(Long datasetId);

    List<DatasetTableVO> getIcebergTablesForDataflow(Long dataflowId, Long providerId, Long datasetId);

    String getDatasetEditingUsername(Long datasetId);

    String getDatasetNonExpiredEditingUsername(Long datasetId);

    String getDatasetEditingUsernameForTable(Long datasetId, String tableSchemaId);

    Boolean enableEditingForDatasetTableWithUser(Long datasetId, String username,Boolean isBigData, List<String> tableSchemaIds);

    Boolean disableEditingForDatasetTableWithUser(Long datasetId, String username);

    Boolean disableEditingForDatasetTable(Long datasetId);

    DatasetEditingStatusVO getEditingStatus(Long datasetId, String username);

    boolean isAnyDatasetBeingEdited(List<Long> datasetIds);

    List<DatasetTableVO> getDatasetTablesWithExpiredEditingLocks();
}
