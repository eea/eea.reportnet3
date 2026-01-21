package org.eea.dataset.service.impl;

import lombok.SneakyThrows;
import org.apache.commons.lang3.BooleanUtils;
import org.eea.dataset.mapper.DatasetTableMapper;
import org.eea.dataset.persistence.metabase.domain.DatasetTable;
import org.eea.dataset.persistence.metabase.repository.DatasetTableRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetTableService;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DatasetTableVO;
import org.eea.interfaces.vo.dataset.schemas.DatasetEditingStatusVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaIdNameVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DatasetTableServiceImpl implements DatasetTableService {

    @Autowired
    private DatasetTableRepository datasetTableRepository;

    @Autowired
    private DatasetMetabaseService datasetMetabaseService;

    @Lazy
    @Autowired
    private DatasetSchemaService datasetSchemaService;

    @Autowired
    private DatasetTableMapper datasetTableMapper;

    @Override
    public DatasetTable findEntryByDatasetIdAndTableSchemaId(Long datasetId, String tableSchemaId){
        Optional<DatasetTable> optionalDatasetTable = datasetTableRepository.findByDatasetIdAndTableSchemaId(datasetId, tableSchemaId);
        if(optionalDatasetTable.isPresent()){
            return optionalDatasetTable.get();
        }
        return null;
    }

    @Override
    public void saveOrUpdateDatasetTableEntry(DatasetTable datasetTable){
        DatasetTable existingEntry = findEntryByDatasetIdAndTableSchemaId(datasetTable.getDatasetId(), datasetTable.getTableSchemaId());
        if(existingEntry != null){
            //we need to update the existing entry
            existingEntry.setIsIcebergTableCreated(datasetTable.getIsIcebergTableCreated());
            existingEntry.setEditingUsername(datasetTable.getEditingUsername());
            datasetTableRepository.save(existingEntry);
        }
        else{
            //we need to save a new entry
            datasetTableRepository.save(datasetTable);
        }
    }

    @Override
    public Boolean icebergTableIsCreated(Long datasetId, String tableSchemaId){
        DatasetTable existingEntry = findEntryByDatasetIdAndTableSchemaId(datasetId, tableSchemaId);
        if(existingEntry != null && BooleanUtils.isTrue(existingEntry.getIsIcebergTableCreated())){
            return true;
        }
        return false;
    }

    @Override
    public List<DatasetTable> getIcebergTablesByDatasetId(Long datasetId){
        List<DatasetTable> datasetTables = datasetTableRepository.findByDatasetIdAndIsIcebergTableCreated(datasetId, true);
        for(DatasetTable table: datasetTables){
            TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(table.getTableSchemaId(), table.getDatasetSchemaId());
            //need to check for null because custodian might have created iceberg tables and then delete them, so they do not exist in mongo anymore
            if(tableSchemaVO != null && tableSchemaVO.getNameTableSchema() != null){
                table.setTableName(tableSchemaVO.getNameTableSchema());
            }
        }
        return datasetTables;
    }

    @Override
    public List<DatasetTableVO> getIcebergTablesForDataflow(Long dataflowId, Long providerId, Long datasetId){
        List<DatasetTable> icebergTables = new ArrayList<>();
        if(datasetId != null){
            icebergTables = getIcebergTablesByDatasetId(datasetId);
        }
        else{
            List<DataSetMetabaseVO> dataSetMetabaseList;
            if(providerId != null){
                dataSetMetabaseList = datasetMetabaseService.getDatasetsByDataflowIdAndProviderId(dataflowId, providerId);
            }
            else{
                dataSetMetabaseList = datasetMetabaseService.findDataSetByDataflowIds(Collections.singletonList(dataflowId));
            }
            for(DataSetMetabaseVO dataset: dataSetMetabaseList){
                List<DatasetTable> tablesByDatasetId = getIcebergTablesByDatasetId(dataset.getId());
                icebergTables.addAll(tablesByDatasetId);
            }
        }
        return datasetTableMapper.entityListToClass(icebergTables);
    }

    @Override
    public String getDatasetEditingUsername(Long datasetId) {
        List<String> editors = datasetTableRepository.findEditors(datasetId);
        return editors.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()           // alphabetical for deterministic output
                .findFirst()
                .orElse(null);
    }

    @SneakyThrows
    @Override
    @Transactional
    public Boolean enableEditingForDatasetTableWithUser(
            Long datasetId,
            String username,
            Boolean isBigdata,
            List<String> tableSchemaIds) {

        String datasetSchemaId = datasetMetabaseService
                .findDatasetMetabase(datasetId)
                .getDatasetSchema();

        // Only Citus (non-bigdata) needs to insert missing entries
        if (!isBigdata) {

            // Load all table schema IDs if none provided from endpoint call
            if (tableSchemaIds == null || tableSchemaIds.isEmpty()) {
                List<TableSchemaIdNameVO> allTables;
                allTables = datasetSchemaService.getTableSchemasIds(datasetId);

                tableSchemaIds = allTables.stream()
                        .map(TableSchemaIdNameVO::getIdTableSchema)
                        .collect(Collectors.toList());
            }

            createMissingDatasetTableEntries(datasetId, datasetSchemaId, tableSchemaIds);
        }

        // Acquire lock
        int locked = datasetTableRepository.lockEditingForDatasetUser(datasetId, username);

        // If lock == 0, another user already holds editing lock, fail
        if (locked == 0) {
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public Boolean disableEditingForDatasetTableWithUser(Long datasetId, String username) {

        List<String> editors = datasetTableRepository.findEditors(datasetId);

        // If someone else is editing → block
        if (!editors.isEmpty() && !editors.contains(username)) {
            return false; // failure
        }

        // Clear ALL editing locks for this dataset
        datasetTableRepository.unlockEditingForDatasetUser(datasetId);

        return true; // success
    }

    private void createMissingDatasetTableEntries(Long datasetId, String datasetSchemaId, List<String> tableSchemaIds) {
        String arrayLiteral = "{" + String.join(",", tableSchemaIds) + "}";
        datasetTableRepository.insertMissingDatasetTableEntries(
                datasetId, datasetSchemaId, arrayLiteral);
    }

    @Override
    public DatasetEditingStatusVO getEditingStatus(Long datasetId, String username) {
        String editor = getDatasetEditingUsername(datasetId);

        DatasetEditingStatusVO vo = new DatasetEditingStatusVO();
        vo.setDatasetId(datasetId);
        vo.setIsEditing(editor != null);
        vo.setEditor(editor);
        vo.setIsLockedForUser(editor != null && !editor.equals(username));

        return vo;
    }

    @Override
    public boolean isAnyDatasetBeingEdited(List<Long> datasetIds) {
        for (Long datasetId : datasetIds) {
            String editor = getDatasetEditingUsername(datasetId);
            if (editor != null ) {
                return true;
            }
        }
        return false;
    }


}
