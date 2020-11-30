package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The Interface ReportingDatasetMapper.
 */
@Mapper(componentModel = "spring")
public interface ReportingDatasetMapper extends IMapper<ReportingDataset, ReportingDatasetVO> {

}
