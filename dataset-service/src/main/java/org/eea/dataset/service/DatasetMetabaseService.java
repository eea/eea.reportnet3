package org.eea.dataset.service;

import java.util.List;
import org.eea.interfaces.vo.dataset.DataSetVO;

public interface DatasetMetabaseService {

  List<DataSetVO> getDataSetIdByDataflowId(Long idFlow);

}
