package org.eea.dataset.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;


/**
 * The Interface DesignDatasetService.
 */
public interface DesignDatasetService {

  /**
   * Gets the design data set id by dataflow id.
   *
   * @param idFlow the id flow
   * @return the design data set id by dataflow id
   */
  List<DesignDatasetVO> getDesignDataSetIdByDataflowId(Long idFlow);


  /**
   * Gets the file name design.
   *
   * @param mimeType the mime type
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   * @return the file name design
   * @throws EEAException the EEA exception
   */
  String getFileNameDesign(String mimeType, String idTableSchema, Long datasetId)
      throws EEAException;

}
