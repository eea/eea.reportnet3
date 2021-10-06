package org.eea.dataset.service;

import java.util.List;
import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetPublicVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;

/**
 * The Interface DatasetMetabaseService.
 */
public interface ReportingDatasetService {

  /**
   * Gets the data set id by dataflow id.
   *
   * @param idFlow the id flow
   * @return the data set id by dataflow id
   */
  List<ReportingDatasetVO> getDataSetIdByDataflowId(Long idFlow);

  /**
   * Gets the data set id by dataflow id and schema id.
   *
   * @param schemaId the schema id
   * @return the data set id by dataflow id and schema id
   */
  List<ReportingDatasetVO> getDataSetIdBySchemaId(String schemaId);


  /**
   * Update reporting dataset metabase.
   *
   * @param reportingVO the reporting VO
   */
  void updateReportingDatasetMetabase(ReportingDatasetVO reportingVO);

  /**
   * Gets the data set id public by dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the data set id public by dataflow
   */
  List<ReportingDatasetPublicVO> getDataSetPublicByDataflow(Long dataflowId);



  /**
   * Gets the data set id by dataflow id and data provider id.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the data set id by dataflow id and data provider id
   */
  List<ReportingDatasetVO> getDataSetIdByDataflowIdAndDataProviderId(Long dataflowId,
      Long dataProviderId);

  /**
   * Gets the data set public by dataflow.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @return the data set public by dataflow
   */
  List<ReportingDatasetPublicVO> getDataSetPublicByDataflowAndProviderId(Long dataflowId,
      Long providerId);

  /**
   * Gets the reportings by dataflow ids.
   *
   * @param dataflowIds the dataflow ids
   * @return the reportings by dataflow ids
   */
  List<ReportingDatasetVO> getReportingsByDataflowIds(List<Long> dataflowIds);

  /**
   * Find reporting datasets summary list.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<DatasetsSummaryVO> findReportingDatasetsSummaryList(Long dataflowId);

}
