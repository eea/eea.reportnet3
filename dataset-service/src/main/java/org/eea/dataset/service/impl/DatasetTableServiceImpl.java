package org.eea.dataset.service.impl;

import lombok.SneakyThrows;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.mapper.DatasetTableMapper;
import org.eea.dataset.mapper.DatasetTableMapperImpl;
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
import org.eea.lock.redis.LockEnum;
import org.eea.lock.redis.RedisLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    @Autowired
    private RedisLockService redisLockService;

    @Value("${dataset.edit.lock.expirationInterval}")
    private Long expirationIntervalInHours;

    @Override
    public DatasetTable findEntryByDatasetIdAndPreparationCodeAndTableSchemaId(Long datasetId, String preparationCode, String tableSchemaId){
        final Optional<DatasetTable> optionalDatasetTable;
        optionalDatasetTable = datasetTableRepository.findByDatasetIdAndPreparationCodeAndTableSchemaId(
                    datasetId,
                    preparationCode,
                    tableSchemaId);
        return optionalDatasetTable.orElse(null);
    }

    @Override
    public void saveOrUpdateDatasetTableEntry(DatasetTable datasetTable){
        final DatasetTable existingEntry = findEntryByDatasetIdAndPreparationCodeAndTableSchemaId(
                datasetTable.getDatasetId(),
                datasetTable.getPreparationCode(),
                datasetTable.getTableSchemaId());
        if (existingEntry != null){
            //we need to update the existing entry
            existingEntry.setIsIcebergTableCreated(datasetTable.getIsIcebergTableCreated());
            existingEntry.setEditingUsername(datasetTable.getEditingUsername());
            existingEntry.setEditLockExpirationDate(datasetTable.getEditLockExpirationDate());
            datasetTableRepository.save(existingEntry);
        }
        else {
            //we need to save a new entry
            datasetTableRepository.save(datasetTable);
        }
    }

    @Override
    public Boolean icebergTableIsCreated(Long datasetId, String preparationCode, String tableSchemaId){
        final DatasetTable existingEntry = findEntryByDatasetIdAndPreparationCodeAndTableSchemaId(
                datasetId,
                preparationCode,
                tableSchemaId);
        return existingEntry != null && BooleanUtils.isTrue(existingEntry.getIsIcebergTableCreated());
    }

    @Override
    public List<DatasetTable> getIcebergTablesByDatasetId(Long datasetId, String preparationCode){
        final List<DatasetTable> datasetTables = datasetTableRepository.findByDatasetIdAndPreparationCodeAndIsIcebergTableCreated(datasetId, preparationCode, true);
        for (DatasetTable table: datasetTables) {
               final TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(table.getTableSchemaId(), table.getDatasetSchemaId());
                //need to check for null because custodian might have created iceberg tables and then delete them, so they do not exist in mongo anymore
                if (tableSchemaVO != null && tableSchemaVO.getNameTableSchema() != null){
                    table.setTableName(tableSchemaVO.getNameTableSchema());
                }
        }
        return datasetTables;
    }

    @Override
    public List<DatasetTableVO> getIcebergTablesForDataflow(Long dataflowId, Long providerId, Long datasetId, String preparationCode){
        List<DatasetTable> icebergTables = new ArrayList<>();
        if(datasetId != null){
            icebergTables = getIcebergTablesByDatasetId(datasetId, preparationCode);
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
                List<DatasetTable> tablesByDatasetId = getIcebergTablesByDatasetId(dataset.getId(), null);
                icebergTables.addAll(tablesByDatasetId);
            }
        }
        return datasetTableMapper.entityListToClass(icebergTables);
    }

    @Override
    public String getDatasetEditingUsername(Long datasetId, String preparationCode) {
        final List<String> editors;
        if (StringUtils.isBlank(preparationCode)) {
            editors = datasetTableRepository.findEditors(datasetId);
        }
        else {
            editors = datasetTableRepository.findEditors(datasetId, preparationCode);
        }
        return editors.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()           // alphabetical for deterministic output
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getDatasetEditingUsernameForTable(Long datasetId, String preparationCode, String tableSchemaId) {
        final List<String> editors;
        if (StringUtils.isBlank(preparationCode)) {
            editors = datasetTableRepository.findEditorsOfTable(datasetId, tableSchemaId);
        }
        else {
            editors = datasetTableRepository.findEditorsOfTable(datasetId, preparationCode, tableSchemaId);
        }
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
        int locked = datasetTableRepository.lockEditingForDatasetUser(datasetId, username, expirationIntervalInHours);

        // If lock == 0, another user already holds editing lock, fail
        return locked != 0;
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
    public DatasetEditingStatusVO getEditingStatus(Long datasetId, String preparationCode, String username) {
        final String editor = getDatasetEditingUsername(datasetId, preparationCode);

        final DatasetEditingStatusVO vo = new DatasetEditingStatusVO();
        vo.setDatasetId(datasetId);
        vo.setIsEditing(editor != null);
        vo.setEditor(editor);
        vo.setIsLockedForUser(editor != null && !editor.equals(username));

        final String lockKey = LockEnum.PARQUET_CONVERSION.getValue() + "_" + datasetId;
        final Map<String, String> activeLocks = redisLockService.listActiveLocks(lockKey);
        vo.setIsConverting(!activeLocks.isEmpty());

        return vo;
    }

    @Override
    public boolean isAnyDatasetBeingEdited(List<Long> datasetIds) {
        for (Long datasetId : datasetIds) {
            //TODO APBO Preparation code should be added here when edit functionality is implemented for prep sets.
            String editor = getDatasetEditingUsername(datasetId, null);
            if (editor != null ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<DatasetTableVO> getDatasetTablesByEditingUser(String username) {
        final List<DatasetTable> datasetTables = datasetTableRepository.findDatasetTablesByEditingUsername(username);

        if (datasetTables.isEmpty()) {
            return new ArrayList<>();
        }
        final DatasetTableMapperImpl mapper = new DatasetTableMapperImpl();
        return mapper.entityListToClass(datasetTables);
    }

    @Override
    @Transactional
    public void disableEditingForDatasetTable(Long datasetId) {
        // Clear ALL editing locks for this dataset
        datasetTableRepository.unlockEditingForDatasetUser(datasetId);
    }

    public Date getLockExpirationDate(Long datasetId) {
        return datasetTableRepository.getLockExpirationDateByDatasetId(datasetId);
    }

    @Override
    public List<DatasetTableVO> getDatasetTablesWithExpiredEditingLocks() {

        final List<DatasetTable> datasetTables = datasetTableRepository.findDatasetTableByEditLockExpirationDateBefore(new Date());

        if (datasetTables.isEmpty()) {
            return new ArrayList<>();
        }
        final DatasetTableMapperImpl mapper = new DatasetTableMapperImpl();
        return mapper.entityListToClass(datasetTables);
    }

    public String getDatasetNonExpiredEditingUsername(Long datasetId) {
        final List<String> editors = datasetTableRepository.findNonExpiredEditors(datasetId);
        return editors.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()           // alphabetical for deterministic output
                .findFirst()
                .orElse(null);
    }

    public List<DatasetTableVO> getDatasetTablesByDataflowId(Long dataflowId) {
        final List<DatasetTable> datasetTables = datasetTableRepository.findDatasetTableByDataflowId(dataflowId);

        if (datasetTables.isEmpty()) {
            return new ArrayList<>();
        }
        final DatasetTableMapperImpl mapper = new DatasetTableMapperImpl();
        return mapper.entityListToClass(datasetTables);
    }
}
