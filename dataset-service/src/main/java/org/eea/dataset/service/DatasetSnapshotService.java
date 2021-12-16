package org.eea.dataset.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.metabase.ReleaseVO;
import org.eea.interfaces.vo.metabase.SnapshotVO;

/**
 * The Interface DatasetSnapshotService.
 */
public interface DatasetSnapshotService {


  /**
   * Gets the by id.
   *
   * @param idSnapshot the id snapshot
   * @return the by id
   * @throws EEAException the EEA exception
   */
  SnapshotVO getById(Long idSnapshot) throws EEAException;

  /**
   * Gets the schema by id.
   *
   * @param idSnapshot the id snapshot
   * @return the schema by id
   * @throws EEAException the EEA exception
   */
  SnapshotVO getSchemaById(Long idSnapshot) throws EEAException;

  /**
   * Gets the snapshots by id dataset.
   *
   * @param datasetId the dataset id
   * @return the snapshots by id dataset
   * @throws EEAException the EEA exception
   */
  List<SnapshotVO> getSnapshotsByIdDataset(Long datasetId) throws EEAException;


  /**
   * Adds the snapshot.
   *
   * @param idDataset the id dataset
   * @param createSnapshotVO the create snapshot VO
   * @param partitionIdDestination the partition id destination
   * @param dateRelease the date release
   * @param prefillingReference the prefilling reference
   */
  void addSnapshot(Long idDataset, CreateSnapshotVO createSnapshotVO, Long partitionIdDestination,
      String dateRelease, boolean prefillingReference);

  /**
   * Removes the snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @throws EEAException the EEA exception
   */
  void removeSnapshot(Long idDataset, Long idSnapshot) throws EEAException;

  /**
   * Restore snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @param deleteData the delete data
   * @throws EEAException the EEA exception
   */
  void restoreSnapshot(Long idDataset, Long idSnapshot, Boolean deleteData) throws EEAException;


  /**
   * Restore snapshot to clone data.
   *
   * @param datasetOrigin the dataset origin
   * @param idDatasetDestination the id dataset destination
   * @param idSnapshot the id snapshot
   * @param deleteData the delete data
   * @param datasetType the dataset type
   * @param prefillingReference the prefilling reference
   * @throws EEAException the EEA exception
   */
  void restoreSnapshotToCloneData(Long datasetOrigin, Long idDatasetDestination, Long idSnapshot,
      Boolean deleteData, DatasetTypeEnum datasetType, boolean prefillingReference)
      throws EEAException;

  /**
   * Release snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @param dateRelease the date release
   * @throws EEAException the EEA exception
   */
  void releaseSnapshot(Long idDataset, Long idSnapshot, String dateRelease) throws EEAException;


  /**
   * Gets the schema snapshots by id dataset.
   *
   * @param datasetId the dataset id
   * @return the schema snapshots by id dataset
   * @throws EEAException the EEA exception
   */
  List<SnapshotVO> getSchemaSnapshotsByIdDataset(Long datasetId) throws EEAException;

  /**
   * Adds the schema snapshot.
   *
   * @param idDataset the id dataset
   * @param idDatasetSchema the id dataset schema
   * @param description the description
   */
  void addSchemaSnapshot(Long idDataset, String idDatasetSchema, String description);

  /**
   * Restore schema snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void restoreSchemaSnapshot(Long idDataset, Long idSnapshot) throws EEAException, IOException;



  /**
   * Removes the schema snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  void removeSchemaSnapshot(Long idDataset, Long idSnapshot)
      throws EEAException, IOException, Exception;

  /**
   * Delete all schema snapshots.
   *
   * @param idDesignDataset the id design dataset
   * @throws EEAException the EEA exception
   */
  void deleteAllSchemaSnapshots(Long idDesignDataset) throws EEAException;


  /**
   * Creates the receipt PDF.
   *
   * @param out the out
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   */
  void createReceiptPDF(OutputStream out, Long dataflowId, Long dataProviderId);

  /**
   * Gets the snapshots released by id dataset.
   *
   * @param datasetId the dataset id
   * @return the snapshots released by id dataset
   */
  List<ReleaseVO> getSnapshotsReleasedByIdDataset(Long datasetId);

  /**
   * Gets the snapshots released by id data collection.
   *
   * @param dataCollectionId the data collection id
   * @return the snapshots released by id data collection
   */
  List<ReleaseVO> getSnapshotsReleasedByIdDataCollection(Long dataCollectionId);

  /**
   * Gets the snapshots released by id EU dataset.
   *
   * @param datasetId the dataset id
   * @return the snapshots released by id EU dataset
   * @throws EEAException the EEA exception
   */
  List<ReleaseVO> getSnapshotsReleasedByIdEUDataset(Long datasetId) throws EEAException;

  /**
   * Update snapshot EU release.
   *
   * @param datasetId the dataset id
   */
  void updateSnapshotEURelease(Long datasetId);

  /**
   * Gets the dataset historic releases per each type.
   *
   * @param datasetId the dataset id
   * @return the releases
   * @throws EEAException the EEA exception
   */
  List<ReleaseVO> getReleases(Long datasetId) throws EEAException;


  /**
   * Creates the release snapshots.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param restrictFromPublic the restrict from public
   * @throws EEAException the EEA exception
   */
  void createReleaseSnapshots(Long dataflowId, Long dataProviderId, boolean restrictFromPublic)
      throws EEAException;


  /**
   * Release locks related to release.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @throws EEAException the EEA exception
   */
  void releaseLocksRelatedToRelease(Long dataflowId, Long dataProviderId) throws EEAException;

  /**
   * Obtain partition.
   *
   * @param datasetId the dataset id
   * @param user the user
   * @return the partition data set metabase
   * @throws EEAException the EEA exception
   */
  PartitionDataSetMetabase obtainPartition(final Long datasetId, final String user)
      throws EEAException;
}
