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

  /** The snapshot repository. */
  @Autowired
  private SnapshotRepository snapshotRepository;


  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The reporting dataset service. */
  @Autowired
  private ReportingDatasetService reportingDatasetService;

  /** The partition data set metabase repository. */
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
    addLocksRelatedToPopulateEU(reportings, dataflowId);

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


  /**
   * Adds the locks related to populate EU.
   *
   * @param reportings the reportings
   * @param dataflowId the dataflow id
   * @throws EEAException the EEA exception
   */
  private void addLocksRelatedToPopulateEU(List<ReportingDatasetVO> reportings, Long dataflowId)
      throws EEAException {
    // Locks to avoid a provider can release a snapshot
    for (ReportingDatasetVO reporting : reportings) {
      Map<String, Object> mapCriteria = new HashMap<>();
      mapCriteria.put("signature", LockSignature.RELEASE_SNAPSHOT.getValue());
      mapCriteria.put("datasetId", reporting.getId());
      lockService.createLock(new Timestamp(System.currentTimeMillis()),
          SecurityContextHolder.getContext().getAuthentication().getName(), LockType.METHOD,
          mapCriteria);
    }
    // Lock to avoid export EUDataset while is copying data
    Map<String, Object> mapCriteriaExport = new HashMap<>();
    mapCriteriaExport.put("signature", LockSignature.RELEASE_SNAPSHOT.getValue());
    mapCriteriaExport.put("dataflowId", dataflowId);
    lockService.createLock(new Timestamp(System.currentTimeMillis()),
        SecurityContextHolder.getContext().getAuthentication().getName(), LockType.METHOD,
        mapCriteriaExport);
  }

  /**
   * Removes the locks related to populate EU.
   *
   * @param reportings the reportings
   * @param dataflowId the dataflow id
   */
  private void removeLocksRelatedToPopulateEU(List<ReportingDatasetVO> reportings,
      Long dataflowId) {
    // Release lock to the copy data to EU
    List<Object> criteria = new ArrayList<>();
    criteria.add(LockSignature.POPULATE_EU_DATASET.getValue());
    criteria.add(dataflowId);
    lockService.removeLockByCriteria(criteria);

    // Release lock to the export EU
    List<Object> criteriaExport = new ArrayList<>();
    criteriaExport.add(LockSignature.EXPORT_EU_DATASET.getValue());
    criteriaExport.add(dataflowId);
    lockService.removeLockByCriteria(criteriaExport);

    // Release locks to avoid a provider can release a snapshot
    for (ReportingDatasetVO reporting : reportings) {
      List<Object> criteriaReporting = new ArrayList<>();
      criteriaReporting.add(LockSignature.RELEASE_SNAPSHOT.getValue());
      criteriaReporting.add(reporting.getId());

      lockService.removeLockByCriteria(criteriaReporting);
    }
  }


  /**
   * Obtain partition.
   *
   * @param datasetId the dataset id
   * @param user the user
   * @return the partition data set metabase
   * @throws EEAException the EEA exception
   */
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
