package org.eea.dataset.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eea.dataset.mapper.EUDatasetMapper;
import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.eea.dataset.persistence.metabase.domain.EUDataset;
import org.eea.dataset.persistence.metabase.domain.Snapshot;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.EUDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.SnapshotRepository;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.service.EUDatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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
    // Load the dataCollections to be copied
    List<DataCollection> dataCollectionList = dataCollectionRepository.findByDataflowId(dataflowId);
    List<EUDataset> euDatasetList = euDatasetRepository.findByDataflowId(dataflowId);
    // Store the data in snapshots for quick import
    for (DataCollection dataCollection : dataCollectionList) {
      datasetSnapshotService.addSnapshot(dataCollection.getId(), dataCollection.getDatasetSchema(),
          false);
    }
    Map<Long, Long> relatedDatasetsByIds =
        combineListsIntoOrderedMap(dataCollectionList, euDatasetList);

    for (Entry<Long, Long> entry : relatedDatasetsByIds.entrySet()) {
      Snapshot snap = snapshotRepository.findByReportingDatasetId(entry.getKey()).get(0);
      datasetSnapshotService.restoreSnapshotToCloneData(entry.getKey(), entry.getValue(),
          snap.getId(), true, DatasetTypeEnum.EUDATASET);
    }
    // borrar los snapshots
  }

  /**
   * Combine lists into ordered map.
   *
   * @param dataCollectionList the data collection list
   * @param euDataflowList the eu dataflow list
   * @return the map
   */
  Map<Long, Long> combineListsIntoOrderedMap(List<DataCollection> dataCollectionList,
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

}
