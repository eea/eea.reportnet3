package org.eea.dataset.service;

import java.util.List;
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

}
