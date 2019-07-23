package org.eea.dataset.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.metabase.SnapshotVO;

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

  List<SnapshotVO> getSnapshotsByIdDataset(Long datasetId) throws EEAException;

  void addSnapshot(Long idDataset, String description) throws EEAException;

  void removeSnapshot(Long idDataset, Long idSnapshot) throws EEAException;

}
