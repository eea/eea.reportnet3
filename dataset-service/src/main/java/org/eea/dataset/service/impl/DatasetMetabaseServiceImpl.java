package org.eea.dataset.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.mapper.DataSetMetabaseMapper;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
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

  /** The record store controller zull. */
  @Autowired
  private RecordStoreControllerZull recordStoreControllerZull;


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
   * @param datasetName the dataset name
   * @param idDatasetSchema the id dataset schema
   * @param idDataflow the id dataflow
   * @throws EEAException the EEA exception
   */
  @Override
  @org.springframework.transaction.annotation.Transactional(
      value = "metabaseDataSetsTransactionManager")
  /**
   * We use spring Transactional with this value to indicate we want to use the metabase
   * transactional manager. Otherwise the operation will be fail
   */
  public void createEmptyDataset(final String datasetName, String idDatasetSchema, Long idDataflow)
      throws EEAException {

    ReportingDataset reportingData = new ReportingDataset();
    reportingData.setDataSetName(datasetName);
    reportingData.setCreationDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
    reportingData.setDataflowId(idDataflow);
    PartitionDataSetMetabase partition = new PartitionDataSetMetabase();
    partition.setUsername("root");
    partition.setIdDataSet(reportingData);
    List<PartitionDataSetMetabase> partitions = new ArrayList<>();
    partitions.add(partition);
    reportingData.setPartitions(partitions);
    // save reporting dataset into metabase
    reportingDatasetRepository.save(reportingData);

    // create the dataset into datasets
    recordStoreControllerZull.createEmptyDataset("dataset_" + reportingData.getId(),
        idDatasetSchema);
  }


  /**
   * Gets the dataset name.
   *
   * @param idDataset the id dataset
   * @return the dataset name
   */
  @Override
  public DataSetMetabaseVO findDatasetMetabase(Long idDataset) {
    DataSetMetabase datasetMetabase =
        dataSetMetabaseRepository.findById(idDataset).orElse(new DataSetMetabase());
    return dataSetMetabaseMapper.entityToClass(datasetMetabase);

  }

  @Override
  @Transactional
  public void deleteDesignDataset(Long datasetId) {
    dataSetMetabaseRepository.deleteById(datasetId);
  }

  @Override
  public boolean updateDatasetName(Long datasetId, String datasetName) {
    DataSetMetabase datasetMetabase = dataSetMetabaseRepository.findById(datasetId).orElse(null);
    if (!datasetName.isEmpty() && datasetMetabase != null) {
      datasetMetabase.setDataSetName(datasetName);
      dataSetMetabaseRepository.save(datasetMetabase);
      return true;
    }
    return false;
  }
}
