package org.eea.dataset.service;

import org.eea.dataset.persistence.metabase.domain.Statistics;
import org.eea.interfaces.vo.dataset.ImportStatisticsVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaIdNameVO;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    /**
     * Saves statistics
     * @param statistics the list of statistics
     */
    void saveStatistics(List<Statistics> statistics);

    /**
     * Saves or updates a statistic
     * @param statistics the object
     */
    void saveOrUpdateStatistics(Statistics statistics);

    /**
     * Get import date and imported number of records
     *
     * @param datasetId the dataset id
     * @param tableSchemaIdNameVOList the tables in the dataset
     * @return a hashmap where key is tableSchemaId and value are the statistics
     *
     */
    Map<String, ImportStatisticsVO> getImportRelatedStatistics(Long datasetId, List<TableSchemaIdNameVO> tableSchemaIdNameVOList) throws Exception;

    /**
     * Deletes old stats by dataset id but ignores some and saves the new ones
     *
     * @param datasetId the dataset id
     * @param statisticsToIgnore the statistics that will be ignored
     * @param newStatistics the new statistics that will be saved
     * @return
     *
     */
    void deleteOldStatsAndSaveNewOnes(Long datasetId, List<String> statisticsToIgnore, List<Statistics> newStatistics);
}
