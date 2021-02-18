package org.eea.dataset.mapper;

import org.eea.interfaces.vo.dataset.ReportingDatasetPublicVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The Interface ReportingDatasetMapper.
 */
@Mapper(componentModel = "spring")
public interface ReportingDatasetPublicMapper
    extends IMapper<ReportingDatasetVO, ReportingDatasetPublicVO> {

}
