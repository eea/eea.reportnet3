package org.eea.dataset.mapper;

import org.eea.interfaces.vo.dataset.ReferenceDatasetPublicVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;



/**
 * The Interface ReferenceDatasetPublicMapper.
 */
@Mapper(componentModel = "spring")
public interface ReferenceDatasetPublicMapper
    extends IMapper<ReferenceDatasetVO, ReferenceDatasetPublicVO> {

}
