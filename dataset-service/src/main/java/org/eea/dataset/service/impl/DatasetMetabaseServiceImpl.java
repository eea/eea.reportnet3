package org.eea.dataset.service.impl;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.mapper.DataSetMetabaseMapper;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.domain.Statistics;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.StatisticsRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.TableStatisticsVO;
import org.eea.interfaces.vo.dataset.enums.TypeDatasetEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class DatasetMetabaseServiceImpl.
 */
@Service("datasetMetabaseService")
public class DatasetMetabaseServiceImpl implements DatasetMetabaseService {

  /** The data set metabase repository. */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The data set metabase mapper. */
  @Autowired
  private DataSetMetabaseMapper dataSetMetabaseMapper;

  /** The reporting dataset repository. */
  @Autowired
  private ReportingDatasetRepository reportingDatasetRepository;

  /** The design dataset repository. */
  @Autowired
  private DesignDatasetRepository designDatasetRepository;

  /** The record store controller zull. */
  @Autowired
  private RecordStoreControllerZull recordStoreControllerZull;

  @Autowired
  private StatisticsRepository statisticsRepository;


  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetMetabaseServiceImpl.class);

  /**
   * Gets the data set id by dataflow id.
   *
   * @param idFlow the id flow
   * @return the data set id by dataflow id
   */
  @Override
  public List<DataSetMetabaseVO> getDataSetIdByDataflowId(Long idFlow) {

    List<DataSetMetabase> datasets = dataSetMetabaseRepository.findByDataflowId(idFlow);
    return dataSetMetabaseMapper.entityListToClass(datasets);
  }



  /**
   * Creates the empty dataset.
   *
   * @param datasetType the dataset type
   * @param datasetName the dataset name
   * @param datasetSchemaId the dataset schema id
   * @param dataflowId the dataflow id
   * @return the long
   * @throws EEAException the EEA exception
   */
  @Override
  @org.springframework.transaction.annotation.Transactional(
      value = "metabaseDataSetsTransactionManager")
  /**
   * We use spring Transactional with this value to indicate we want to use the metabase
   * transactional manager. Otherwise the operation will be fail
   */
  public Long createEmptyDataset(TypeDatasetEnum datasetType, String datasetName,
      String datasetSchemaId, Long dataflowId) throws EEAException {

    if (datasetType != null && datasetName != null && datasetSchemaId != null
        && dataflowId != null) {
      DataSetMetabase dataset;

      switch (datasetType) {
        case REPORTING:
          dataset = new ReportingDataset();
          fillDataset(dataset, datasetName, dataflowId);
          reportingDatasetRepository.save((ReportingDataset) dataset);
          break;
        case DESIGN:
          dataset = new DesignDataset();
          fillDataset(dataset, datasetName, dataflowId);
          designDatasetRepository.save((DesignDataset) dataset);
          break;
        default:
          throw new EEAException("Unsupported datasetType: " + datasetType);
      }

      recordStoreControllerZull.createEmptyDataset("dataset_" + dataset.getId(), datasetSchemaId);
      return dataset.getId();
    }

    throw new EEAException("createEmptyDataset: Bad arguments");
  }

  /**
   * Fill dataset.
   *
   * @param dataset the dataset
   * @param datasetName the dataset name
   * @param idDataFlow the id data flow
   */
  private void fillDataset(DataSetMetabase dataset, String datasetName, Long idDataFlow) {

    dataset.setDataSetName(datasetName);
    dataset.setCreationDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
    dataset.setDataflowId(idDataFlow);

    PartitionDataSetMetabase partition = new PartitionDataSetMetabase();
    partition.setUsername("root");
    partition.setIdDataSet(dataset);

    dataset.setPartitions(Arrays.asList(partition));
  }


  /**
   * Gets the dataset name.
   *
   * @param idDataset the id dataset
   * @return the dataset name
   */
  @Override
  public DataSetMetabaseVO findDatasetMetabase(Long idDataset) {
    Optional<DataSetMetabase> datasetMetabase = dataSetMetabaseRepository.findById(idDataset);
    return dataSetMetabaseMapper.entityToClass(datasetMetabase.get());
  }

  /**
   * Delete design dataset.
   *
   * @param datasetId the dataset id
   */
  @Override
  @Transactional
  public void deleteDesignDataset(Long datasetId) {
    dataSetMetabaseRepository.deleteById(datasetId);
  }

  /**
   * Update dataset name.
   *
   * @param datasetId the dataset id
   * @param datasetName the dataset name
   * @return true, if successful
   */
  @Override
  public boolean updateDatasetName(Long datasetId, String datasetName) {
    DataSetMetabase datasetMetabase = dataSetMetabaseRepository.findById(datasetId).orElse(null);
    if (datasetMetabase != null) {
      datasetMetabase.setDataSetName(datasetName);
      dataSetMetabaseRepository.save(datasetMetabase);
      return true;
    }
    return false;
  }


  /**
   * Gets the statistics.
   *
   * @param datasetId the dataset id
   *
   * @return the statistics
   *
   * @throws EEAException the EEA exception
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   */
  @Override
  @Transactional
  public StatisticsVO getStatistics(final Long datasetId)
      throws EEAException, InstantiationException, IllegalAccessException {

    List<Statistics> statistics = statisticsRepository.findStatisticsByIdDataset(datasetId);

    return processStatistics(statistics);
  }


  /**
   * Sets the entity property.
   *
   * @param object the object
   * @param fieldName the field name
   * @param fieldValue the field value
   *
   * @return the boolean
   */
  public static Boolean setEntityProperty(Object object, String fieldName, String fieldValue) {
    Class<?> clazz = object.getClass();
    while (clazz != null) {
      try {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        if (field.getType().equals(Long.class)) {
          field.set(object, Long.valueOf(fieldValue));
        } else if (field.getType().equals(Boolean.class)) {
          field.set(object, Boolean.valueOf(fieldValue));
        } else {
          field.set(object, fieldValue);
        }

        return true;
      } catch (NoSuchFieldException e) {
        clazz = clazz.getSuperclass();
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }
    return false;
  }


  private StatisticsVO processStatistics(List<Statistics> statistics)
      throws InstantiationException, IllegalAccessException {

    StatisticsVO stats = new StatisticsVO();

    List<Statistics> statisticsTables = statistics.stream()
        .filter(s -> StringUtils.isNotBlank(s.getIdTableSchema())).collect(Collectors.toList());
    List<Statistics> statisticsDataset = statistics.stream()
        .filter(s -> StringUtils.isBlank(s.getIdTableSchema())).collect(Collectors.toList());

    Map<String, List<Statistics>> tablesMap = statisticsTables.stream()
        .collect(Collectors.groupingBy(Statistics::getIdTableSchema, Collectors.toList()));

    // Dataset level stats
    Class<?> clazzStats = stats.getClass();
    Object instance = clazzStats.newInstance();
    statisticsDataset.stream().forEach(s -> {
      setEntityProperty(instance, s.getStatName(), s.getValue());
    });
    stats = (StatisticsVO) instance;

    // Table statistics
    stats.setTables(new ArrayList<>());
    for (List<Statistics> listStats : tablesMap.values()) {
      Class<?> clazzTable = TableStatisticsVO.class;
      Object instanceTable = clazzTable.newInstance();
      listStats.stream().forEach(s -> {
        setEntityProperty(instanceTable, s.getStatName(), s.getValue());
      });
      stats.getTables().add((TableStatisticsVO) instanceTable);
    }

    return stats;


  }



  @Override
  public List<StatisticsVO> getGlobalStatistics(Long dataflowId)
      throws InstantiationException, IllegalAccessException {

    List<StatisticsVO> statistics = new ArrayList<>();

    List<ReportingDataset> datasets = reportingDatasetRepository.findByDataflowId(dataflowId);
    List<Long> datasetsId = new ArrayList<>();
    datasets.stream().forEach(d -> datasetsId.add(d.getId()));
    List<Statistics> stats = statisticsRepository.findStatisticsByIdDatasets(datasetsId);

    Map<ReportingDataset, List<Statistics>> statsMap =
        stats.stream().collect(Collectors.groupingBy(Statistics::getDataset, Collectors.toList()));

    statsMap.values().stream().forEach(s -> {

      try {
        statistics.add(processStatistics(s));
      } catch (InstantiationException | IllegalAccessException e) {

      }

    });


    return statistics;
  }

}
