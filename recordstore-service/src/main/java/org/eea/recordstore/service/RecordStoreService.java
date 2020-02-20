package org.eea.recordstore.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.eea.interfaces.vo.dataset.enums.TypeDatasetEnum;
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


  /**
   * Creates the data snapshot.
   *
   * @param idReportingDataset the id reporting dataset
   * @param idSnapshot the id snapshot
   * @param idPartitionDataset the id partition dataset
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws RecordStoreAccessException the record store access exception
   */
  void createDataSnapshot(Long idReportingDataset, Long idSnapshot, Long idPartitionDataset)
      throws SQLException, IOException, RecordStoreAccessException;


  /**
   * Restore data snapshot.
   *
   * @param idReportingDataset the id reporting dataset
   * @param idSnapshot the id snapshot
   * @param partitionId the partition id
   * @param typeDataset the type dataset
   * @param isSchemaSnapshot the is schema snapshot
   * @param deleteData the delete data
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws RecordStoreAccessException the record store access exception
   */
  void restoreDataSnapshot(Long idReportingDataset, Long idSnapshot, Long partitionId,
      TypeDatasetEnum typeDataset, Boolean isSchemaSnapshot, Boolean deleteData)
      throws SQLException, IOException, RecordStoreAccessException;

  /**
   * Delete data snapshot.
   *
   * @param idReportingDataset the id reporting dataset
   * @param idSnapshot the id snapshot
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void deleteDataSnapshot(Long idReportingDataset, Long idSnapshot) throws IOException;

  /**
   * Delete dataset.
   *
   * @param datasetSchemaName the dataset schema name
   */
  void deleteDataset(String datasetSchemaName);

  /**
   * Creates a schema for each entry in the list. Also releases events to feed the new schemas.
   * <p>
   * <b>Note:</b> {@literal @}<i>Async</i> annotated method.
   * </p>
   *
   * @param datasetIdsAndSchemaIds Map matching datasetIds with datasetSchemaIds.
   * @param dataflowId The DataCollection's dataflow.
   */
  void createSchemas(Map<Long, String> datasetIdAndSchemaId, Long dataflowId);
}
