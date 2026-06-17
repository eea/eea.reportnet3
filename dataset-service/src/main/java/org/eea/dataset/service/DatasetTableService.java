package org.eea.dataset.service;

import org.eea.dataset.persistence.metabase.domain.DatasetTable;
import org.eea.interfaces.vo.dataset.DatasetTableVO;
import org.eea.interfaces.vo.dataset.schemas.DatasetEditingStatusVO;

import java.util.List;

public interface DatasetTableService {

    DatasetTable findEntryByDatasetIdAndPreparationCodeAndTableSchemaId(Long datasetId, String preparationCode, String tableSchemaId);

    void saveOrUpdateDatasetTableEntry(DatasetTable datasetTable);

    Boolean icebergTableIsCreated(Long datasetId, String preparationCode, String tableSchemaId);

    List<DatasetTable> getIcebergTablesByDatasetId(Long datasetId, String preparationCode);

    List<DatasetTableVO> getIcebergTablesForDataflow(Long dataflowId, Long providerId, Long datasetId, String preparationCode);

    String getDatasetEditingUsername(Long datasetId, String preparationCode);

    String getDatasetEditingUsernameForTable(Long datasetId, String preparationCode, String tableSchemaId);

    Boolean enableEditingForDatasetTableWithUser(Long datasetId, String username,Boolean isBigData, List<String> tableSchemaIds);

    Boolean disableEditingForDatasetTableWithUser(Long datasetId, String username);

    DatasetEditingStatusVO getEditingStatus(Long datasetId, String preparationCode, String username);

    boolean isAnyDatasetBeingEdited(List<Long> datasetIds);

    List<DatasetTableVO> getDatasetTablesByEditingUser(String username);

    void disableEditingForDatasetTable(Long datasetId);

    List<DatasetTableVO> getDatasetTablesWithExpiredEditingLocks();

    List<DatasetTableVO> getDatasetTablesByDataflowId(Long dataflowId);


    String getDatasetNonExpiredEditingUsername(Long datasetId);

}
