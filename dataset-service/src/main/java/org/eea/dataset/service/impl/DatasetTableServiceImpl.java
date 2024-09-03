package org.eea.dataset.service.impl;

import org.apache.commons.lang3.BooleanUtils;
import org.eea.dataset.mapper.DatasetTableMapper;
import org.eea.dataset.persistence.metabase.domain.DatasetTable;
import org.eea.dataset.persistence.metabase.repository.DatasetTableRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetTableService;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DatasetTableVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class DatasetTableServiceImpl implements DatasetTableService {

    @Autowired
    private DatasetTableRepository datasetTableRepository;

    @Autowired
    private DatasetMetabaseService datasetMetabaseService;

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


}
