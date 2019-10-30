package org.eea.dataset.mapper;

import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The Interface DesignDatasetMapper.
 */
@Mapper(componentModel = "spring")
public interface DesignDatasetMapper extends IMapper<DesignDataset, DesignDatasetVO> {

}
