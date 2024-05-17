package org.eea.dataset.service.impl;

import org.apache.commons.lang3.BooleanUtils;
import org.eea.dataset.persistence.metabase.domain.DatasetTable;
import org.eea.dataset.persistence.metabase.repository.DatasetTableRepository;
import org.eea.dataset.service.DatasetTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DatasetTableServiceImpl implements DatasetTableService {

    @Autowired
    private DatasetTableRepository datasetTableRepository;

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
}
