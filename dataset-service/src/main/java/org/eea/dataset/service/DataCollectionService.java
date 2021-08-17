package org.eea.dataset.service;

import java.util.Date;
import java.util.List;
import org.eea.dataset.service.model.FKDataCollection;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataCollectionVO;

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
   * @param manualCheck enable the manual check for the custodian approval
   * @param showPublicInfo the show public info
   * @param referenceDataflow the reference dataflow
   * @param stopAndNotifyPKError the stop and notify PK error
   */
  void createEmptyDataCollection(Long dataflowId, Date dueDate, boolean stopAndNotifySQLErrors,
      boolean manualCheck, boolean showPublicInfo, boolean referenceDataflow,
      boolean stopAndNotifyPKError);

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

}
