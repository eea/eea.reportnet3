package org.eea.dataset.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eea.dataset.mapper.EUDatasetMapper;
import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.eea.dataset.persistence.metabase.domain.EUDataset;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.Snapshot;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.EUDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.SnapshotRepository;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.service.EUDatasetService;
import org.eea.dataset.service.ReportingDatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.lock.enums.LockType;
import org.eea.lock.service.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


/**
 * The Class EUDatasetServiceImpl.
 */
@Service
public class EUDatasetServiceImpl implements EUDatasetService {

  /** The eu dataset repository. */
  @Autowired
  private EUDatasetRepository euDatasetRepository;

  /** The eu dataset mapper. */
  @Autowired
  private EUDatasetMapper euDatasetMapper;

  /** The dataset snapshot service. */
  @Autowired
  private DatasetSnapshotService datasetSnapshotService;

  /** The data collection service. */
  @Autowired
  private DataCollectionRepository dataCollectionRepository;

  @Autowired
  private SnapshotRepository snapshotRepository;


  @Autowired
  private LockService lockService;

  @Autowired
  private ReportingDatasetService reportingDatasetService;

  @Autowired
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;



  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(EUDatasetServiceImpl.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");



  /**
   * Gets the EU dataset by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the EU dataset by dataflow id
   */
  @Override
  public List<EUDatasetVO> getEUDatasetByDataflowId(Long idDataflow) {
    List<EUDataset> euDatasets = euDatasetRepository.findByDataflowId(idDataflow);
    return euDatasetMapper.entityListToClass(euDatasets);
  }



  /**
   * Populate EU dataset with data collection.
   *
   * @param dataflowId the dataflow id
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void populateEUDatasetWithDataCollection(Long dataflowId) throws EEAException {
    // First we lock some operations
    List<ReportingDatasetVO> reportings =
        reportingDatasetService.getDataSetIdByDataflowId(dataflowId);
    addLocksRelatedToPopulateEU(reportings);

    // Load the dataCollections to be copied
    List<DataCollection> dataCollectionList = dataCollectionRepository.findByDataflowId(dataflowId);
    List<EUDataset> euDatasetList = euDatasetRepository.findByDataflowId(dataflowId);

    Map<Long, Long> relatedDatasetsByIds =
        combineListsIntoOrderedMap(dataCollectionList, euDatasetList);

    // Store the data in snapshots for quick import
    for (DataCollection dataCollection : dataCollectionList) {
      datasetSnapshotService.addSnapshot(dataCollection.getId(), dataCollection.getDatasetSchema(),
          false, obtainPartition(relatedDatasetsByIds.get(dataCollection.getId()), "root").getId());
    }


    try {
      Thread.sleep(60000L);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    for (Entry<Long, Long> entry : relatedDatasetsByIds.entrySet()) {
      LOG.info("Voy a buscar los snapshots del datasetId {}", entry.getKey());
      Snapshot snap = snapshotRepository.findFirstByReportingDatasetId(entry.getKey());
      datasetSnapshotService.restoreSnapshotToCloneData(entry.getKey(), entry.getValue(),
          snap.getId(), true, DatasetTypeEnum.EUDATASET);
    }
    // Finally, we release the locks
    removeLocksRelatedToPopulateEU(reportings, dataflowId);

    // borrar los snapshots
  }

  /**
   * Combine lists into ordered map.
   *
   * @param dataCollectionList the data collection list
   * @param euDataflowList the eu dataflow list
   * @return the map
   */
  private Map<Long, Long> combineListsIntoOrderedMap(List<DataCollection> dataCollectionList,
      List<EUDataset> euDataflowList) {
    if (dataCollectionList.size() != euDataflowList.size())
      throw new IllegalArgumentException("Cannot combine lists with dissimilar sizes");
    Map<Long, Long> map = new HashMap<>();
    for (int i = 0; i < dataCollectionList.size(); i++) {
      DataCollection dataCollection = dataCollectionList.get(i);
      EUDataset euDatasetFound = euDataflowList.stream()
          .filter(
              euDataset -> euDataset.getDatasetSchema().equals(dataCollection.getDatasetSchema()))
          .findFirst().orElse(null);
      if (euDatasetFound != null) {
        map.put(dataCollection.getId(), euDatasetFound.getId());
      }
    }
    return map;
  }


  private void addLocksRelatedToPopulateEU(List<ReportingDatasetVO> reportings)
      throws EEAException {
    for (ReportingDatasetVO reporting : reportings) {
      Map<String, Object> mapCriteria = new HashMap<>();
      mapCriteria.put("signature", LockSignature.RELEASE_SNAPSHOT.getValue());
      mapCriteria.put("datasetId", reporting.getId());
      lockService.createLock(new Timestamp(System.currentTimeMillis()),
          SecurityContextHolder.getContext().getAuthentication().getName(), LockType.METHOD,
          mapCriteria);
    }
  }

  private void removeLocksRelatedToPopulateEU(List<ReportingDatasetVO> reportings,
      Long dataflowId) {
    List<Object> criteria = new ArrayList<>();
    criteria.add(LockSignature.POPULATE_EU_DATASET.getValue());
    criteria.add(dataflowId);
    lockService.removeLockByCriteria(criteria);

    for (ReportingDatasetVO reporting : reportings) {
      List<Object> criteriaReporting = new ArrayList<>();
      criteriaReporting.add(LockSignature.RELEASE_SNAPSHOT.getValue());
      criteriaReporting.add(reporting.getId());

      lockService.removeLockByCriteria(criteriaReporting);
    }
  }


  public void pruebaLock(Long dataflowId) throws EEAException, InterruptedException {

    List<ReportingDatasetVO> reportings =
        reportingDatasetService.getDataSetIdByDataflowId(dataflowId);

    for (ReportingDatasetVO reporting : reportings) {
      List<Object> criteriaReporting = new ArrayList<>();
      criteriaReporting.add(LockSignature.RELEASE_SNAPSHOT.getValue());
      criteriaReporting.add(reporting.getId());
      Map<String, Object> mapa = new HashMap<>();
      mapa.put("signature", LockSignature.RELEASE_SNAPSHOT.getValue());
      mapa.put("datasetId", reporting.getId());
      lockService.createLock(new Timestamp(System.currentTimeMillis()),
          SecurityContextHolder.getContext().getAuthentication().getName(), LockType.METHOD, mapa);
    }

    Thread.sleep(60000L);

    List<Object> criteria = new ArrayList<>();
    criteria.add(LockSignature.POPULATE_EU_DATASET.getValue());
    criteria.add(dataflowId);
    lockService.removeLockByCriteria(criteria);

    for (ReportingDatasetVO reporting : reportings) {
      List<Object> criteriaReporting = new ArrayList<>();
      criteriaReporting.add(LockSignature.RELEASE_SNAPSHOT.getValue());
      criteriaReporting.add(reporting.getId());

      lockService.removeLockByCriteria(criteriaReporting);
    }

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
