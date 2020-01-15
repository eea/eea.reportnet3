package org.eea.dataset.service;

import java.io.IOException;
import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.metabase.SnapshotVO;

/**
 * The Interface DatasetSnapshotService.
 */
public interface DatasetSnapshotService {

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
   * @param description the description
   * @param released the released
   */
  void addSnapshot(Long idDataset, String description, Boolean released);

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
   * Release snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @throws EEAException the EEA exception
   */
  void releaseSnapshot(Long idDataset, Long idSnapshot) throws EEAException;


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
   * Delete all snapshots.
   *
   * @param idDataset the id dataset
   * @throws EEAException the EEA exception
   */
  void deleteAllSnapshots(Long idDataset) throws EEAException;


}
