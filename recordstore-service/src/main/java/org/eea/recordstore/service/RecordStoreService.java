/*
 * 
 */
package org.eea.recordstore.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.recordstore.exception.RecordStoreAccessException;

/**
 * The Interface RecordStoreService.
 */
public interface RecordStoreService {


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
   * Creates the data set from other.
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
   * @param dateRelease the date release
   * @param prefillingReference the prefilling reference
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws RecordStoreAccessException the record store access exception
   * @throws EEAException the EEA exception
   */
  void createDataSnapshot(Long idReportingDataset, Long idSnapshot, Long idPartitionDataset,
      String dateRelease, boolean prefillingReference)
      throws SQLException, IOException, RecordStoreAccessException, EEAException;


  /**
   * Restore data snapshot.
   *
   * @param idReportingDataset the id reporting dataset
   * @param idSnapshot the id snapshot
   * @param partitionId the partition id
   * @param typeDataset the type dataset
   * @param isSchemaSnapshot the is schema snapshot
   * @param deleteData the delete data
   * @param prefillingReference the prefilling reference
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws RecordStoreAccessException the record store access exception
   */
  void restoreDataSnapshot(Long idReportingDataset, Long idSnapshot, Long partitionId,
      DatasetTypeEnum typeDataset, Boolean isSchemaSnapshot, Boolean deleteData,
      boolean prefillingReference) throws SQLException, IOException, RecordStoreAccessException;

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
   * @param datasetIdAndSchemaId the dataset id and schema id
   * @param dataflowId The DataCollection's dataflow.
   * @param isCreation the is creation
   * @param isMaterialized the is materialized
   */
  void createSchemas(Map<Long, String> datasetIdAndSchemaId, Long dataflowId, boolean isCreation,
      boolean isMaterialized);


  /**
   * Execute query view commands.
   *
   * @param command the command
   * @throws RecordStoreAccessException the record store access exception
   */
  void executeQueryViewCommands(String command) throws RecordStoreAccessException;


  /**
   * Creates the update query view.
   *
   * @param datasetId the dataset id
   * @param isMaterialized the is materialized
   */
  void createUpdateQueryView(Long datasetId, boolean isMaterialized);

  /**
   * Update materialized query view.
   *
   * @param datasetId the dataset id
   * @param user the user
   * @param released the released
   * @param processId the process id
   */
  void updateMaterializedQueryView(Long datasetId, String user, Boolean released, String processId);

  /**
   * Launch update materialized query view.
   *
   * @param datasetId the dataset id
   * @throws RecordStoreAccessException the record store access exception
   */
  void launchUpdateMaterializedQueryView(Long datasetId) throws RecordStoreAccessException;

  /**
   * Refresh materialized query.
   *
   * @param datasetIds the dataset ids
   * @param continueValidation the continue validation
   * @param released the released
   * @param datasetId the dataset id
   * @param processId the process id
   */
  void refreshMaterializedQuery(List<Long> datasetIds, boolean continueValidation, boolean released,
      Long datasetId, String processId);

  /**
   * Update snapshot disabled.
   *
   * @param datasetId the dataset id
   */
  void updateSnapshotDisabled(Long datasetId);

  /**
   * Creates the snapshot to clone.
   *
   * @param originDataset the origin dataset
   * @param targetDataset the target dataset
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param partitionDatasetTarget the partition dataset target
   * @param tableSchemasIdPrefill the table schemas id prefill
   */
  void createSnapshotToClone(Long originDataset, Long targetDataset,
      Map<String, String> dictionaryOriginTargetObjectId, Long partitionDatasetTarget,
      List<String> tableSchemasIdPrefill);

  /**
   * Distribute tables.
   *
   * @param datasetId the dataset id
   */
  void distributeTables(Long datasetId);

  /**
   * Distribute tables job.
   *
   * @param datasetId the dataset id
   */
  void distributeTablesJob(Long datasetId);

  /**
   * Gets the notdistributed datasets.
   *
   * @return the notdistributed datasets
   */
  List<String> getNotdistributedDatasets();

  /**
   * Creates the update query view async.
   *
   * @param datasetId the dataset id
   * @param isMaterialized the is materialized
   */
  void createUpdateQueryViewAsync(Long datasetId, boolean isMaterialized);
}
