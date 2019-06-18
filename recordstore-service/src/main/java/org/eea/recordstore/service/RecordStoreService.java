package org.eea.recordstore.service;

import java.util.List;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.recordstore.exception.DockerAccessException;

/**
 * The interface Record store service.
 */
public interface RecordStoreService {



  /**
   * Reset dataset database.
   *
   * @throws DockerAccessException the docker access exception
   * @deprecated (pending to remove)
   */
  @Deprecated
  void resetDatasetDatabase() throws DockerAccessException;

  /**
   * Create empty data set.
   *
   * @param datasetName the dataset name
   *
   * @throws DockerAccessException the docker access exception
   */
  void createEmptyDataSet(String datasetName) throws DockerAccessException;

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
   * @throws DockerAccessException the docker access exception
   */
  ConnectionDataVO getConnectionDataForDataset(String datasetName) throws DockerAccessException;

  /**
   * Gets connection data for dataset.
   *
   * @return the connection data for dataset
   *
   * @throws DockerAccessException the docker access exception
   */
  List<ConnectionDataVO> getConnectionDataForDataset() throws DockerAccessException;
}
