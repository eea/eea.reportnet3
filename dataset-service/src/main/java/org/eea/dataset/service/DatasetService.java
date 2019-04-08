package org.eea.dataset.service;

import java.util.List;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.RecordVO;

public interface DatasetService {

  DataSetVO getDatasetById(String datasetId) throws Exception;

  void addRecordToDataset(String datasetId, List<RecordVO> record);

  void createEmptyDataset(String datasetName);

}
