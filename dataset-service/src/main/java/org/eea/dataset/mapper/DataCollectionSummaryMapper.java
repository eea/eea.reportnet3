package org.eea.dataset.mapper;

import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface DataCollectionSummaryMapper.
 */
@Mapper(componentModel = "spring")
public interface DataCollectionSummaryMapper extends IMapper<DataCollectionVO, DatasetsSummaryVO> {

}
