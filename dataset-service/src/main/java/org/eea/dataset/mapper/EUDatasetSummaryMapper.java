package org.eea.dataset.mapper;

import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface EUDatasetSummaryMapper.
 */
@Mapper(componentModel = "spring")
public interface EUDatasetSummaryMapper extends IMapper<EUDatasetVO, DatasetsSummaryVO> {

}
