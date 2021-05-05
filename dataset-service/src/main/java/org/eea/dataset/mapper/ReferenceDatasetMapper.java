package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.ReferenceDataset;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;



/**
 * The Interface ReferenceDatasetMapper.
 */
@Mapper(componentModel = "spring")
public interface ReferenceDatasetMapper extends IMapper<ReferenceDataset, ReferenceDatasetVO> {

}
