package org.eea.dataset.mapper;

import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataset.TestDatasetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface TestDatasetSummaryMapper.
 */
@Mapper(componentModel = "spring")
public interface TestDatasetSummaryMapper extends IMapper<TestDatasetVO, DatasetsSummaryVO> {

}
