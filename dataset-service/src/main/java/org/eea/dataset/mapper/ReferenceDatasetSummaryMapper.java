package org.eea.dataset.mapper;

import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface ReferenceDatasetSummaryMapper.
 */
@Mapper(componentModel = "spring")
public interface ReferenceDatasetSummaryMapper
    extends IMapper<ReferenceDatasetVO, DatasetsSummaryVO> {

}
