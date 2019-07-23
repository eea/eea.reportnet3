package org.eea.dataset.service.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.eea.dataset.mapper.DataSetMetabaseMapper;
import org.eea.dataset.mapper.SnapshotMapper;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.domain.Snapshot;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.SnapshotRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.metabase.SnapshotVO;
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


    List<Snapshot> snapshots = snapshotRepository.findByReportingDatasetId(datasetId);

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

    Snapshot snap = new Snapshot();
    snap.setCreationDate(Date.valueOf(LocalDate.now()));
    snap.setDescription(description);
    ReportingDataset reportingDataset = new ReportingDataset();
    reportingDataset.setId(idDataset);
    snap.setReportingDataset(reportingDataset);


    snapshotRepository.save(snap);

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
  }

}
