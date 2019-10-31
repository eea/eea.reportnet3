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
   * @throws EEAException the EEA exception
   */
  void addSnapshot(Long idDataset, String description) throws EEAException;

  /**
   * Removes the snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @throws EEAException the EEA exception
   */
  void removeSnapshot(Long idDataset, Long idSnapshot) throws EEAException;



  void restoreSnapshot(Long idDataset, Long idSnapshot) throws EEAException;

  /**
   * Release snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @throws EEAException the EEA exception
   */
  void releaseSnapshot(Long idDataset, Long idSnapshot) throws EEAException;


  List<SnapshotVO> getSchemaSnapshotsByIdDataset(Long datasetId) throws EEAException;

  void addSchemaSnapshot(Long idDataset, String idDatasetSchema, String description)
      throws EEAException, IOException;

  void restoreSchemaSnapshot(Long idDataset, Long idSnapshot) throws EEAException, IOException;


  void removeSchemaSnapshot(Long idDataset, Long idSnapshot) throws EEAException, IOException;



}
