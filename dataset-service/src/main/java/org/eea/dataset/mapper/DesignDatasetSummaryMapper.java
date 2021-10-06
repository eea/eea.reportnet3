package org.eea.dataset.mapper;

import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The Interface DesignDatasetSummaryMapper.
 */
@Mapper(componentModel = "spring")
public interface DesignDatasetSummaryMapper extends IMapper<DesignDatasetVO, DatasetsSummaryVO> {

}
