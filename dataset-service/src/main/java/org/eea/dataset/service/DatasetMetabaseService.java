package org.eea.dataset.service;

import java.util.Date;
import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.enums.TypeDatasetEnum;

/**
 * The Interface DatasetMetabaseService.
 */
public interface DatasetMetabaseService {

  /**
   * Gets the data set id by dataflow id.
   *
   * @param idFlow the id flow
   * @return the data set id by dataflow id
   */
  List<DataSetMetabaseVO> getDataSetIdByDataflowId(Long idFlow);



  /**
   * Creates the empty dataset.
   *
   * @param datasetType the dataset type
   * @param datasetName the dataset name
   * @param datasetSchemaId the dataset schema id
   * @param dataflowId the dataflow id
   * @param dueDate the due date
   * @param representative the representative
   * @return the long
   * @throws EEAException the EEA exception
   */
  Long createEmptyDataset(TypeDatasetEnum datasetType, String datasetName, String datasetSchemaId,
      Long dataflowId, Date dueDate, RepresentativeVO representative) throws EEAException;

  /**
   * Gets the dataset name.
   *
   * @param idDataset the id dataset
   * @return the dataset name
   */
  DataSetMetabaseVO findDatasetMetabase(Long idDataset);


  /**
   * Delete design dataset.
   *
   * @param datasetId the dataset id
   */
  void deleteDesignDataset(Long datasetId);

  /**
   * Update dataset name.
   *
   * @param datasetId the dataset id
   * @param datasetName the dataset name
   * @return true, if successful
   */
  boolean updateDatasetName(Long datasetId, String datasetName);



  /**
   * Gets the statistics.
   *
   * @param datasetId the dataset id
   * @return the statistics
   * @throws EEAException the EEA exception
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   */
  StatisticsVO getStatistics(Long datasetId)
      throws EEAException, InstantiationException, IllegalAccessException;


  /**
   * Gets the global statistics.
   *
   * @param idDataschema the id dataschema
   * @return the global statistics
   * @throws EEAException the EEA exception
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   */
  List<StatisticsVO> getGlobalStatistics(String idDataschema)
      throws EEAException, InstantiationException, IllegalAccessException;


  /**
   * Creates the group provider and add user.
   *
   * @param datasetId the dataset id
   * @param userMail the user mail
   * @param idDataflow the id dataflow
   */
  void createGroupProviderAndAddUser(Long datasetId, String userMail, Long idDataflow);

  /**
   * Creates the group dc and add user.
   *
   * @param datasetId the dataset id
   */
  void createGroupDcAndAddUser(Long datasetId);


}
