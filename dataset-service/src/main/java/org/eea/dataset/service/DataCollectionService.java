package org.eea.dataset.service;

import java.time.LocalDateTime;
import java.util.List;
import org.eea.dataset.service.model.FKDataCollection;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.springframework.web.bind.annotation.PathVariable;

/** The Interface DataCollectionService. */
public interface DataCollectionService {

  /**
   * Gets the data collection id by dataflow id.
   *
   * @param idFlow the id flow
   * @return the data collection id by dataflow id
   */
  List<DataCollectionVO> getDataCollectionIdByDataflowId(Long idFlow);

  /**
   * Gets the dataflow status.
   *
   * @param dataflowId the dataflow id
   * @return the dataflow status
   */
  TypeStatusEnum getDataflowStatus(Long dataflowId);

  /**
   * Undo data collection creation.
   *
   * @param datasetIds the dataset ids
   * @param dataflowId the dataflow id
   * @param isCreation the is creation
   */
  void undoDataCollectionCreation(List<Long> datasetIds, Long dataflowId, boolean isCreation);

  /**
   * Creates the empty data collection.
   *
   * @param dataflowId the dataflow id
   * @param dueDate the due date
   * @param stopAndNotifySQLErrors the stop and notify SQL errors
   * @param manualCheck the manual check
   * @param showPublicInfo the show public info
   * @param referenceDataflow the reference dataflow
   * @param stopAndNotifyPKError the stop and notify PK error
   */
  void createEmptyDataCollection(Long dataflowId, LocalDateTime dueDate,
      boolean stopAndNotifySQLErrors, boolean manualCheck, boolean showPublicInfo,
      boolean referenceDataflow, boolean stopAndNotifyPKError);

  /**
   * Adds the foreign relations from new reportings.
   *
   * @param datasetsRegistry the datasets registry
   */
  void addForeignRelationsFromNewReportings(List<FKDataCollection> datasetsRegistry);

  /**
   * Update data collection.
   *
   * @param dataflowId the dataflow id
   * @param referenceDataflow the reference dataflow
   */
  void updateDataCollection(Long dataflowId, boolean referenceDataflow);


  /**
   * Gets the dataflow metabase.
   *
   * @param dataflowId the dataflow id
   * @return the dataflow metabase
   */
  DataFlowVO getDataflowMetabase(Long dataflowId);

  /**
   * Gets the providers pending to copy into EU.
   *
   * @param dataCollectionId the data collection id
   * @return the providers pending to copy into EU
   */
  List<String> getProvidersPendingToCopyIntoEU(Long dataCollectionId);

  /**
   * Gets the data collection id by dataset schema id
   *
   * @param datasetSchemaId the dataset schema id
   * @return the data collection id
   */
  Long findDataCollectionIdByDatasetSchemaId(String datasetSchemaId);

}
