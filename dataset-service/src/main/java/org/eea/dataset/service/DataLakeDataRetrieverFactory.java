package org.eea.dataset.service;

import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataLakeDataRetrieverFactory {

    private final List<DataLakeDataRetriever> dataLakeDataRetriever;
    private final DatasetMetabaseService datasetMetabaseService;

    public DataLakeDataRetrieverFactory(List<DataLakeDataRetriever> dataLakeDataRetriever, DatasetMetabaseService datasetMetabaseService) {
        this.dataLakeDataRetriever = dataLakeDataRetriever;
        this.datasetMetabaseService = datasetMetabaseService;
    }

    public DataLakeDataRetriever getRetriever(Long datasetId) {
        DataSetMetabaseVO dataset = datasetMetabaseService.findDatasetMetabase(datasetId);
        return dataLakeDataRetriever.stream().filter(r -> r.isApplicable(dataset.getDatasetTypeEnum().getValue())).findFirst()
                .orElseThrow(()->new IllegalArgumentException("Could not find applicable type for " + dataset.getDatasetTypeEnum()));
    }
}






















