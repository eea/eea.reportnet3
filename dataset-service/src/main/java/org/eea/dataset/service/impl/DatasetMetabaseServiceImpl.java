package org.eea.dataset.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.eea.dataset.mapper.DataSetMetabaseMapper;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
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
          fillDataset(dataset, datasetName, dataflowId, datasetSchemaId);
          reportingDatasetRepository.save((ReportingDataset) dataset);
          break;
        case DESIGN:
          dataset = new DesignDataset();
          fillDataset(dataset, datasetName, dataflowId, datasetSchemaId);
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
  private void fillDataset(DataSetMetabase dataset, String datasetName, Long idDataFlow,
      String datasetSchemaId) {

    dataset.setDataSetName(datasetName);
    dataset.setCreationDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
    dataset.setDataflowId(idDataFlow);
    dataset.setDatasetSchema(datasetSchemaId);

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
}
