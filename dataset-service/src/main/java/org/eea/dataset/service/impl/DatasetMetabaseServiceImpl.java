package org.eea.dataset.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.eea.dataset.mapper.DataSetMetabaseMapper;
import org.eea.dataset.mapper.SnapshotMapper;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.domain.Snapshot;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.SnapshotRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

  /** The snapshot repository. */
  @Autowired
  private SnapshotRepository snapshotRepository;

  /** The snapshot mapper. */
  @Autowired
  private SnapshotMapper snapshotMapper;

  /**
   * The table repository.
   */
  @Autowired
  private TableRepository tableRepository;

  @Autowired
  private RecordRepository recordRepository;

  /**
   * The partition data set metabase repository.
   */
  @Autowired
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  /**
   * The record store controller zull.
   */
  @Autowired
  private RecordStoreControllerZull recordStoreControllerZull;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetMetabaseServiceImpl.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


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
   * Gets the snapshots by id dataset.
   *
   * @param datasetId the dataset id
   * @return the snapshots by id dataset
   * @throws EEAException the EEA exception
   */
  @Override
  public List<SnapshotVO> getSnapshotsByIdDataset(Long datasetId) throws EEAException {

    List<Snapshot> snapshots =
        snapshotRepository.findByReportingDatasetIdOrderByCreationDateDesc(datasetId);

    return snapshotMapper.entityListToClass(snapshots);

  }

  /**
   * Adds the snapshot.
   *
   * @param idDataset the id dataset
   * @param description the description
   * @throws EEAException the EEA exception
   */
  @Override
  public void addSnapshot(Long idDataset, String description) throws EEAException {

    // 1. Create the snapshot in the metabase
    Snapshot snap = new Snapshot();
    snap.setCreationDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
    snap.setDescription(description);
    ReportingDataset reportingDataset = new ReportingDataset();
    reportingDataset.setId(idDataset);
    snap.setReportingDataset(reportingDataset);
    snap.setDataSetName("snapshot from dataset_" + idDataset);

    snapshotRepository.save(snap);
    LOG.info("Snapshot {} created into the metabase", snap.getId());

    // 2. Create the data file of the snapshot, calling to recordstore-service
    // we need the partitionId. By now only consider the user root
    Long idPartition = obtainPartition(idDataset, "root").getId();
    recordStoreControllerZull.createSnapshotData(idDataset, snap.getId(), idPartition);
    LOG.info("Snapshot {} data files created", snap.getId());
  }

  /**
   * Removes the snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @throws EEAException the EEA exception
   */
  @Override
  public void removeSnapshot(Long idDataset, Long idSnapshot) throws EEAException {
    snapshotRepository.removeSnaphot(idDataset, idSnapshot);
    LOG.info("Snapshot {} removed", idSnapshot);
  }

  @Override
  public void restoreSnapshot(Long idDataset, Long idSnapshot) throws EEAException {

    // 1. Delete the dataset values implied
    // we need the partitionId. By now only consider the user root
    Long idPartition = obtainPartition(idDataset, "root").getId();
    recordRepository.deleteRecordValuesToRestoreSnapshot(idPartition);
    LOG.info("First step of restoring snapshot completed. Previously data erased");

    // 2. Restore the dataset data, using the operation from recordstore
    recordStoreControllerZull.restoreSnapshotData(idDataset, idSnapshot);
    LOG.info("Snapshot {} restored", idSnapshot);
  }


  private PartitionDataSetMetabase obtainPartition(final Long datasetId, final String user)
      throws EEAException {
    final PartitionDataSetMetabase partition = partitionDataSetMetabaseRepository
        .findFirstByIdDataSet_idAndUsername(datasetId, user).orElse(null);
    if (partition == null) {
      LOG_ERROR.error(EEAErrorMessage.PARTITION_ID_NOTFOUND);
      throw new EEAException(EEAErrorMessage.PARTITION_ID_NOTFOUND);
    }
    return partition;
  }

}
