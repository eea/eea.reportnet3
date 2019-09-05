package org.eea.recordstore.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.recordstore.exception.RecordStoreAccessException;

/**
 * The interface Record store service.
 */
public interface RecordStoreService {


  /**
   * Reset dataset database.
   *
   * @throws RecordStoreAccessException the docker access exception
   * @deprecated (pending to remove)
   */
  @Deprecated
  void resetDatasetDatabase() throws RecordStoreAccessException;


  /**
   * Creates the empty data set.
   *
   * @param datasetName the dataset name
   * @param idDatasetSchema the id dataset schema
   * @throws RecordStoreAccessException the record store access exception
   */
  void createEmptyDataSet(String datasetName, String idDatasetSchema)
      throws RecordStoreAccessException;

  /**
   * Create data set from other.
   *
   * @param sourceDatasetName the source dataset name
   * @param destinationDataSetName the destination data set name
   */
  void createDataSetFromOther(String sourceDatasetName, String destinationDataSetName);

  /**
   * Gets connection data for dataset.
   *
   * @param datasetName the dataset name
   *
   * @return the connection data for dataset
   *
   * @throws RecordStoreAccessException the docker access exception
   */
  ConnectionDataVO getConnectionDataForDataset(String datasetName)
      throws RecordStoreAccessException;

  /**
   * Gets connection data for dataset.
   *
   * @return the connection data for dataset
   *
   * @throws RecordStoreAccessException the docker access exception
   */
  List<ConnectionDataVO> getConnectionDataForDataset() throws RecordStoreAccessException;


  void createDataSnapshot(Long idReportingDataset, Long idSnapshot, Long idPartitionDataset)
      throws SQLException, IOException, RecordStoreAccessException;

  void restoreDataSnapshot(Long idReportingDataset, Long idSnapshot)
      throws SQLException, IOException, RecordStoreAccessException;

  void deleteDataSnapshot(Long idReportingDataset, Long idSnapshot) throws IOException;


}
