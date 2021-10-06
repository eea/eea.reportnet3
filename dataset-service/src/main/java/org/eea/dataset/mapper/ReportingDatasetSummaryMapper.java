package org.eea.dataset.mapper;

import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface ReportingDatasetSummaryMapper.
 */
@Mapper(componentModel = "spring")
public interface ReportingDatasetSummaryMapper
    extends IMapper<ReportingDatasetVO, DatasetsSummaryVO> {
}
