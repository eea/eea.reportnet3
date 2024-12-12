package org.eea.dataset.service.impl;

import org.eea.dataset.persistence.metabase.domain.Statistics;
import org.eea.dataset.persistence.metabase.repository.StatisticsRepository;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.StatisticsService;
import org.eea.interfaces.vo.dataset.ImportStatisticsVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaIdNameVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.eea.utils.LiteralConstants.*;

@Service("statisticsService")
public class StatisticsServiceImpl implements StatisticsService {


    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    /** The statistics repository. */
    @Autowired
    private StatisticsRepository statisticsRepository;

    /**
     * Saves statistics
     * @param statistics the list of statistics
     */
    @Override
    public void saveStatistics(List<Statistics> statistics){
        statisticsRepository.saveAll(statistics);
    }

    /**
     * Saves or updates a statistic
     * @param statistics the object
     */
    @Override
    public void saveOrUpdateStatistics(Statistics statistics){
        Optional<Statistics> optionalStatistics = statisticsRepository.findFirstByDatasetAndAndIdTableSchemaAndStatName(statistics.getDataset().getId(), statistics.getIdTableSchema(), statistics.getStatName());
        if(optionalStatistics.isPresent()){
            Statistics oldStatistics = optionalStatistics.get();
            //update the value of the statistics
            oldStatistics.setValue(statistics.getValue());
            statisticsRepository.save(oldStatistics);
        }
        else{
            statisticsRepository.save(statistics);
        }
    }

    @Override
    public Map<String, ImportStatisticsVO> getImportRelatedStatistics(Long datasetId, List<TableSchemaIdNameVO> tableSchemaIdNameVOList) throws Exception{
        Map<String, ImportStatisticsVO> statisticsMap = new HashMap<>();
        if(tableSchemaIdNameVOList != null){
            for(TableSchemaIdNameVO table: tableSchemaIdNameVOList){
                ImportStatisticsVO importStatistics = new ImportStatisticsVO();
                List<String> statisticsToRetrieve = Arrays.asList(LAST_IMPORT_DATE, TOTAL_RECORDS_IMPORTED, LAST_IMPORT_FILE_EXTENSION);
                List<Statistics> statisticsMetabase = statisticsRepository.findAllByDatasetAndIdTableSchemaAndStatNameIsIn(datasetId, table.getIdTableSchema(), statisticsToRetrieve);
                for(Statistics stat: statisticsMetabase){
                    if(stat.getStatName().equals(LAST_IMPORT_DATE)){
                        Date importDate = (stat.getValue() != null) ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stat.getValue()) : null;
                        importStatistics.setLastImportDate(importDate);
                    }
                    else if(stat.getStatName().equals(TOTAL_RECORDS_IMPORTED)){
                        Long numberOfRecordsImported = (stat.getValue() != null) ? Long.valueOf(stat.getValue()) : 0L;
                        importStatistics.setNumberOfRecordsImported(numberOfRecordsImported);
                    }
                    else if(stat.getStatName().equals(LAST_IMPORT_FILE_EXTENSION)){
                        String fileExtension = stat.getValue();
                        importStatistics.setFileExtension(fileExtension);
                    }
                }
                statisticsMap.put(table.getIdTableSchema(), importStatistics);
            }
        }
        return statisticsMap;
    }


    /**
     * Deletes old stats by dataset id but ignores some and saves the new ones
     *
     * @param datasetId the dataset id
     * @param statisticsToIgnore the statistics that will be ignored
     * @param newStatistics the new statistics that will be saved
     * @return
     *
     */
    @Override
    public void deleteOldStatsAndSaveNewOnes(Long datasetId, List<String> statisticsToIgnore, List<Statistics> newStatistics){
        statisticsRepository.deleteStatsByIdDatasetIgnoreStatsByName(datasetId, statisticsToIgnore);
        statisticsRepository.flush();
        statisticsRepository.saveAll(newStatistics);
    }
}
